package com.multitv.ott.multitvvideoplayer.download;

public interface DownloadVideoListener {
    void Downloading();
    void pauseDownload();
    void downloadCompleted();
    void removeDownload();
    void downloadFail();
    void startDownloadInit();
}
