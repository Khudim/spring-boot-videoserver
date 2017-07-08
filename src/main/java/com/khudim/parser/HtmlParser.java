package com.khudim.parser;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Created by Beaver.
 */
@Component
public class HtmlParser {

    private final static Logger log = LoggerFactory.getLogger(HtmlParser.class);
    private final static String URL = "https://arhivach.org";

    @Value("${parser.directory}")
    private String directory;

    @Scheduled(cron = "${parser.cron}")
    public void downloadVideo() {
        log.debug("Start downloadVideo");
        IntStream.range(0, 10)
                .mapToObj(i -> parseUrlsForPage(URL + generatePageString(i)))
                .flatMap(Set::stream)
                .forEach(this::downloadVideo);
    }

    private Set<String> parseUrlsForPage(String pageUrl) {
        Set<String> urls = Collections.emptySet();
        getDocument(pageUrl)
                .ifPresent(document -> urls.addAll(document
                        .addClass("thread_text")
                        .getAllElements()
                        .stream()
                        .filter(this::checkElement)
                        .map(element -> element.attr("href"))
                        .filter(StringUtils::isNotBlank)
                        .map(href -> URL + href)
                        .collect(Collectors.toSet())));
        return urls;
    }

    private boolean checkElement(Element element) {
        return (element.text().toLowerCase().contains("webm")
                || element.text().toLowerCase().contains("цуиь"))
                && (element.text().toLowerCase().contains("music")
                || element.text().toLowerCase().contains("музыкальный"));
    }

    private void downloadVideo(String url) {
        getDocument(url)
                .ifPresent(document -> document.addClass("img_filename")
                        .getAllElements()
                        .stream()
                        .map(element -> element.attr("href"))
                        .filter(src -> src.endsWith(".webm"))
                        .forEach(src -> downloadFromSrc(src, src.substring(src.lastIndexOf("/")))));
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


