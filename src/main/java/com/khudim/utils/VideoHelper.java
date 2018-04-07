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


    @Value("${scanner.tmpDir}")
    private String tmpDir = "/tmp";

    public static String[] parseRanges(String range) {
        String[] ranges = range.split("=")[1].split("-");

        if (ranges.length < 2) {
            String start = ranges[0];
            String stop = "-1";
            ranges = new String[]{start, stop};
        }
        return ranges;
    }

    public static int[] getVideoSize(String path) {
        try {
            BufferedImage image = ImageIO.read(new File(path));
            return new int[]{image.getWidth(), image.getHeight()};
        } catch (IOException e) {
            log.error("Can't get image size, reason = {}", e);
            return new int[2];
        }
    }

    public static byte[] getImageFromVideo(String path) {
        File tempFile = null;
        Process process = null;
        ST template = new ST(imageEncoderCmd);
        try {
            tempFile = Files.createTempFile("temp", ".jpg").toFile();
            template.add("image", path);
            template.add("file", tempFile);
            ProcessBuilder pb = new ProcessBuilder(template.render().split(" "));
            process = pb.start();
            process.waitFor(20, TimeUnit.SECONDS);

            if (tempFile.length() == 0) {
                log.debug("Can't get image from file " + Paths.get(path).getFileName());
                return new byte[0];
            }
            return Files.readAllBytes(tempFile.toPath());
        } catch (Exception e) {
            return new byte[0];
        } finally {
            if (process != null) {
                process.destroy();
            }
            FileUtils.deleteQuietly(tempFile);
        }
    }

    public static String getNameFromPath(String filePath) {
        return filePath.substring(filePath.lastIndexOf("/") + 1);
    }
}
