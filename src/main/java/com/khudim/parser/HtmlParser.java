package com.khudim.parser;

import com.khudim.dao.service.VideoService;
import com.khudim.utils.ProgressBar;
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
import java.util.*;

import static com.khudim.utils.VideoHelper.VIDEO_TAG;
import static com.khudim.utils.VideoHelper.createFileNameWithTags;
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
    private final VideoService videoService;
    private List<String> searchTags = Arrays.asList(VIDEO_TAG);
    private static int total;

    private ProgressBar progressBar = new ProgressBar();

    @Value("${parser.directory}")
    private String directory;

    @Autowired
    public HtmlParser(VideoService videoService) {
        this.videoService = videoService;
    }

    @Scheduled(cron = "${parser.cron}")
    public void schedulingDownload() {
        LOG.debug("Start scheduling download");
        downloadVideo(Arrays.asList(VIDEO_TAG));
        LOG.debug("Stop scheduling download");
    }

    public void downloadVideo(List<String> tags) {
        LOG.debug("Start downloadVideo");
        progressBar = new ProgressBar();
        Set<String> urls = range(0, progressBar.getScanLimit())
                .mapToObj(i -> {
                    progressBar.riseScanProgress();
                    return parseUrlsForPage(URL + generatePageString(i), tags);
                })
                .flatMap(Set::stream).collect(toSet());

        progressBar.setTotalVideos(urls.size());

        total = urls.size();
        if (total == 0) {
            LOG.debug("Total is 0");
            return;
        }
        urls.forEach(url -> {
            downloadVideo(url, tags);
            progressBar.riseDownloadProgress();
        });
        LOG.debug("Stop downloadVideo");
    }

    private Set<String> parseUrlsForPage(String pageUrl, List<String> searchTags) {
        Set<String> urls = new HashSet<>();
        getDocument(pageUrl, 0)
                .ifPresent(document -> {
                            urls.addAll(document
                                    .addClass("thread_text")
                                    .getAllElements()
                                    .stream()
                                    .filter(element -> checkElement(element, searchTags))
                                    .map(element -> element.attr("href"))
                                    .filter(StringUtils::isNotBlank)
                                    .map(href -> URL + href)
                                    .collect(toSet()));
                            LOG.debug("urls size = " + urls.size());
                        }
                );
        return urls;
    }

    private boolean checkElement(Element element, List<String> searchTags) {
        return searchTags.stream().allMatch(tag -> element.text().toLowerCase().contains(tag.toLowerCase()));
    }

    private void downloadVideo(String url, List<String> searchTags) {
        getDocument(url, 0)
                .ifPresent(document -> document.addClass("img_filename")
                        .getAllElements()
                        .stream()
                        .map(element -> element.attr("href"))
                        .filter(this::checkFile)
                        .forEach(src -> downloadFromSrc(src, getFileNameFromUrl(src), searchTags)));
    }

    private boolean checkFile(String src) {
        return src.endsWith("." + VIDEO_TAG) && !videoService.isRepeated(getFileNameFromUrl(src));
    }

    private String getFileNameFromUrl(String src) {
        return src.substring(src.lastIndexOf("/"));
    }

    private Optional<Document> getDocument(String url, int attemptCount) {
        try {
            return Optional.ofNullable(Jsoup.connect(url)
                    .userAgent("NING/1.0")
                    .get());
        } catch (IOException e) {
            LOG.warn("Can't get document, reason: ", e.getMessage());
            if (attemptCount < 5) {
                sleep();
                return getDocument(url, ++attemptCount);
            } else {
                return Optional.empty();
            }
        }
    }

    private void sleep() {
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e1) {
            e1.printStackTrace();
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

    private void downloadFromSrc(String url, String fileName, List<String> searchTags) {
        try {
            LOG.debug("Start download {}", url);
            URL imgSrc = new URL(url);
            URLConnection con = imgSrc.openConnection();
            con.setRequestProperty("User-Agent", "NING/1.0");
            InputStream is = con.getInputStream();
            FileUtils.copyInputStreamToFile(is, new File(directory + createFileNameWithTags(fileName, searchTags)));
        } catch (IOException e) {
            LOG.error("Can't download from url: {}", url);
        }
    }
}


