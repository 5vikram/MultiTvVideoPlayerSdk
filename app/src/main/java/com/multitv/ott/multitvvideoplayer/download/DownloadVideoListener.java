package com.multitv.ott.multitvvideoplayer.download;

import android.net.Uri;

public interface DownloadVideoListener {
    void Downloading();
    void pauseDownload();
    void downloadCompleted(Uri videouri);
    void removeDownload();
    void downloadFail();
    void startDownloadInit();
}
