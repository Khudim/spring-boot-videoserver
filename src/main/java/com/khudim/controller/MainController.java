package com.khudim.controller;

import com.khudim.dao.entity.Video;
import com.khudim.dao.repository.VideoRepository;
import com.khudim.dao.service.ContentService;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
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

import static com.khudim.utils.VideoHelper.getRangeBytesFromVideo;

/**
 * Created by Beaver.
 */
@RestController
public class MainController {

    private static Logger log = LoggerFactory.getLogger(MainController.class);
    private final ContentService contentService;
    private final VideoRepository videoRepository;

    @Autowired
    public MainController(ContentService contentService, VideoRepository videoRepository) {
        this.contentService = contentService;
        this.videoRepository = videoRepository;
    }

    @RequestMapping(value = "/video", method = RequestMethod.GET)
    public List<Video> getVideo(@RequestParam int page, @RequestParam int limit, HttpServletResponse response) {
        response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
        return videoRepository.findAll(new PageRequest(page, limit)).getContent();
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
                bytes = getRangeBytesFromVideo(filePath, range, response);
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


}