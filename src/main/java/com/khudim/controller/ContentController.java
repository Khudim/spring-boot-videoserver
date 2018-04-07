package com.khudim.controller;

import com.khudim.dao.entity.Content;
import com.khudim.dao.entity.Video;
import com.khudim.dao.service.ContentService;
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

import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang.math.NumberUtils.toInt;

/**
 * Created by Beaver.
 */
@RestController
@CrossOrigin
public class ContentController {

    private static Logger log = LoggerFactory.getLogger(ContentController.class);

    private final ContentService contentService;
    private final List<IFileStorage> fileStorages;

    @Autowired
    public ContentController(ContentService contentService, List<IFileStorage> fileStorages) {
        this.contentService = contentService;
        this.fileStorages = fileStorages;
    }

    @GetMapping(value = "/content")
    public ResponseContent getVideo(@RequestParam(required = false) List<String> tags, @RequestParam int page, @RequestParam int limit) {
        long count = contentService.getCount(tags, fileStorages);
        List<Video> videos = contentService.findByTag(tags, page, limit, fileStorages)
                .stream()
                .map(Content::getVideo)
                .collect(toList());
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
                int offset = toInt(ranges[0]);
                int limit = (ranges.length < 2 || "-1".equals(ranges[1])) ? (int) content.getLength() : toInt(ranges[1]);
                bytes = fileStorage.downloadFile(content.getPath(), offset, limit);
                response.setHeader("Accept-Ranges", "bytes");
                response.setHeader("Content-Range", "bytes " + offset + "-" + (limit - 1) + "/" + content.getLength());
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