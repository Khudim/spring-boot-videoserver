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
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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
public class HtmlParser {

    private final static Logger LOG = LoggerFactory.getLogger(HtmlParser.class);
    private final static String URL = "https://arhivach.org";
    private final static int PAGE_LIMIT = 30;
    private final TagsService tagsService;
    private final VideoService videoService;
    private final ContentService contentService;
    private ProgressBar progressBar = new ProgressBar();

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
        LOG.debug("Start scheduling download");
        findVideos();
        LOG.debug("Stop scheduling download");
    }

    public void findVideos() {
        LOG.debug("Start downloadVideo");
        progressBar = new ProgressBar();
        Set<ContentInfo> contentInfos = range(0, progressBar.getScanLimit())
                .mapToObj(i -> {
                    progressBar.riseScanProgress();
                    return parseUrlsForPage(URL + generatePageString(i));
                })
                .flatMap(Set::stream)
                .collect(toSet());

        progressBar.setTotalVideos(contentInfos.size());

        if (contentInfos.size() == 0) {
            LOG.debug("Total contentInfos == 0");
            return;
        }
        contentInfos.forEach(contentInfo -> {
            downloadVideo(contentInfo);
            progressBar.riseDownloadProgress();
        });
        LOG.debug("Stop downloadVideo");
    }

    private Set<ContentInfo> parseUrlsForPage(String pageUrl) {
        return Stream.ofNullable(getConnection(pageUrl, 0))
                .flatMap(this::searchUrls)
                .collect(toSet());
    }

    private Stream<ContentInfo> searchUrls(Document document) {
        return document.addClass("thread_text")
                .getAllElements()
                .stream()
                .filter(this::checkElement)
                .map(element -> new ContentInfo(element.attr("href"), List.of(element.text().toLowerCase().split(" "))));
    }

    private boolean checkElement(Element element) {
        return element.text().toLowerCase().contains(VIDEO_TAG);
    }

    private void downloadVideo(ContentInfo info) {
        Stream.ofNullable(getConnection(info.getThreadUrl(), 0)).forEach(document -> downloadByTags(document, info));
    }

    private void downloadByTags(Document document, ContentInfo info) {
        document.addClass("img_filename")
                .getAllElements()
                .stream()
                .map(element -> element.attr("href"))
                .filter(this::checkFile)
                .forEach(src -> {
                    String filePath = directory + getFileNameFromUrl(src) + "." + VIDEO_TAG;
                    if (downloadFromSrc(src, filePath)) {
                        addContentToBase(info, Paths.get(filePath), getFileNameFromUrl(src));
                    }
                });
    }

    private boolean checkFile(String src) {
        return src.endsWith("." + VIDEO_TAG) && !videoService.isRepeated(getFileNameFromUrl(src));
    }

    private String getFileNameFromUrl(String src) {
        return src.substring(src.lastIndexOf("/"));
    }

    private Document getConnection(String url, int attemptCount) {
        try {
            return Jsoup.connect(url)
                    .userAgent("NING/1.0")
                    .get();
        } catch (IOException e) {
            LOG.warn("Can't get document, reason: ", e.getMessage());
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
            LOG.debug("Start download {}", url);
            URL imgSrc = new URL(url);
            URLConnection con = imgSrc.openConnection();
            con.setRequestProperty("User-Agent", "NING/1.0");
            InputStream is = con.getInputStream();
            FileUtils.copyInputStreamToFile(is, new File(fileName));
            return true;
        } catch (IOException e) {
            LOG.error("Can't download from url: {}", url);
            return false;
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void addContentToBase(ContentInfo info, Path contentPath, String fileName) {
        try {
            byte[] image = VideoHelper.getImageFromVideo(contentPath);
            int[] videoSize = VideoHelper.getVideoSize(contentPath);

            Content content = new Content();
            content.setPath(contentPath.toString());
            content.setImage(image);
            contentService.save(content);

            Video video = createVideo(fileName, videoSize, content);
            prepareTags(info, video);

            videoService.save(video);
        } catch (Exception e) {
            //videoHelper.deleteFile(path);
            LOG.error("Can't prepare content " + contentPath, e);
        }
    }

    private void prepareTags(ContentInfo info, Video video) {
        Set<Tags> tags = tagsService.findTags(info.getTags());
        video.setVideoTags(tags);
        tags.forEach(t -> t.getVideos().add(video));
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


