package com.khudim.utils;

import lombok.Data;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.stringtemplate.v4.ST;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
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

    private String imageEncoderCmd = "ffmpeg -ss 00:00:01 -i <image> -vframes 1 -q:v 31 <file> -y";
    private String videoSizeCmd = "ffprobe -v error -show_entries stream=width,height -of default=noprint_wrappers=1 <video>";

    @Value("${scanner.tmpDir}")
    private String tmpDir = "/tmp";

    public byte[] getRangeBytesFromVideo(String filePath, String range, HttpServletResponse response) throws IOException {
        File file = new File(filePath);
        long fileLength = file.length();

        String[] ranges = parseRanges(range, fileLength);
        int start = Integer.parseInt(ranges[0]);
        int stop = Integer.parseInt(ranges[1]);

        response.setHeader("Accept-Ranges", "bytes");
        response.setHeader("Content-Range", "bytes " + start + "-" + stop + "/" + fileLength);

        byte[] bytes = new byte[stop - start + 1];
        try (RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw")) {
            randomAccessFile.seek(start);
            randomAccessFile.readFully(bytes, 0, stop - start);
        }
        return bytes;
    }

    private String[] parseRanges(String range, long fileLength) {

        String[] ranges = range.split("=")[1].split("-");

        if (ranges.length < 2) {
            String start = ranges[0];
            String stop = String.valueOf(fileLength - 1);
            ranges = new String[]{start, stop};
        }

        long stop = Long.parseLong(ranges[1]);

        if (stop > fileLength) {
            stop = fileLength - 1;
            ranges[1] = String.valueOf(stop);
        }

        return ranges;
    }

    public int[] getVideoSize(Path path) throws IOException, InterruptedException {
        int width;
        int height;
        Process pb = null;
        try {
            pb = new ProcessBuilder(createVideoSizeCmd(path)
                    .render()
                    .split(" "))
                    .start();
            pb.waitFor(10, TimeUnit.SECONDS);

            String[] output = output(pb.getInputStream());
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

    private String[] output(InputStream inputStream) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            return reader.lines().toArray(String[]::new);
        }
    }

    public byte[] getImageFromVideo(Path path) {
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
                log.debug("Can't get image from file " + path.getFileName());
                return new byte[0];
            }
            return Files.readAllBytes(tempFile.toPath());
        } catch (Exception e) {
            return new byte[0];
        } finally {
            if (process != null) {
                process.destroy();
            }
            if (tempFile != null) {
                deleteFile(tempFile.toPath());
            }
        }
    }

    private ST createVideoSizeCmd(Path path) {
        ST template = new ST(videoSizeCmd);
        template.add("video", path.toString());
        template.render();
        return template;
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

    public static String createFileNameWithTags(String fileName, List<String> tags) {
        String extension = "." + VIDEO_TAG;
        return fileName.replaceAll(extension, "_" + StringUtils.join(tags, ";")) + extension;
    }
}
