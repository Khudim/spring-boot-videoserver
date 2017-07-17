package com.khudim.parser;

import com.khudim.dao.service.VideoService;
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
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.IntStream.range;

/**
 * Created by Beaver.
 */
@Component
public class HtmlParser {

    private final static Logger log = LoggerFactory.getLogger(HtmlParser.class);
    private final static String URL = "https://arhivach.org";

    private final VideoService videoService;

    @Value("${parser.directory}")
    private String directory;

    @Autowired
    public HtmlParser(VideoService videoService) {
        this.videoService = videoService;
    }

    @Scheduled(cron = "${parser.cron}")
    public void downloadVideo() {
        log.debug("Start downloadVideo");
        range(0, 30)
                .mapToObj(i -> parseUrlsForPage(URL + generatePageString(i)))
                .flatMap(Set::stream)
                .forEach(this::downloadVideo);
        log.debug("Stop downloadVideo");
    }

    private Set<String> parseUrlsForPage(String pageUrl) {
        Set<String> urls = new HashSet<>();
        getDocument(pageUrl)
                .ifPresent(document -> {
                            urls.addAll(document
                                    .addClass("thread_text")
                                    .getAllElements()
                                    .stream()
                                    .filter(this::checkElement)
                                    .map(element -> element.attr("href"))
                                    .filter(StringUtils::isNotBlank)
                                    .map(href -> URL + href)
                                    .collect(Collectors.toSet()));
                            log.debug("urls size = " + urls.size());
                        }
                );
        return urls;
    }

    private boolean checkElement(Element element) {
        return (element.text().toLowerCase().contains("webm")
                || element.text().toLowerCase().contains("цуиь"))
                && (element.text().toLowerCase().contains("music")
                || element.text().toLowerCase().contains("музыка"));
    }

    private void downloadVideo(String url) {
        getDocument(url)
                .ifPresent(document -> document.addClass("img_filename")
                        .getAllElements()
                        .stream()
                        .map(element -> element.attr("href"))
                        .filter(this::checkFile)
                        .forEach(src -> downloadFromSrc(src, getFileNameFromUrl(src))));
    }

    private boolean checkFile(String src) {
        return src.endsWith(".webm") && !videoService.isRepeated(getFileNameFromUrl(src));
    }

    private String getFileNameFromUrl(String src) {
        return src.substring(src.lastIndexOf("/"));
    }

    private Optional<Document> getDocument(String url) {
        try {
            return Optional.ofNullable(Jsoup.connect(url)
                    .userAgent("NING/1.0")
                    .get());
        } catch (IOException e) {
            log.warn("Can't get document, reason: ", e);
            return Optional.empty();
        }
    }

    private String generatePageString(int numberOfPage) {
        String pagePrefix = "/index/";
        if (numberOfPage <= 0) {
            return pagePrefix;
        } else {
            return pagePrefix + numberOfPage * 25;
        }
    }

    private void downloadFromSrc(String url, String fileName) {
        try {
            log.debug("Start download {}", url);
            URL imgSrc = new URL(url);
            URLConnection con = imgSrc.openConnection();
            con.setRequestProperty("User-Agent", "NING/1.0");
            InputStream is = con.getInputStream();
            FileUtils.copyInputStreamToFile(is, new File(directory + fileName));
        } catch (IOException e) {
            log.error("Can't download from url: {}", url);
        }
    }
}


