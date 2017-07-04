package com.khudim.scanner;

import com.khudim.dao.entity.Content;
import com.khudim.dao.entity.Video;
import com.khudim.dao.repository.VideoRepository;
import com.khudim.dao.service.ContentService;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.stringtemplate.v4.ST;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.apache.commons.lang.math.NumberUtils.toInt;

/**
 * Created by Beaver.
 */
@Component
@Data
@ConfigurationProperties(prefix = "scanner")
public class FileScanner {

    private static Logger log = LoggerFactory.getLogger(FileScanner.class);

    private final ContentService contentService;
    private final VideoRepository videoRepository;

    private String directory;
    private String imageEncoderCmd;
    private String videoSizeCmd;

    @Autowired
    public FileScanner(ContentService contentService, VideoRepository videoRepository) {
        this.contentService = contentService;
        this.videoRepository = videoRepository;
    }

    public void addVideoToBase() {
        log.debug("Start add videoRepository to base");
        searchVideo().forEach(path -> {
            Content content = new Content();
            content.setPath(path.toString());
            content.setImage(getImageFromVideo(path));
            contentService.save(content);

            int[] videoSize = findVideoSize(path.toString());

            Video video = new Video();
            video.setContentId(content.getId());
            video.setDate(System.currentTimeMillis());
            video.setWidth(videoSize[0]);
            video.setHeight(videoSize[1]);
            video.setName(path.getFileName().toString());
            videoRepository.save(video);
        });
    }

    private byte[] getImageFromVideo(Path path) {
        Path tempFile = null;
        Process process = null;
        ST template = new ST(imageEncoderCmd);
        try {
            tempFile = Files.createTempFile("temp", ".jpg");
            template.add("image", path);
            template.add("file", tempFile);
            process = new ProcessBuilder(template.render().split(" ")).start();
            process.waitFor(20, TimeUnit.SECONDS);
            return Files.readAllBytes(tempFile);
        } catch (IOException | InterruptedException e) {
            log.error("Can't get image from file: {}, reason: {}", path, e);
        } finally {
            if (process != null) {
                process.destroy();
            }
            deleteFile(tempFile);
        }
        return new byte[0];
    }

    private void deleteFile(Path tempFile) {
        try {
            if (tempFile != null) {
                Files.delete(tempFile);
            }
        } catch (IOException e) {
            log.error("Can't delete file: {}", e);
        }
    }

    private List<Path> searchVideo() {
        try {
            return Files.walk(Paths.get(directory))
                    .filter(this::isRightPath)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            log.error("Can't find videoRepository in directory: {}", directory);
        }
        return Collections.emptyList();
    }

    private boolean isRightPath(Path path) {
        return path.toString().endsWith(".webm") && !contentService.isPathExist(path);
    }

    private int[] findVideoSize(String videoPath) {
        ST template = new ST(videoSizeCmd);
        template.add("video", videoPath);
        int width = 0;
        int height = 0;
        Process pb = null;
        try {
            pb = new ProcessBuilder(template.render().split(" ")).start();
            pb.waitFor();
            String[] output = output(pb.getInputStream());
            width = toInt(output[0].split("=")[1]);
            height = toInt(output[1].split("=")[1]);
            log.debug("width: {}, height: {}", width, height);
        } catch (Exception e) {
            System.out.println(e);
            log.error("Can't find videoRepository size file: {}, reason: {}", videoPath, e);
        } finally {
            if (pb != null) {
                pb.destroy();
            }
        }
        return new int[]{width, height};
    }

    private String[] output(InputStream inputStream) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            return reader.lines().toArray(String[]::new);
        }
    }
}
