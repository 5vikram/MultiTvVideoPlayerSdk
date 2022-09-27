package com.multitv.ott.multitvvideoplayer;

import android.net.Uri;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.MediaMetadata;
import com.google.android.exoplayer2.offline.Download;
import com.google.android.exoplayer2.util.MimeTypes;
import com.multitv.ott.multitvvideoplayer.download.DownloadVideo;
import com.multitv.ott.multitvvideoplayer.download.DownloadVideoListener;

import io.github.yoobi.downloadvideo.common.DownloadTracker;
import io.github.yoobi.downloadvideo.common.DownloadUtil;
import io.github.yoobi.downloadvideo.common.MediaItemTag;

public class ExoVideoDownloadHelper implements DownloadTracker.Listener {
    private AppCompatActivity context;
    private DownloadVideoListener downloadVideoListener;
    private MediaItem mediaItem;

    public ExoVideoDownloadHelper(AppCompatActivity context) {
        this.context = context;
        DownloadUtil.INSTANCE.getDownloadTracker(context).addListener(this);
    }

    public void setDownloadVideoListener(DownloadVideoListener downloadVideoListener) {
        this.downloadVideoListener = downloadVideoListener;
    }

    private MediaItem getMediaItem(String uri, String title) {
        return new MediaItem.Builder()
                .setUri(uri)
                .setMimeType(MimeTypes.APPLICATION_M3U8)
                .setMediaMetadata(new MediaMetadata.Builder().setTitle(title).build())
                .setTag(new MediaItemTag(-1, title))
                .build();


    }

    public void downloadVideo(String url, String videoTitle, Long videoDurationInSeconds, ImageView imageView) {
        mediaItem = getMediaItem(url, videoTitle);
        if (DownloadUtil.INSTANCE.getDownloadTracker(context).isDownloaded(getMediaItem(url, videoTitle))) {
            Toast.makeText(context, "Video already downloaded.", Toast.LENGTH_SHORT).show();
        } else {
            new DownloadVideo(context).downloadVideo(mediaItem, imageView, videoDurationInSeconds);
        }
    }


    public void removeDownload() {
        DownloadUtil.INSTANCE.getDownloadTracker(context).removeListener(this);
    }

    @Override
    public void onDownloadsChanged(@NonNull Download download) {
        switch (download.state) {
            case Download.STATE_DOWNLOADING:
                downloadVideoListener.Downloading(download.request.uri);
                Log.e("Download Uri:::", "" + download.getPercentDownloaded());
                break;
            case Download.STATE_QUEUED:
                downloadVideoListener.pauseDownload();
                break;

            case Download.STATE_STOPPED:
                downloadVideoListener.pauseDownload();
                break;

            case Download.STATE_COMPLETED:
                downloadVideoListener.downloadCompleted(download.request.uri);
                break;

            case Download.STATE_REMOVING:
                downloadVideoListener.startDownloadInit();
                break;

            case Download.STATE_FAILED:
                downloadVideoListener.downloadFail();
                break;
        }
    }
}
