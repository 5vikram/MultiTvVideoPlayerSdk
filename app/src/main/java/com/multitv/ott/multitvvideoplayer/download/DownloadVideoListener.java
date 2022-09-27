package com.multitv.ott.multitvvideoplayer.download;

import android.net.Uri;

import com.google.android.exoplayer2.offline.Download;

public interface DownloadVideoListener {
    void Downloading(Download download);

    void pauseDownload(Download download);

    void downloadCompleted(Download download,Uri videouri);

    void removeDownload(Download download);

    void downloadFail(Download download);

    void startDownloadInit(Download download);
}
