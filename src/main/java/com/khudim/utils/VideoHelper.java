package com.khudim.utils;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;


/**
 * Created by Beaver.
 */
public class VideoHelper {

    private final static int MB = 1024 * 1024;

    public static byte[] getRangeBytesFromVideo(String filePath, String range, HttpServletResponse response) throws IOException {
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

    private static String[] parseRanges(String range, long fileLength) {

        String[] ranges = range.split("=")[1].split("-");

        if (ranges.length < 2) {
            String start = ranges[0];
            String stop = String.valueOf(fileLength - 1);
            ranges = new String[]{start, stop};
        }

        long start = Long.parseLong(ranges[0]);
        long stop = Long.parseLong(ranges[1]);

        if (stop - start > MB) {
            stop = start + MB;
            if (stop > fileLength) {
                stop = fileLength - 1;
            }
            ranges[1] = String.valueOf(stop);
        }

        return ranges;
    }
}
