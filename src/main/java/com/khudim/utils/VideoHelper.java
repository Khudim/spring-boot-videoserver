package com.khudim.utils;

import lombok.Data;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.stringtemplate.v4.ST;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.TimeUnit;


/**
 * Created by Beaver.
 */
@Component
@Data
@ConfigurationProperties(prefix = "video")
public class VideoHelper {

    public final static List<String> ALLOWED_VIDEO_TYPES = List.of("webm", "mp4");

    private static Logger log = LoggerFactory.getLogger(VideoHelper.class);
    private static String imageEncoderCmd = "ffmpeg -ss 00:00:01 -i <image> -vframes 1 -q:v 31 <file> -y";
    private static String expectedVideoFormat = ".mp4";
    private static String videoEncodeCmd = "ffmpeg -i <source> <target> -y";

    private static String tmpDir = "/tmp";

    public static String[] parseRanges(String range) {
        String[] ranges = range.split("=")[1].split("-");

        if (ranges.length < 2) {
            String start = ranges[0];
            String stop = "-1";
            ranges = new String[]{start, stop};
        }
        return ranges;
    }

    public static int[] getVideoSize(File imageFile) {
        try {
            BufferedImage image = ImageIO.read(imageFile);
            if (image != null) {
                return new int[]{image.getWidth(), image.getHeight()};
            }
        } catch (IOException e) {
            log.error("Can't get image size, reason = {}", e);

        }
        return new int[2];
    }

    public static File encodeVideo(File file) {
        log.debug("Start encode video = {}", file);
        Process process = null;
        ST template = new ST(videoEncodeCmd);
        String fileName = getNameWithoutExtension(file.getPath());
        try {
            File targetFile = Files.createTempFile(fileName, expectedVideoFormat).toFile();
            template.add("source", file.getPath());
            template.add("target", targetFile);
            ProcessBuilder pb = new ProcessBuilder(template.render().split(" "));
            pb.redirectError(ProcessBuilder.Redirect.appendTo(new File(tmpDir + "error.txt")));
            process = pb.start();
            process.waitFor(120, TimeUnit.SECONDS);
            return targetFile;
        } catch (Exception e) {
            log.error("Can't encode video, reason: {}", e);
            return null;
        } finally {
            if (process != null) {
                process.destroy();
            }
            FileUtils.deleteQuietly(file);
        }
    }

    public static File getImageFromVideo(String path) throws Exception {
        Process process = null;
        ST template = new ST(imageEncoderCmd);
        try {
            File imageFile = Files.createTempFile("temp", ".jpg").toFile();
            template.add("image", path);
            template.add("file", imageFile);
            ProcessBuilder pb = new ProcessBuilder(template.render().split(" "));
            process = pb.start();
            process.waitFor(20, TimeUnit.SECONDS);
            return imageFile;
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
    }

    public static String getNameFromPath(String filePath) {
        return Paths.get(filePath).getFileName().toString();
    }

    private static String getNameWithoutExtension(String filePath) {
        String nameWithExtension = getNameFromPath(filePath);
        return nameWithExtension.substring(0, nameWithExtension.lastIndexOf("."));
    }

    @Value("${video.tmpDir}")
    public void setTmpDir(String tmpDir) {
        VideoHelper.tmpDir = tmpDir;
    }
}
