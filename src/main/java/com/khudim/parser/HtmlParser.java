package com.khudim.parser;

import com.khudim.dao.entity.Content;
import com.khudim.dao.entity.Tags;
import com.khudim.dao.entity.Video;
import com.khudim.dao.service.ContentService;
import com.khudim.dao.service.TagsService;
import com.khudim.dao.service.VideoService;
import com.khudim.utils.ProgressBar;
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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static com.khudim.utils.Utilities.sleep;
import static com.khudim.utils.VideoHelper.VIDEO_TAG;
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
    private final TagsService tagsService;
    private final VideoService videoService;
    private final ContentService contentService;
    private final ProgressBar progressBar = new ProgressBar();

    @Value("${parser.directory}")
    private String directory;

    @Autowired
    public HtmlParser(VideoService videoService, ContentService contentService, TagsService tagsService) {
        this.videoService = videoService;
        this.contentService = contentService;
        this.tagsService = tagsService;
    }

    @Scheduled(cron = "${parser.cron}")
    public void schedulingDownload() {
        log.debug("Start scheduling download");
        findVideos();
        log.debug("Stop scheduling download");
    }

    @Override
    public void findVideos() {
        if (!progressBar.getInProcess().compareAndSet(false, true)) {
            log.debug("Downloading in progress, can't start new one");
            return;
        }
        log.debug("Start downloadVideo");

        progressBar.reset();

        Set<ContentInfo> contentInfo = range(0, progressBar.getScanLimit())
                .mapToObj(i -> {
                    progressBar.riseScanProgress();
                    return parseUrlsForEachPage(URL + generatePageString(i));
                })
                .flatMap(Set::stream)
                .collect(toSet());

        progressBar.setTotalVideos(contentInfo.size());

        if (contentInfo.size() == 0) {
            log.debug("Total contentInfo == 0");
            progressBar.getInProcess().set(false);
            return;
        }
        contentInfo.forEach(info -> {
            downloadVideo(info);
            progressBar.riseDownloadProgress();
        });
        progressBar.getInProcess().set(false);
        log.debug("Stop downloadVideo");
    }

    private Set<ContentInfo> parseUrlsForEachPage(String pageUrl) {
        return Stream.ofNullable(getConnection(pageUrl, 0))
                .flatMap(this::searchUrls)
                .collect(toSet());
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
        return element.text().toLowerCase().contains(VIDEO_TAG)
                && StringUtils.isNotBlank(element.attr("href"));
    }

    private void downloadVideo(ContentInfo info) {
        Stream.ofNullable(getConnection(info.getThreadUrl(), 0))
                .forEach(document -> downloadByTags(document, info));
    }

    private void downloadByTags(Document document, ContentInfo info) {
        document.addClass("img_filename")
                .getAllElements()
                .stream()
                .map(element -> element.attr("href"))
                .filter(this::checkFile)
                .forEach(src -> {
                    String filePath = directory + getFileNameFromUrl(src);
                    if (downloadFromSrc(src, filePath)) {
                        addContentToBase(info, Paths.get(filePath), getFileNameFromUrl(src));
                    }
                });
    }

    private boolean checkFile(String src) {
        return src.endsWith("." + VIDEO_TAG) && !videoService.isRepeated(getFileNameFromUrl(src));
    }

    private String getFileNameFromUrl(String src) {
        return src.substring(src.lastIndexOf("/") + 1);
    }

    private Document getConnection(String url, int attemptCount) {
        try {
            return Jsoup.connect(url)
                    .userAgent("NING/1.0")
                    .get();
        } catch (IOException e) {
            log.warn("Can't get document, reason: ", e.getMessage());
            if (attemptCount < 5) {
                sleep();
                return getConnection(url, ++attemptCount);
            } else {
                return null;
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

    private boolean downloadFromSrc(String url, String fileName) {
        try {
            log.debug("Start download {}", url);
            URL imgSrc = new URL(url);
            URLConnection con = imgSrc.openConnection();
            con.setRequestProperty("User-Agent", "NING/1.0");
            InputStream is = con.getInputStream();
            FileUtils.copyInputStreamToFile(is, new File(fileName));
            return true;
        } catch (IOException e) {
            log.error("Can't download from url: {}", url);
            return false;
        }
    }

    private void addContentToBase(ContentInfo info, Path contentPath, String fileName) {
        try {
            byte[] image = VideoHelper.getImageFromVideo(contentPath);
            int[] videoSize = VideoHelper.getVideoSize(contentPath);

            Content content = new Content();
            content.setPath(contentPath.toString());
            content.setImage(image);
            contentService.save(content);

            Video video = createVideo(fileName, videoSize, content);
            videoService.save(video);
            Set<Tags> tags = tagsService.findOrCreateTags(info.getTags());
            video.setVideoTags(tags);
            tags.forEach(tag -> {
                tag.addVideo(video);
                tagsService.save(tag);
            });
        } catch (Exception e) {
            //videoHelper.deleteFile(path);
            log.error("Can't prepare content " + contentPath, e);
        }
    }

    private Video createVideo(String fileName, int[] videoSize, Content content) {
        Video video = new Video();
        video.setContentId(content.getId());
        video.setName(fileName);
        video.setDate(System.currentTimeMillis());
        video.setWidth(videoSize[0]);
        video.setHeight(videoSize[1]);
        return video;
    }

    @Data
    @AllArgsConstructor
    public class ContentInfo {
        private String threadUrl;
        private List<String> tags;
    }
}


