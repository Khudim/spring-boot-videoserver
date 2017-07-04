package com.khudim.parser;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by Beaver.
 */
@Component
public class HtmlParser {

    private final static Logger log = LoggerFactory.getLogger(HtmlParser.class);
    private final static String URL = "https://arhivach.org";
    private final static ExecutorService executor = Executors.newFixedThreadPool(10);

    @Value("${scanner.directory}")
    private String directory;

    public void downloadVideo() {
        log.debug("Start downloadVideo");
        Stream.of(0, 10)
                .map(this::parsePage)
                .map(this::handleFuture)
                .flatMap(Set::stream)
                .map(url -> executor.submit(() -> downloadVideo(url)))
                .forEach(this::waitDownload);
    }

    private Future<Set<String>> parsePage(Integer i) {
        String urlWithPage = URL + generatePageString(i);
        return executor.submit(() -> parseUrlsForPage(urlWithPage));
    }

    private Set<String> handleFuture(Future<Set<String>> future) {
        Set<String> urls = Collections.emptySet();
        try {
            urls = future.get();
        } catch (Exception e) {
            log.error("Can't wait result");
        }
        return urls;
    }

    private Set<String> parseUrlsForPage(String url) {
        return getConnection(url)
                .addClass("thread_text")
                .getAllElements()
                .stream()
                .filter(this::checkElement)
                .map(element -> element.attr("href"))
                .filter(StringUtils::isNotBlank)
                .map(href -> URL + href)
                .collect(Collectors.toSet());
    }

    private boolean checkElement(Element element) {
        return element.text().toLowerCase().contains("webm")
                || element.text().toLowerCase().contains("цуиь");
    }

    private void downloadVideo(String url) {
        getConnection(url)
                .addClass("img_filename")
                .getAllElements()
                .stream()
                .map(element -> element.attr("href"))
                .filter(src -> src.endsWith(".webm"))
                .forEach(src -> downloadFromSrc(src, src.substring(src.lastIndexOf("/"))));
    }

    private void waitDownload(Future<?> future) {
        try {
            future.get(30, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("Can't get video urls from thread. ", e);
        }
    }

    private Document getConnection(String url) {
        Document doc;
        try {
            doc = Jsoup.connect(url)
                    .userAgent("NING/1.0")
                    .get();
        } catch (IOException e) {
            log.warn("Can't get connection, reason: ", e);
            waitMeSecond();
            doc = getConnection(url);
        }
        return doc;
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

    private void waitMeSecond() {
        try {
            Thread.sleep(1000L);
        } catch (InterruptedException e) {
            log.error("Can't wait. {}", e.getMessage());
        }
    }

}


