package com.multitv.ott.multitvvideoplayer.download;

import android.net.Uri;

import com.google.android.exoplayer2.offline.Download;

public interface DownloadVideoListener {
    void Downloading(float videouri);
    void downloadCompleted(Uri videouri);
    void removeDownload();
    void downloadFail();
    void startDownloadInit();
    void pauseDownload();
    void dailogDismissEvent(boolean status);
}
