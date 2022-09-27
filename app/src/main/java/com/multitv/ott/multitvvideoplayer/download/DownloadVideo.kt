package com.multitv.ott.multitvvideoplayer.download

import android.content.Context
import android.media.Image
import android.net.Uri
import android.widget.ImageView
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.MediaMetadata
import io.github.yoobi.downloadvideo.common.DownloadUtil
import io.github.yoobi.downloadvideo.common.MediaItemTag
import java.time.Duration

class DownloadVideo(private val context: Context) {

    fun downloadVideo(mediaItem: MediaItem, progressDrawable: ImageView, duration: Long) {

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
        } else {
            DownloadUtil.getDownloadTracker(context)
                .toggleDownloadPopupMenu(context, progressDrawable, item.playbackProperties?.uri)
        }
    }




}