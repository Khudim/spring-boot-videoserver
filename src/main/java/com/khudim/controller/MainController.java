package com.khudim.controller;

import com.khudim.dao.entity.Video;
import com.khudim.dao.service.ContentService;
import com.khudim.dao.service.VideoService;
import com.khudim.parser.HtmlParser;
import com.khudim.utils.VideoHelper;
import lombok.Data;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

/**
 * Created by Beaver.
 */
@RestController
public class MainController {

    private static Logger log = LoggerFactory.getLogger(MainController.class);
    private final ContentService contentService;
    private final VideoService videoService;
    private final VideoHelper videoHelper;

    @Autowired
    private HtmlParser parser;

    @Autowired
    public MainController(ContentService contentService, VideoService videoService, VideoHelper videoHelper) {
        this.contentService = contentService;
        this.videoService = videoService;
        this.videoHelper = videoHelper;
    }

    @RequestMapping(value = "/test", method = RequestMethod.GET)
    public void test() {
        parser.downloadVideo();
    }

    @RequestMapping(value = "/video", method = RequestMethod.GET)
    public ResponseContent getVideo(@RequestParam int page, @RequestParam int limit, HttpServletResponse response) {
        response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
        long count = videoService.getCount();
        List<Video> videos = videoService.findAll(page, limit);
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