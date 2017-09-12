package com.khudim.utils;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ProgressBar {

    private int scanLimit = 30;
    private int scanProgress;

    private int totalVideos;
    private int downloadProgress;

    public void riseScanProgress() {
        scanProgress = ++scanProgress / scanLimit * 100;
    }

    public void riseDownloadProgress() {
        if (totalVideos == 0) {
            scanProgress = 0;
        } else {
            scanProgress = (++downloadProgress / totalVideos * 100);
        }
    }
}
