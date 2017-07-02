package com.khudim.controller;

import com.khudim.dao.entity.Video;
import com.khudim.dao.repository.VideoRepository;
import com.khudim.dao.service.ContentService;
import com.khudim.parser.HtmlParser;
import com.khudim.scanner.FileScanner;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static com.khudim.utils.VideoHelper.getRangeBytesFromVideo;

/**
 * Created by Beaver.
 */
@RestController
public class MainController {

    private static Logger log = LoggerFactory.getLogger(MainController.class);
    private final ContentService contentService;
    private final VideoRepository videoRepository;
    private final FileScanner fileScanner;
    private final HtmlParser htmlParser;

    @Autowired
    public MainController(ContentService contentService, VideoRepository videoRepository, FileScanner fileScanner, HtmlParser htmlParser) {
        this.contentService = contentService;
        this.videoRepository = videoRepository;
        this.fileScanner = fileScanner;
        this.htmlParser = htmlParser;
    }

    @RequestMapping(value = "/video", method = RequestMethod.GET)
    public List<Video> getVideo(@RequestParam int page, @RequestParam int limit, HttpServletResponse response) {
        response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
        return videoRepository.findAll(new PageRequest(page, limit)).getContent();
    }

    @RequestMapping(value = "/addVideo", method = RequestMethod.GET)
    public void addVideo() {
        fileScanner.addVideoToBase();
    }

    @RequestMapping(value = "/downloadVideo", method = RequestMethod.GET)
    public void startParse() {
        htmlParser.downloadVideo();
    }

    @RequestMapping(value = "/img/{contentId}", produces = MediaType.IMAGE_JPEG_VALUE)
    public byte[] getImage(@PathVariable long contentId) {
        return contentService.getImage(contentId);
    }

    @RequestMapping(value = "/video/{contentId}", method = RequestMethod.GET)
    public byte[] getVideo(@PathVariable long contentId,
                           HttpServletResponse response,
                           @RequestHeader(required = false) String range) {

        String filePath = contentService.getVideoPath(contentId);
        byte[] bytes = new byte[0];
        try {
            if (StringUtils.isBlank(range)) {
                bytes = Files.readAllBytes(Paths.get(filePath));
                response.setStatus(200);
            } else {
                bytes = getRangeBytesFromVideo(filePath, range, response);
                response.setStatus(206);
            }
            response.setContentType("video/webm");
            response.setContentLength(bytes.length);
        } catch (IOException e) {
            log.error("Can't get range bytes from video, reason: ", e);
            response.setStatus(500);
        }
        return bytes;
    }


}