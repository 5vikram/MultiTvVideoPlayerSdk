package com.multitv.ott.multitvvideoplayer.utils

import android.content.Context
import android.net.Uri
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.drm.DrmSessionManager
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.dash.DashMediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.google.android.exoplayer2.util.Util


class ExoUttils {
/*
    fun buildMediaSource(
        type: Int, mediaItem: MediaItem
    ): MediaSource? {
        val dataSourceFactory = DefaultHttpDataSource.Factory()

        when (type) {
            1 -> {
                return HlsMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(mediaItem)
            }
            2 -> {
                return buildMp4MediaSource(mediaItem)
            }
            3 -> {

            }
            else -> {
                throw  IllegalStateException("Unsupported type: " + type);
            }
        }
        return null
    }
*/


    // creating a ConcatenatingMediaSource
/*
    fun buildMp4MediaSource(mediaItem: MediaItem): MediaSource {

        val dataSourceFactory = DefaultHttpDataSource.Factory()

        val mediaSource1 = ProgressiveMediaSource.Factory(dataSourceFactory)

            .createMediaSource(mediaItem)

        return mediaSource1
    }
*/


    fun buildMediaSource(
        context: Context,
        mediaItem: MediaItem,
        uri: String,
        drmSessionManager: DrmSessionManager
    ): MediaSource? {

        var uriData = Uri.parse(uri)
        val type: Int = Util.inferContentType(uriData.lastPathSegment.toString())
        val dataSourceFactory: DataSource.Factory = DefaultDataSource.Factory(context)
        val dataSourceFactory1 = DefaultHttpDataSource.Factory()
        return when (type) {
            C.TYPE_HLS -> HlsMediaSource.Factory(dataSourceFactory1).setAllowChunklessPreparation(true)
                .createMediaSource(mediaItem)
            C.TYPE_DASH -> DashMediaSource.Factory(dataSourceFactory)
                .setDrmSessionManagerProvider { unusedMediaItem: MediaItem? -> drmSessionManager }
                .createMediaSource(MediaItem.fromUri(uri))
            C.TYPE_OTHER -> ProgressiveMediaSource.Factory(dataSourceFactory1)
                .createMediaSource(MediaItem.fromUri(uri))
            else -> throw IllegalStateException("Unsupported type: $type")
        }
    }


/*
    private fun buildDashMediaSource(url: String): MediaSource {

        // using DefaultHttpDataSource for http data source
        val dataSourceFactory = DefaultHttpDataSource.Factory()

        // Set a custom authentication request header
        // dataSourceFactory.setDefaultRequestProperty("Header", "Value")

        // just-in-time behavior using a ResolvingDataSource

        // create DASH media source
        return DashMediaSource.Factory(dataSourceFactory)
            .createMediaSource(MediaItem.fromUri(url))
    }
*/
}