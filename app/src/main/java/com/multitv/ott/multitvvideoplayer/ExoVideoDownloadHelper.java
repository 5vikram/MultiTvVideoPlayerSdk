package com.multitv.ott.multitvvideoplayer;

import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.MediaMetadata;
import com.google.android.exoplayer2.offline.Download;
import com.google.android.exoplayer2.util.MimeTypes;
import com.multitv.ott.multitvvideoplayer.download.DownloadTracker;
import com.multitv.ott.multitvvideoplayer.download.DownloadUtil;
import com.multitv.ott.multitvvideoplayer.download.DownloadVideo;
import com.multitv.ott.multitvvideoplayer.download.DownloadVideoListener;
import com.multitv.ott.multitvvideoplayer.download.MediaItemTag;
import com.multitv.ott.multitvvideoplayer.download.SdkPopCallbackListner;
import com.multitv.ott.multitvvideoplayer.fabbutton.FabButton;
import com.multitv.ott.multitvvideoplayer.listener.DownloadsDetailsListener;


public class ExoVideoDownloadHelper implements DownloadTracker.Listener, SdkPopCallbackListner {
    private AppCompatActivity context;
    private DownloadVideoListener downloadVideoListener;
    private DownloadsDetailsListener downloadsDetailsListener;
    private MediaItem mediaItem;
    private Handler handler = new Handler();

    public ExoVideoDownloadHelper(AppCompatActivity context) {
        this.context = context;
        DownloadUtil.INSTANCE.getDownloadTracker(context).addListener(this);
    }

    public void setDownloadVideoListener(DownloadVideoListener downloadVideoListener) {
        this.downloadVideoListener = downloadVideoListener;
    }

    public void setDownloadDetailsCallBack(DownloadsDetailsListener downloadVideoListener) {
        this.downloadsDetailsListener = downloadVideoListener;
    }

    private MediaItem getMediaItem(String uri, String title) {
        return new MediaItem.Builder()
                .setUri(uri)
                .setMimeType(MimeTypes.APPLICATION_M3U8)
                .setMediaMetadata(new MediaMetadata.Builder().setTitle(title).build())
                .setTag(new MediaItemTag(-1, title))
                .build();


    }

    public void downloadVideo(String url, String videoTitle, Long videoDurationInSeconds) {
        mediaItem = getMediaItem(url, videoTitle);
        if (DownloadUtil.INSTANCE.getDownloadTracker(context).isDownloaded(getMediaItem(url, videoTitle))) {
            new DownloadVideo(context, this).removeVideoFromDownload(mediaItem.playbackProperties.uri);
            new DownloadVideo(context, this).downloadVideo(mediaItem, videoDurationInSeconds);
        } else {
            new DownloadVideo(context, this).downloadVideo(mediaItem, videoDurationInSeconds);
        }
    }


    public void downloadPopUpMenuOption(View view, String uri) {
        DownloadUtil.INSTANCE.getDownloadTracker(context)
                .toggleDownloadPopupMenu(context, view, Uri.parse(uri),downloadsDetailsListener);
    }


    public void downloadVideo(String url, String videoTitle, Long videoDurationInSeconds, ImageView imageView) {
        mediaItem = getMediaItem(url, videoTitle);
        if (DownloadUtil.INSTANCE.getDownloadTracker(context).isDownloaded(getMediaItem(url, videoTitle))) {
            new DownloadVideo(context, this).removeVideoFromDownload(mediaItem.playbackProperties.uri);
            new DownloadVideo(context, this).downloadVideo(mediaItem, imageView, videoDurationInSeconds);
        } else {
            new DownloadVideo(context, this).downloadVideo(mediaItem, imageView, videoDurationInSeconds);
        }
    }
/* public void removeDownload() {
        DownloadUtil.INSTANCE.getDownloadTracker(context).removeListener(this);
    }*/


    public boolean isVideoDownload(String videoUrl, String videoTitle) {
        return DownloadUtil.INSTANCE.getDownloadTracker(context).isDownloaded(getMediaItem(videoUrl, videoTitle));
    }

    public void removeDownload() {
        DownloadUtil.INSTANCE.getDownloadTracker(context).removeListener(this);
    }

    public void removeVideoFromStorage(Uri uri) {
        new DownloadVideo(context, this).removeVideoFromDownload(uri);
    }

    private Runnable r = null;


    @Override
    public void onDownloadsChanged(@NonNull Download download) {
        switch (download.state) {
            case Download.STATE_DOWNLOADING:

                r = new Runnable() {
                    public void run() {

                        if (download.getPercentDownloaded() < 99) {
                            if (downloadVideoListener != null)
                                downloadVideoListener.Downloading(download.getPercentDownloaded());
                            handler.postDelayed(this, 1000);
                        } else {
                            if (handler != null && handler != null) {
                                handler.removeCallbacks(r);
                                handler.removeCallbacksAndMessages(null);
                            }
                        }


                    }
                };

                handler.postDelayed(r, 1000);

                break;
            case Download.STATE_QUEUED:
                if (downloadVideoListener != null)
                    downloadVideoListener.pauseDownload();
                break;

            case Download.STATE_STOPPED:
                if (downloadVideoListener != null)
                    downloadVideoListener.pauseDownload();
                break;


            /* download*/

            case Download.STATE_COMPLETED:
                if (downloadVideoListener != null)
                    downloadVideoListener.downloadCompleted(download.request.uri);
                break;

            case Download.STATE_REMOVING:
                if (downloadVideoListener != null)
                    downloadVideoListener.startDownloadInit();
                break;

            case Download.STATE_FAILED:
                if (downloadVideoListener != null)
                    downloadVideoListener.downloadFail();
                break;
        }
    }

    @Override
    public void dismissEvent() {
        if (downloadVideoListener != null)
            downloadVideoListener.dailogDismissEvent(true);
    }
}
