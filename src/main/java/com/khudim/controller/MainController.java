package com.khudim.controller;

import com.khudim.dao.entity.Video;
import com.khudim.dao.service.ContentService;
import com.khudim.dao.service.VideoService;
import com.khudim.parser.HtmlParser;
import com.khudim.scanner.FileScanner;
import com.khudim.utils.ProgressBar;
import com.khudim.utils.VideoHelper;
import lombok.Data;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Beaver.
 */
@RestController
public class MainController {

    private static Logger log = LoggerFactory.getLogger(MainController.class);
    private final ContentService contentService;
    private final VideoService videoService;
    private final VideoHelper videoHelper;
    private final HtmlParser parser;
    private final FileScanner fileScanner;

    private final ExecutorService executorService;

    @Value("{controller.threads}")
    private int threadCount = 2;

    @Autowired
    public MainController(ContentService contentService, VideoService videoService, VideoHelper videoHelper, HtmlParser parser, FileScanner fileScanner) {
        this.contentService = contentService;
        this.videoService = videoService;
        this.videoHelper = videoHelper;
        this.parser = parser;
        this.fileScanner = fileScanner;
        this.executorService = Executors.newFixedThreadPool(threadCount);
    }

    @RequestMapping(value = "/parse", method = RequestMethod.GET)
    public void parse() {
        executorService.submit(fileScanner::addVideoToBase);
    }

    @RequestMapping(value = "/download", method = RequestMethod.GET)
    public void download() {
        executorService.submit(parser::downloadVideo);
    }

    @RequestMapping(value = "/progress", method = RequestMethod.GET)
    public ProgressBar getDownloadProgress() {
        return parser.getProgressBar();
    }

    @RequestMapping(value = "/video", method = RequestMethod.GET)
    public ResponseContent getVideo(@RequestParam int page, @RequestParam int limit, HttpServletResponse response) {
        response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
        long count = videoService.getCount();
        List<Video> videos = videoService.findAll(page, limit);
        return new ResponseContent(count, videos);
    }

    @RequestMapping(value = "/video", method = RequestMethod.POST)
    public ResponseContent getVideo(@RequestParam List<String> tags, @RequestParam int page, @RequestParam int limit, HttpServletResponse response) {
        response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
        long count = videoService.getCount(tags);
        List<Video> videos = videoService.findByTag(tags, page, limit);
        return new ResponseContent(count, videos);
    }

    @RequestMapping(value = "/img/{contentId}", produces = MediaType.IMAGE_JPEG_VALUE)
    public byte[] getImage(@PathVariable long contentId) {
        return contentService.getImage(contentId);
    }

    @RequestMapping(value = "/video/{contentId}", method = RequestMethod.GET)
    public ResponseEntity<byte[]> getVideo(@PathVariable long contentId,
                                           HttpServletResponse response,
                                           @RequestHeader(required = false) String range) {
        HttpStatus status;
        byte[] bytes = new byte[0];
        try {
            String filePath = contentService.getVideoPath(contentId);
            if (StringUtils.isBlank(range)) {
                bytes = Files.readAllBytes(Paths.get(filePath));
                status = HttpStatus.OK;
            } else {
                bytes = videoHelper.getRangeBytesFromVideo(filePath, range, response);
                status = HttpStatus.PARTIAL_CONTENT;
            }
            response.setContentType("video/webm");
            response.setContentLength(bytes.length);
        } catch (IOException e) {
            log.error("Can't get range bytes from video, reason: ", e);
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        return new ResponseEntity<>(bytes, status);
    }

    @Data
    public class ResponseContent {
        private long count;
        private List<?> content;

        ResponseContent(long count, List<?> content) {
            this.count = count;
            this.content = content;
        }
    }
}