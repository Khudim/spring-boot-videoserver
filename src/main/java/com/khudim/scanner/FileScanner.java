package com.khudim.scanner;

import com.khudim.dao.entity.Content;
import com.khudim.dao.entity.Video;
import com.khudim.dao.service.ContentService;
import com.khudim.dao.service.VideoService;
import com.khudim.utils.VideoHelper;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Beaver.
 */
@Component
@Data
public class FileScanner {

    private static Logger log = LoggerFactory.getLogger(FileScanner.class);

    private final ContentService contentService;
    private final VideoService videoService;
    private final VideoHelper videoHelper;

    @Value("${scanner.directory}")
    private String directory;

    @Autowired
    public FileScanner(ContentService contentService, VideoService videoService, VideoHelper videoHelper) {
        this.contentService = contentService;
        this.videoService = videoService;
        this.videoHelper = videoHelper;
    }

    @Scheduled(cron = "${scanner.cron}")
    public void addVideoToBase() {
        log.debug("Start add video to base from directory: {}", directory);
        searchVideo().forEach(this::addContentToBase);
        log.debug("Stop file scanner.");
    }

    @Transactional(rollbackFor = Exception.class)
    private void addContentToBase(Path path) {
        try {
            byte[] image = videoHelper.getImageFromVideo(path);
            int[] videoSize = videoHelper.getVideoSize(path);

            Content content = new Content();
            content.setPath(path.toString());
            content.setImage(image);
            contentService.save(content);

            Video video = new Video();
            video.setContentId(content.getId());
            video.setDate(System.currentTimeMillis());
            video.setWidth(videoSize[0]);
            video.setHeight(videoSize[1]);
            video.setName(path.getFileName().toString());
            videoService.save(video);
        } catch (Exception e) {
            //videoHelper.deleteFile(path);
            log.error("Can't prepare content " + path, e);
        }
    }

    private List<Path> searchVideo() {
        try {
            return Files.walk(Paths.get(directory))
                    .filter(this::isRightPath)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Can't find videoRepository in directory: {}", directory);
        }
        return Collections.emptyList();
    }

    private boolean isRightPath(Path path) {
        return path.toString().endsWith(".webm") && !contentService.isPathExist(path);
    }
}
