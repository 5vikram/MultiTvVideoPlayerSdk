package com.multitv.ott.multitvvideoplayer.download

import android.content.Context
import android.widget.ImageView
import com.google.android.exoplayer2.MediaItem

class DownloadVideo(
    private val context: Context,
    private val downloadVideoListener: SdkPopCallbackListner
) : DailogCallbackListener {
    private var dailogShown = false;

    fun downloadVideo(mediaItem: MediaItem, progressDrawable: ImageView, duration: Long): Boolean {

        val item = mediaItem.buildUpon()
            .setTag(
                (mediaItem.playbackProperties?.tag as MediaItemTag)
                    .copy(duration = duration)
            )
            .build()
        if (!DownloadUtil.getDownloadTracker(context)
                .hasDownload(item.playbackProperties?.uri)
        ) {
            DownloadUtil.getDownloadTracker(context)
                .toggleDownloadDialogHelper(context, item, this)
            return DownloadUtil.getDownloadTracker(context).getTrackDailogStatus()
        } else {
            DownloadUtil.getDownloadTracker(context)
                .toggleDownloadPopupMenu(context, progressDrawable, item.playbackProperties?.uri)

            return DownloadUtil.getDownloadTracker(context).getTrackDailogStatus()
        }
    }

    override fun trackDailogStatus(shown: Boolean) {
        dailogShown = shown;
        downloadVideoListener.dismissEvent()
    }


}