package com.khudim.utils;

import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.stringtemplate.v4.ST;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import static org.apache.commons.lang.math.NumberUtils.toInt;


/**
 * Created by Beaver.
 */
@Component
@Data
@ConfigurationProperties(prefix = "video")
public class VideoHelper {

    private static Logger log = LoggerFactory.getLogger(VideoHelper.class);

    private String imageEncoderCmd = "ffmpeg -ss 00:00:01 -i <image> -vframes 1 -q:v 31 <file> -y";
    private String videoSizeCmd = "ffprobe -v error -show_entries stream=width,height -of default=noprint_wrappers=1 <video>";

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
            pb.waitFor();

            String[] output = output(pb.getInputStream());
            if (output.length != 2) {
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

    public byte[] getImageFromVideo(Path path) throws IOException, InterruptedException {
        Path tempFile = null;
        Process process = null;
        ST template = new ST(imageEncoderCmd);
        try {
            tempFile = Files.createTempFile("temp", ".jpg");
            template.add("image", path);
            template.add("file", tempFile);

            process = new ProcessBuilder(template.render().split(" ")).start();
            process.waitFor(40, TimeUnit.SECONDS);

            if (tempFile.toFile().length() == 0) {
                throw new IOException("Can't get image from video with command: " + imageEncoderCmd);
            }
            return Files.readAllBytes(tempFile);
        } finally {
            if (process != null) {
                process.destroy();
            }
            deleteFile(tempFile);
        }
    }

    private ST createVideoSizeCmd(Path path) {
        ST template = new ST(videoSizeCmd);
        template.add("video", path.toString());
        template.render();
        return template;
    }

    public void deleteFile(Path tempFile) {
        try {
            if (tempFile != null) {
                Files.delete(tempFile);
            }
        } catch (IOException e) {
            log.error("Can't delete file: {}", e);
        }
    }
}
