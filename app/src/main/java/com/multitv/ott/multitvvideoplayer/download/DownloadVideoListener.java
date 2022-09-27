package com.multitv.ott.multitvvideoplayer.download;

import android.net.Uri;

import com.google.android.exoplayer2.offline.Download;

public interface DownloadVideoListener {
    void Downloading(Uri videouri);

    void pauseDownload();

    void downloadCompleted(Uri videouri);

    void removeDownload();

    void downloadFail();

    void startDownloadInit();
}
