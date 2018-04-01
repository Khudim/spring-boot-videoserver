package com.khudim.controller;

import com.khudim.dao.entity.Content;
import com.khudim.dao.entity.Video;
import com.khudim.dao.service.ContentService;
import com.khudim.dao.service.VideoService;
import com.khudim.parser.HtmlParser;
import com.khudim.parser.IHtmlParser;
import com.khudim.storage.IFileStorage;
import com.khudim.utils.ProgressBar;
import com.khudim.utils.VideoHelper;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.nio.file.NoSuchFileException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * Created by Beaver.
 */
@RestController
@CrossOrigin
public class MainController {

    private static Logger log = LoggerFactory.getLogger(MainController.class);

    private final ContentService contentService;
    private final VideoService videoService;
    private final IHtmlParser parser;
    private final ExecutorService executorService;

    private final List<IFileStorage> fileStorages;

    @Value("${controller.threads}")
    private int threadCount = 10;

    @Autowired
    public MainController(ContentService contentService, VideoService videoService, HtmlParser parser, List<IFileStorage> fileStorages) {
        this.contentService = contentService;
        this.videoService = videoService;
        this.parser = parser;
        this.fileStorages = fileStorages;
        this.executorService = Executors.newFixedThreadPool(threadCount);
    }

    @RequestMapping(value = "/download", method = RequestMethod.GET)
    public void download() {
        executorService.submit(parser::findVideos);
    }

    @RequestMapping(value = "/progress", method = RequestMethod.GET)
    public ProgressBar getDownloadProgress() {
        return parser.getProgressBar();
    }

    @RequestMapping(value = "/content", method = RequestMethod.GET)
    public ResponseContent getVideo(@RequestParam(required = false) int page, @RequestParam(required = false) int limit) {
        long count = videoService.getCount();
        List<Video> videos = videoService.findAll(page, limit);
        return new ResponseContent(count, videos);
    }
/*
    @RequestMapping(value = "/content", method = RequestMethod.POST)
    public ResponseContent getVideo(@RequestParam List<String> tags, @RequestParam int page, @RequestParam int limit) {
        long count = videoService.getCount(tags);
        List<Video> videos = videoService.findByTag(tags, page, limit);
        return new ResponseContent(count, videos);
    }
*/
    @RequestMapping(value = "/img/{contentId}", produces = MediaType.IMAGE_JPEG_VALUE)
    public byte[] getImage(@PathVariable long contentId) {
        return contentService.getImage(contentId);
    }

    @RequestMapping(value = "/video/{contentId}", method = RequestMethod.GET)
    public ResponseEntity<byte[]> downloadVideo(@PathVariable long contentId,
                                                HttpServletResponse response,
                                                @RequestHeader(required = false) String range) {
        HttpStatus status;
        byte[] bytes = new byte[0];
        try {
            Content content = contentService.getContent(contentId);
            IFileStorage fileStorage = fileStorages.stream()
                    .filter(storage -> storage.getStorageName().equals(content.getStorage()))
                    .findFirst()
                    .orElseThrow(Exception::new);

            bytes = fileStorage.downloadFile(content.getPath(), VideoHelper.parseRanges(range));
            status = HttpStatus.PARTIAL_CONTENT;
            response.setContentType("video/webm");
            response.setContentLength(bytes.length);
        } catch (NoSuchFileException e) {
            status = HttpStatus.NOT_FOUND;
        } catch (Exception e) {
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