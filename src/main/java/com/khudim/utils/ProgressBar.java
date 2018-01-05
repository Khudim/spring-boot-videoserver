package com.khudim.utils;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.concurrent.atomic.AtomicBoolean;

@Data
@NoArgsConstructor
public class ProgressBar {

    private final AtomicBoolean inProcess = new AtomicBoolean(false);

    private int scanLimit = 30;
    private double scanProgress;

    private int totalVideos;
    private double downloadProgress;

    public void riseScanProgress() {
        scanProgress = (int) (++scanProgress / scanLimit * 100);
    }

    public void riseDownloadProgress() {
        if (totalVideos == 0) {
            downloadProgress = 0;
        } else {
            downloadProgress = (int) (++downloadProgress / totalVideos * 100);
        }
    }

    public void reset() {
        setScanProgress(0);
        setDownloadProgress(0);
        setTotalVideos(0);
    }
}
