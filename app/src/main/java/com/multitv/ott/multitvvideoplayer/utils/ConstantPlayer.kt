package com.multitv.ott.multitvvideoplayer.utils

import android.net.Uri

object ConstantPlayer {
    const val HLS_URL = 1
    const val MP4_URL = 2
    const val DASH_URL = 3

    fun uriParser(url: String): Uri {
        return Uri.parse(url)
    }



    const val SITE_ID = "JKJG"
    const val SITE_KEY = "GMdUs9dWqTIhju3mWeTsS9MRiX9WZ0hh"
    const val DRM_TOKEN = "eyJkcm1fdHlwZSI6IldpZGV2aW5lIiwic2l0ZV9pZCI6IkpLSkciLCJ1c2VyX2lkIjoiYWRtaW5AdGVjaG1qYXBhbi5jb20iLCJjaWQiOiJjb250ZW50XzEwMDQiLCJwb2xpY3kiOiIyamlUNjFpbFNnRVp4Z01zcng4RWZSVVh4TFFUTHdyWEVtUld5bGNzNmZPbHhQNkZcLzk2VWpXR3g1ZmVkU0NEWCs0NHVMeUxZUCtRZjNCYkVpaWhpWFJSNkZxc0FRaUs0Z3dJNkhYZzZ5dlpVdFk5SG1PNjRPTmNpWUQ4ZG9Sbm8iLCJ0aW1lc3RhbXAiOiIyMDIyLTA3LTA1VDA5OjQ0OjU5WiIsInJlc3BvbnNlX2Zvcm1hdCI6Im9yaWdpbmFsIiwiaGFzaCI6ImVDS0tGWkpoNHYxeHBvdHU1MTliUnpVZkJJcFJIUXMwcW5iekRXM2szTU09Iiwia2V5X3JvdGF0aW9uIjpmYWxzZX0="


}