package com.khudim.controller;

import com.khudim.dao.entity.Content;
import com.khudim.dao.entity.Video;
import com.khudim.dao.service.ContentService;
import com.khudim.dao.service.VideoService;
import com.khudim.storage.IFileStorage;
import com.khudim.utils.VideoHelper;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.nio.file.NoSuchFileException;
import java.util.List;


/**
 * Created by Beaver.
 */
@RestController
@CrossOrigin
public class ContentController {

    private static Logger log = LoggerFactory.getLogger(ContentController.class);

    private final ContentService contentService;
    private final VideoService videoService;
    private final List<IFileStorage> fileStorages;

    @Autowired
    public ContentController(ContentService contentService, VideoService videoService, List<IFileStorage> fileStorages) {
        this.contentService = contentService;
        this.videoService = videoService;
        this.fileStorages = fileStorages;
    }

    @PostMapping(value = "/content")
    public ResponseContent getVideo(@RequestParam(required = false) List<String> tags, @RequestParam int page, @RequestParam int limit) {
        long count = videoService.getCount(tags);
        List<Video> videos = videoService.findByTag(tags, page, limit);
        return new ResponseContent(count, videos);
    }

    @GetMapping(value = "/img/{contentId}", produces = MediaType.IMAGE_JPEG_VALUE)
    public byte[] getImage(@PathVariable long contentId) {
        return contentService.getImage(contentId);
    }

    @GetMapping(value = "/video/{contentId}")
    public ResponseEntity<byte[]> downloadVideo(@PathVariable long contentId,
                                                HttpServletResponse response,
                                                @RequestHeader(required = false) String range) {
        HttpStatus status;
        byte[] bytes = new byte[0];
        try {
            Content content = contentService.getContent(contentId);
            IFileStorage fileStorage = fileStorages.stream()
                    .filter(storage -> storage.getStorageType().name().equals(content.getStorage()))
                    .findFirst()
                    .orElseThrow(Exception::new);

            if (range == null) {
                bytes = fileStorage.downloadFile(content.getPath());
                status = HttpStatus.OK;
            } else {
                String[] ranges = VideoHelper.parseRanges(range);
                bytes = fileStorage.downloadFile(content.getPath(), ranges);
                response.setHeader("Accept-Ranges", "bytes");
                response.setHeader("Content-Range", "bytes " + ranges[0] + "-" + ranges[1] + "/" + content.getLength());
                status = HttpStatus.PARTIAL_CONTENT;
            }
            response.setContentType("video/webm");
            response.setContentLength(bytes.length);
        } catch (NoSuchFileException e) {
            status = HttpStatus.NOT_FOUND;
        } catch (Exception e) {
            log.error("Can't get video, reason: ", e);
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