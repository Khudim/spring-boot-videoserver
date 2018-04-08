package com.khudim.parser;

import com.khudim.dao.entity.Content;
import com.khudim.dao.entity.Tags;
import com.khudim.dao.entity.Video;
import com.khudim.dao.service.ContentService;
import com.khudim.dao.service.TagsService;
import com.khudim.dao.service.VideoService;
import com.khudim.storage.IFileStorage;
import com.khudim.storage.StorageType;
import com.khudim.utils.VideoHelper;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import static com.khudim.utils.Utilities.sleep;
import static com.khudim.utils.VideoHelper.ALLOWED_VIDEO_TYPES;
import static com.khudim.utils.VideoHelper.encodeVideo;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.IntStream.range;

/**
 * Created by Beaver.
 */
@Data
@Component
public class HtmlParser implements IHtmlParser {

    private final static Logger log = LoggerFactory.getLogger(HtmlParser.class);
    private final static String URL = "https://arhivach.org";
    private final static int PAGE_LIMIT = 30;

    private final static AtomicBoolean inProgress = new AtomicBoolean(false);

    private final TagsService tagsService;
    private final VideoService videoService;
    private final ContentService contentService;

    private List<IFileStorage> fileStorages;

    @Value("${parser.directory}")
    private String directory;

    @Autowired
    public HtmlParser(VideoService videoService, ContentService contentService, TagsService tagsService, List<IFileStorage> fileStorages) {
        this.videoService = videoService;
        this.contentService = contentService;
        this.tagsService = tagsService;
        this.fileStorages = fileStorages;
    }

    @Scheduled(cron = "${parser.cron}")
    public void schedulingDownload() {
        log.debug("Start scheduling download");
        findVideos();
        log.debug("Stop scheduling download");
    }

    @Override
    public void findVideos() {
        if (!inProgress.compareAndSet(false, true)) {
            log.debug("Downloading in progress, can't start new one");
            return;
        }
        log.debug("Start downloadVideo");

        range(0, PAGE_LIMIT)
                .mapToObj(page -> parseUrlsForEachPage(URL + generatePageString(page)))
                .flatMap(Set::stream)
                .forEach(this::downloadVideo);

        inProgress.set(false);
        log.debug("Stop downloadVideo");
    }

    private Set<ContentInfo> parseUrlsForEachPage(String pageUrl) {
        Set<ContentInfo> contentInfos = new HashSet<>();
        getConnection(pageUrl)
                .ifPresent(document -> contentInfos.addAll(searchUrls(document).collect(toSet())));
        return contentInfos;
    }

    private Stream<ContentInfo> searchUrls(Document document) {
        return document.addClass("thread_text")
                .getAllElements()
                .stream()
                .filter(this::checkElement)
                .map(element -> new ContentInfo(URL + element.attr("href"),
                        List.of(element.text().toLowerCase().split(" "))));
    }

    private boolean checkElement(Element element) {
        return isValid(element) && StringUtils.isNotBlank(element.attr("href"));
    }

    private boolean isValid(Element element) {
        return ALLOWED_VIDEO_TYPES.stream().anyMatch(type -> element.text().toLowerCase().contains(type));
    }

    private void downloadVideo(ContentInfo info) {
        getConnection(info.getThreadUrl())
                .ifPresent(document -> downloadByTags(document, info));
    }

    private void downloadByTags(Document document, ContentInfo info) {
        document.addClass("img_filename")
                .getAllElements()
                .stream()
                .map(element -> element.attr("href"))
                .filter(this::checkFile)
                .forEach(src -> {
                    File file = new File(directory + getFileNameFromUrl(src));
                    IFileStorage fileStorage = selectStorage();
                    if (downloadFromSrc(src, file)) {
                        if (!file.getPath().endsWith(".mp4")) {
                            file = encodeVideo(file);
                        }
                        if (file != null && fileStorage.uploadFile(file.getPath())) {
                            saveInfo(info, file, fileStorage.getStorageType());
                        }
                    }
                });
    }

    private IFileStorage selectStorage() {
        //TODO select storage by.. i don't know yet
        return fileStorages.get(0);
    }

    private boolean checkFile(String src) {
        String videoType = src.substring(src.lastIndexOf(".") + 1, src.length());
        return ALLOWED_VIDEO_TYPES.contains(videoType)
                && !videoService.isRepeated(getFileNameFromUrl(src));
    }

    private String getFileNameFromUrl(String src) {
        return src.substring(src.lastIndexOf("/") + 1);
    }

    private Optional<Document> getConnection(String pageUrl) {
        return getConnection(pageUrl, 0);
    }

    private Optional<Document> getConnection(String url, int attemptCount) {
        try {
            return Optional.of(Jsoup.connect(url)
                    .userAgent("NING/1.0")
                    .get());
        } catch (IOException e) {
            log.warn("Can't get document, reason: ", e.getCause());
            if (attemptCount < 5) {
                sleep();
                return getConnection(url, ++attemptCount);
            } else {
                return Optional.empty();
            }
        }
    }

    private String generatePageString(int numberOfPage) {
        String indexPage = "/index/";
        if (numberOfPage <= 0) {
            return indexPage;
        } else {
            return indexPage + numberOfPage * 25;
        }
    }

    private boolean downloadFromSrc(String url, File fileName) {
        try {
            log.debug("Start download {}", url);
            URL imgSrc = new URL(url);
            URLConnection con = imgSrc.openConnection();
            con.setRequestProperty("User-Agent", "NING/1.0");
            InputStream is = con.getInputStream();
            FileUtils.copyInputStreamToFile(is, fileName);
            return true;
        } catch (IOException e) {
            log.error("Can't download from url: {}, reason: {}", url, e.getCause());
            return false;
        }
    }

    private void saveInfo(ContentInfo info, File file, StorageType storageType) {
        File image = null;
        try {
            image = VideoHelper.getImageFromVideo(file.getPath());
            int[] videoSize = VideoHelper.getVideoSize(image);

            Content content = createContent(file, storageType, image);
            Video video = createVideo(file.getName(), videoSize);
            content.setVideo(video);
            video.setContent(content);

            Set<Tags> tags = tagsService.findTags(info.getTags());
            content.setContentTags(tags);
            tags.forEach(tag -> {
                tag.addContent(content);
                tagsService.save(tag);
            });
        } catch (Exception e) {
            log.error("Can't prepare content " + file, e);
        } finally {
            FileUtils.deleteQuietly(image);
            FileUtils.deleteQuietly(file);
        }
    }

    private Content createContent(File file, StorageType storageType, File image) throws IOException {
        Content content = new Content();
        content.setPath(file.getName());
        content.setImage(Files.readAllBytes(image.toPath()));
        content.setLength(file.length());
        content.setStorage(storageType.name());
        return contentService.save(content);
    }

    private Video createVideo(String fileName, int[] videoSize) {
        Video video = new Video();
        video.setName(fileName);
        video.setDate(System.currentTimeMillis());
        video.setWidth(videoSize[0]);
        video.setHeight(videoSize[1]);
        return videoService.save(video);
    }

    @Data
    @AllArgsConstructor
    public class ContentInfo {
        private String threadUrl;
        private List<String> tags;
    }
}


