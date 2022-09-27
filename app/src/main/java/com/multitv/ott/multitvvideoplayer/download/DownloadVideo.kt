package com.multitv.ott.multitvvideoplayer.download

import android.content.Context
import android.widget.ImageView
import com.google.android.exoplayer2.MediaItem

class DownloadVideo(private val context: Context) {

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
                .toggleDownloadDialogHelper(context, item)
            return DownloadUtil.getDownloadTracker(context).getTrackDailogStatus()
        } else {
            DownloadUtil.getDownloadTracker(context)
                .toggleDownloadPopupMenu(context, progressDrawable, item.playbackProperties?.uri)

            return DownloadUtil.getDownloadTracker(context).getTrackDailogStatus()
        }
    }


}