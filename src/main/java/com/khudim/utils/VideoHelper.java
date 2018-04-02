package com.khudim.utils;

import lombok.Data;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.stringtemplate.v4.ST;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

import static org.apache.commons.lang.math.NumberUtils.toInt;


/**
 * Created by Beaver.
 */
@Component
@Data
@ConfigurationProperties(prefix = "video")
public class VideoHelper {
    public final static String VIDEO_TAG = "webm";
    private static Logger log = LoggerFactory.getLogger(VideoHelper.class);

    private final static String imageEncoderCmd = "ffmpeg -ss 00:00:01 -i <image> -vframes 1 -q:v 31 <file> -y";
    private final static String videoSizeCmd = "ffprobe -v error -show_entries stream=width,height -of default=noprint_wrappers=1 <video>";

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

    public static int[] getVideoSize(String path) throws IOException, InterruptedException {
        int width;
        int height;
        Process pb = null;
        try {
            pb = new ProcessBuilder(createVideoSizeCmd(path)).start();
            pb.waitFor(10, TimeUnit.SECONDS);

            String[] output = readOutput(pb.getInputStream());
            if (output.length < 2) {
                throw new IOException("Can't get video parameters with command: " + videoSizeCmd);
            }
            width = toInt(output[0].split("=")[1]);
            height = toInt(output[1].split("=")[1]);
        } finally {
            if (pb != null) {
                pb.destroy();
            }
        }
        return new int[]{width, height};
    }

    private static String[] readOutput(InputStream inputStream) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            return reader.lines().toArray(String[]::new);
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

    private static String[] createVideoSizeCmd(String path) {
        return new ST(videoSizeCmd).add("video", path).render().split(" ");
    }

    public static String getNameFromPath(String filePath) {
        return filePath.substring(filePath.lastIndexOf("/") + 1);
    }
}
