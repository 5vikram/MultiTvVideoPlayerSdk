package com.multitv.ott.multitvvideoplayer;

import static android.net.Uri.parse;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.offline.Download;
import com.google.android.exoplayer2.offline.DownloadHelper;
import com.google.android.exoplayer2.offline.DownloadManager;
import com.google.android.exoplayer2.offline.DownloadRequest;
import com.google.android.exoplayer2.offline.DownloadService;
import com.google.android.exoplayer2.source.TrackGroup;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.MappingTrackSelector;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.util.Log;
import com.google.android.exoplayer2.util.Util;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class DownloadVideoHelper {
//    private Context context;
//    private DownloadTracker downloadTracker;
//    private DownloadManager downloadManager;
//    private ProgressDialog pDialog;
//    private List<TrackKey> trackKeys = new ArrayList<>();
//    private DefaultTrackSelector.Parameters qualityParams;
//    private DataSource.Factory dataSourceFactory;
//    private DefaultTrackSelector trackSelector;
//    private DefaultTrackSelector.Parameters trackSelectorParameters;
//    public final HashMap<String, VideoDownloadStateChangeListener> downloadTrackerListeners;
//
//    public DownloadVideoHelper(Context context) {
//        this.context = context;
//        //PlayerApplication application = (PlayerApplication) context.getApplication();
//        VideoPlayerAppController application = VideoPlayerAppController.getInstance();
//        downloadTracker = application.getDownloadTracker();
//        downloadManager = application.getDownloadManager();
//        dataSourceFactory = buildDataSourceFactory();
//        trackSelector = new DefaultTrackSelector(context, new AdaptiveTrackSelection.Factory());
//        updateTrackSelectorParameters();
//        trackSelector.setParameters(trackSelectorParameters);
//        downloadTrackerListeners = new HashMap<>();
//    }
//
//    private void updateTrackSelectorParameters() {
//        if (trackSelector != null) {
//            trackSelectorParameters = trackSelector.getParameters();
//        }
//    }
//
//    public void setDownloadTrackerListener() {
//        if (downloadTracker != null)
//            downloadTracker.addListener(new DownloadTrackerListener());
//    }
//
//    public DownloadTracker getDownloadTracker() {
//        return downloadTracker;
//    }
//
//    /**
//     * Returns a new DataSource factory.
//     */
//    private DataSource.Factory buildDataSourceFactory() {
//        return VideoPlayerAppController.getInstance().buildDataSourceFactory();
//    }
//
//    private class DownloadTrackerListener implements DownloadTracker.Listener {
//        private final Handler handler;
//        private DownloadManager downloadManager;
//        private final Runnable runnable = new Runnable() {
//            @Override
//            public void run() {
//                for (Download download : downloadManager.getCurrentDownloads()) {
//                    VideoDownloadStateChangeListener videoStateChangeListener = downloadTrackerListeners.get(download.request.uri.toString());
//                    if (videoStateChangeListener != null)
//                        videoStateChangeListener.videoStateChangeEvent(download.request.uri, download.getPercentDownloaded(), download.state);
//                }
//                handler.postDelayed(this, 1000);
//                if (downloadManager.getCurrentDownloads().isEmpty()) {
//                    handler.removeCallbacksAndMessages(null);
//                    handler.removeCallbacks(this);
//                }
//            }
//        };
//
//        private DownloadTrackerListener() {
//            handler = new Handler(Looper.getMainLooper());
//        }
//
//        @Override
//        public void onDownloadsChanged(DownloadManager downloadManager, Download download) {
//            Log.e(DownloadVideoHelper.class.getSimpleName(), "onDownloadsChanged s:" + download.state + " p:" + download.getPercentDownloaded() + " u:" + download.request.uri);
//            this.downloadManager = downloadManager;
//            handler.removeCallbacks(runnable);
//            handler.post(runnable);
//
//            if (download.state == Download.STATE_COMPLETED) {
//                VideoDownloadStateChangeListener videoStateChangeListener = downloadTrackerListeners.get(download.request.uri.toString());
//                if (videoStateChangeListener != null)
//                    videoStateChangeListener.videoStateChangeEvent(download.request.uri, download.getPercentDownloaded(), download.state);
//            }
//        }
//    }
//
//    // Add download listener
//    private void addDownloadListener(String videoUrl, VideoDownloadStateChangeListener stateChangeListener) {
//        downloadTrackerListeners.put(videoUrl, stateChangeListener);
//    }
//
//    // Remove download listener
//    public void removeDownloadListener(String videoUrl) {
//        downloadTrackerListeners.remove(videoUrl);
//    }
////
////    // Remove All download listener
////    public void removeAllDownloadListener() {
////
////        if (downloadTrackerListeners == null)
////            return;
////
////        if (downloadTrackerListeners.entrySet() == null)
////            return;
////
////        for (Map.Entry<String, VideoDownloadStateChangeListener> entry : downloadTrackerListeners.entrySet()) {
//////            downloadTracker.removeListener(downloadTrackerListeners.get(entry.getKey()));
////            downloadTrackerListeners.remove(entry.getKey());
////        }
////    }
//
//    public float getDownloadPercentage(String videoUrl) {
//
//        if (!downloadTracker.isDownloaded(Uri.parse(videoUrl)))
//            return -1;
//        else
//            return downloadTracker.downloads.get(Uri.parse(videoUrl)).getPercentDownloaded();
//    }
//
//    // Return download state and progress
//    public int observerVideoStatus(String videoUrl) {
//        if (downloadManager.getCurrentDownloads().size() > 0) {
//            for (int i = 0; i < downloadManager.getCurrentDownloads().size(); i++) {
//                Download currentDownload = downloadManager.getCurrentDownloads().get(i);
//                if (!videoUrl.isEmpty() && currentDownload.request.uri.equals(parse(videoUrl))) {
//
//                    if (downloadTracker.downloads.size() > 0) {
//                        if (currentDownload.request.uri.equals(parse(videoUrl))) {
//
//                            Download downloadFromTracker = downloadTracker.downloads.get(parse(videoUrl));
//
//                            if (downloadFromTracker != null)
//                                return downloadFromTracker.state;
//                            else
//                                return -1;
//                        }
//                    }
//                }
//            }
//        }
//        return -1;
//    }
//
//    private boolean shouldDownload(Format track) {
//        return track.height != 240 && track.sampleMimeType.equalsIgnoreCase("video/avc");
//    }
//
//    // check if video request already done or pending
//    public ExoDownloadState checkVideoStatus(String videoUrl) {
//        if (downloadTracker.downloads.size() > 0) {
//            Download download = downloadTracker.downloads.get(Uri.parse(videoUrl));
//
//            if (download != null) {
//                if (download.getPercentDownloaded() > 99.0) {
//                    return ExoDownloadState.DOWNLOAD_COMPLETED;
//
//                } else {
//                    //Resume Download Not 100 % Downloaded
//                    //So, resume download
//                    return ExoDownloadState.DOWNLOAD_RESUME;
////                    String contentId = download.request.id;
////
////                    DownloadService.sendSetStopReason(
////                            OnlinePlayerActivity.this,
////                            DemoDownloadService.class,
////                            contentId,
////                            Download.STOP_REASON_NONE,
////                            /* foreground= */ false);
//
//                }
//            } else {
//                // New Download
//                return ExoDownloadState.DOWNLOAD_START;
//
////                DownloadRequest myDownloadRequest = downloadRequestt;
////                downloadManager.addDownload(myDownloadRequest);
//
//            }
//
//        } else {
//            return ExoDownloadState.DOWNLOAD_START;
//        }
//    }
//
//
//    // Call for start video download request
//    public void videoDownloadRequest(ExoDownloadState exoDownloadState, String videoName, String videoUrl, Long videoDurationInSeconds, VideoDownloadStateChangeListener videoDownloadStateChangeListener) {
////        PlayerApplication.getInstance().videoModels.add(new VideoModel("" + AppUtil.getRandamChannelId(), videoName, videoUrl, videoDurationInSeconds));
//        if (exoDownloadState == null || videoUrl.isEmpty()) {
//            Toast.makeText(context, "Please, Tap Again", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        addDownloadListener(videoUrl, videoDownloadStateChangeListener);
//        DownloadService.start(context, PlayerDownloadService.class);
//
//        switch (exoDownloadState) {
//            case DOWNLOAD_START:
//                fetchDownloadOptionsDialog(videoUrl, videoName, videoDurationInSeconds);
//                break;
//            case DOWNLOAD_PAUSE:
//                downloadManager.addDownload(downloadTracker.getDownloadRequest(Uri.parse(videoUrl)), Download.STATE_STOPPED);
////                DownloadService.sendSetStopReason(
////                        OnlinePlayerActivity.this,
////                        DemoDownloadService.class,
////                        downloadTracker.getDownloadRequest(Uri.parse(videoUrl)).id,
////                        Download.STATE_STOPPED,
////                        /* foreground= */ false);
//
//                break;
//
//            case DOWNLOAD_RESUME:
//                downloadManager.addDownload(downloadTracker.getDownloadRequest(Uri.parse(videoUrl)), Download.STOP_REASON_NONE);
////                DownloadService.sendSetStopReason(
////                        OnlinePlayerActivity.this,
////                        DemoDownloadService.class,
////                        downloadTracker.getDownloadRequest(Uri.parse(videoUrl)).id,
////                        Download.STOP_REASON_NONE,
////                        /* foreground= */ false);
//                break;
//        }
//    }
//
//    // fetch download resolution dialog
//    private void fetchDownloadOptionsDialog(String videoUrl, String videoName, Long videoDurationInSeconds) {
//        trackKeys.clear();
//        if (pDialog == null || !pDialog.isShowing()) {
//            pDialog = new ProgressDialog(context);
//            pDialog.setTitle(null);
//            pDialog.setCancelable(false);
//            pDialog.setMessage("Preparing Download Options...");
//            pDialog.show();
//        }
//
//        DownloadHelper downloadHelper = DownloadHelper.forHls(context, Uri.parse(videoUrl), dataSourceFactory, new DefaultRenderersFactory(context));
//        downloadHelper.prepare(new DownloadHelper.Callback() {
//            @Override
//            public void onPrepared(@NotNull DownloadHelper helper) {
//                // Preparation completes. Now other DownloadHelper methods can be called.
//                for (int i = 0; i < helper.getPeriodCount(); i++) {
//                    TrackGroupArray trackGroups = helper.getTrackGroups(i);
//                    for (int j = 0; j < trackGroups.length; j++) {
//                        TrackGroup trackGroup = trackGroups.get(j);
//                        for (int k = 0; k < trackGroup.length; k++) {
//                            Format track = trackGroup.getFormat(k);
//                            if (shouldDownload(track)) {
//                                trackKeys.add(new TrackKey(trackGroups, trackGroup, track));
//                            }
//                        }
//                    }
//                }
//
//                if (pDialog != null && pDialog.isShowing())
//                    pDialog.dismiss();
//                showDownloadOptionsDialog(videoName, helper, trackKeys, videoDurationInSeconds);
//            }
//
//            @Override
//            public void onPrepareError(@NotNull DownloadHelper helper, @NotNull IOException e) {
//                //do nothing
//            }
//        });
//    }
//
//    // Resolution dailog
//    private void showDownloadOptionsDialog(String videoName, DownloadHelper helper, List<TrackKey> trackKeyss, Long videoDurationInSeconds) {
//        AlertDialog.Builder builder = new AlertDialog.Builder(context);
//        builder.setTitle("Select Download Format");
//        List<String> optionsToDownload = new ArrayList<>();
//        for (int i = 0; i < trackKeyss.size(); i++) {
//            TrackKey trackKey = trackKeyss.get(i);
//            long bitrate = trackKey.getTrackFormat().bitrate;
//            long getInBytes = (bitrate * videoDurationInSeconds) / 8;
//            String getInMb = AppUtil.formatFileSize(getInBytes);
//            String videoResoultionDashSizeWithHeight = " " + trackKey.getTrackFormat().height + "      (" + getInMb + ")";
//            String videoResoultionDashSizeWithWidth = " " + trackKey.getTrackFormat().width + "      (" + getInMb + ")";
//            optionsToDownload.add(i, videoResoultionDashSizeWithHeight);
//            Log.e("Download sdk::::video size with height", "===== " + videoResoultionDashSizeWithHeight);
//            Log.e("Download sdk::::video size with width", "===== " + videoResoultionDashSizeWithWidth);
//        }
//        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(
//                context,
//                android.R.layout.simple_list_item_single_choice,
//                optionsToDownload
//        );
//
//        TrackKey trackKey = trackKeyss.get(0);
//        qualityParams = ((DefaultTrackSelector) trackSelector).getParameters().buildUpon()
//                .setMaxVideoSize(trackKey.getTrackFormat().width, trackKey.getTrackFormat().height)
//                .setMaxVideoBitrate(trackKey.getTrackFormat().bitrate)
//                .build();
//
//        builder.setSingleChoiceItems(arrayAdapter, 0, new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialogInterface, int i) {
//                TrackKey trackKey = trackKeyss.get(i);
//                qualityParams = ((DefaultTrackSelector) trackSelector).getParameters().buildUpon()
//                        .setMaxVideoSize(trackKey.getTrackFormat().width, trackKey.getTrackFormat().height)
//                        .setMaxVideoBitrate(trackKey.getTrackFormat().bitrate)
//                        .build();
//            }
//        });
//        // Set the alert dialog positive button
//        builder.setPositiveButton("Download", (dialogInterface, which) -> {
//            for (int periodIndex = 0; periodIndex < helper.getPeriodCount(); periodIndex++) {
//                MappingTrackSelector.MappedTrackInfo mappedTrackInfo = helper.getMappedTrackInfo(/* periodIndex= */ periodIndex);
//                helper.clearTrackSelections(periodIndex);
//                for (int i = 0; i < mappedTrackInfo.getRendererCount(); i++) {
//                    helper.addTrackSelection(periodIndex, qualityParams);
//                }
//            }
//            DownloadRequest downloadRequest = helper.getDownloadRequest(Util.getUtf8Bytes(videoName));
//
//            // All tracks were deselected in the dialog. Don't start the download.
//            if (downloadRequest.streamKeys.isEmpty())
//                return;
//
//            startDownload(downloadRequest);
//            dialogInterface.dismiss();
//        });
//
//        AlertDialog dialog = builder.create();
//        dialog.setCancelable(true);
//        dialog.show();
//    }
//
//    // Download request
//    private void startDownload(DownloadRequest downloadRequestt) {
//        if (downloadRequestt.uri.toString().isEmpty()) {
//            Toast.makeText(context, "Please Try Again!!", Toast.LENGTH_SHORT).show();
//        } else {
//            downloadManager.addDownload(downloadRequestt);
//        }
//    }
}
