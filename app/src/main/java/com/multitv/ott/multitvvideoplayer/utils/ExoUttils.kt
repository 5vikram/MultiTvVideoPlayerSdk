package com.multitv.ott.multitvvideoplayer.utils

import android.content.Context
import android.net.Uri
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource

class ExoUttils {
    fun buildMediaSource(
        type: Int,
        context: Context?, uri: String
    ): MediaSource? {
        val dataSourceFactory = DefaultHttpDataSource.Factory()

        when (type) {
            1 -> {
                return  HlsMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(MediaItem.fromUri(ConstantPlayer.uriParser(uri)))
            }
            2 -> {
                return buildMp4MediaSource(uri)
            }
            3 -> {

            }
            else -> {
                throw  IllegalStateException("Unsupported type: " + type);
            }
        }
        return null
    }


    // creating a ConcatenatingMediaSource
    fun buildMp4MediaSource(url1: String): MediaSource {

        val dataSourceFactory = DefaultHttpDataSource.Factory()

        val mediaSource1 = ProgressiveMediaSource.Factory(dataSourceFactory)

            .createMediaSource(MediaItem.fromUri(url1))

        return mediaSource1
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