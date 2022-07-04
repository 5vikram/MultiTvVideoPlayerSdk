package com.multitv.ott.multitvvideoplayer.utils

import android.net.Uri

object ConstantPlayer {
    const val HLS_URL = 1
    const val MP4_URL = 2
    const val DASH_URL = 3

    fun uriParser(url: String): Uri {
        return Uri.parse(url)
    }
}