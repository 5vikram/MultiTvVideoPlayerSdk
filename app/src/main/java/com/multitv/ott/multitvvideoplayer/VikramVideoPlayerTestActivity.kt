package com.multitv.ott.multitvvideoplayer

import android.content.res.Configuration
import android.os.Bundle
import android.view.Gravity
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import com.multitv.ott.multitvvideoplayer.custom.ToastMessage
import com.multitv.ott.multitvvideoplayer.listener.VideoPlayerSdkCallBackListener
import com.multitv.ott.multitvvideoplayer.utils.CommonUtils
import com.multitv.ott.multitvvideoplayer.utils.ContentType
import com.multitv.ott.multitvvideoplayer.utils.ScreenUtils

class VikramVideoPlayerTestActivity : AppCompatActivity(), VideoPlayerSdkCallBackListener {
    private var vikramExoVideoPlayer: ShortVideoPlayer? = null
    private var token =
        "eyJkcm1fdHlwZSI6IldpZGV2aW5lIiwic2l0ZV9pZCI6IkpLSkciLCJ1c2VyX2lkIjoiYWRtaW5AdGVjaG1qYXBhbi5jb20iLCJjaWQiOiJjb250ZW50XzEwMDQiLCJwb2xpY3kiOiIyamlUNjFpbFNnRVp4Z01zcng4RWZSVVh4TFFUTHdyWEVtUld5bGNzNmZPbHhQNkZcLzk2VWpXR3g1ZmVkU0NEWCs0NHVMeUxZUCtRZjNCYkVpaWhpWFJSNkZxc0FRaUs0Z3dJNkhYZzZ5dlpVdFk5SG1PNjRPTmNpWUQ4ZG9Sbm8iLCJ0aW1lc3RhbXAiOiIyMDIyLTA3LTA1VDA5OjQ0OjU5WiIsInJlc3BvbnNlX2Zvcm1hdCI6Im9yaWdpbmFsIiwiaGFzaCI6ImVDS0tGWkpoNHYxeHBvdHU1MTliUnpVZkJJcFJIUXMwcW5iekRXM2szTU09Iiwia2V5X3JvdGF0aW9uIjpmYWxzZX0="

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        vikramExoVideoPlayer = findViewById(R.id.vikramExoVideoPlayer)
        val width = ScreenUtils.getScreenWidth(this)
        val height = width * 9 / 16 + 50
        vikramExoVideoPlayer?.setLayoutParams(
            FrameLayout.LayoutParams(
                width,
                height,
                Gravity.CENTER
            )
        )
        startPlayer()

    }

    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: Configuration?
    ) {
        if (isInPictureInPictureMode) {
            supportActionBar?.hide()
        } else {
            supportActionBar?.show()
        }
    }

    private fun startPlayer() {
        if (vikramExoVideoPlayer != null)
            vikramExoVideoPlayer?.releaseVideoPlayer()

        vikramExoVideoPlayer?.setContentType(ContentType.VOD)
        /*  vikramExoVideoPlayer?.setDrmEnabled(
              true, "JKJG",
              "GMdUs9dWqTIhju3mWeTsS9MRiX9WZ0hh", token,
              "https://license.pallycon.com/ri/licenseManager.do"
          )*/
        // vikramExoVideoPlayer?.setContentFilePath("http://d34i7be7c53ukk.cloudfront.net/1004/1004_62ac86b418c6a/dash/stream.mpd")
        vikramExoVideoPlayer?.setContentFilePath("http://dpvruylq68d5u.cloudfront.net/1048/1048_624c3ec915773/1048_624c3ec915773_master.m3u8")
        // vikramExoVideoPlayer?.setSubtitleVideoUri("https://multitvott.s3.ap-south-1.amazonaws.com/subtitle.srt")
        // vikramExoVideoPlayer?.setPreRollAdUrl("https://pubads.g.doubleclick.net/gampad/ads?iu=/21775744923/external/single_ad_samples&sz=640x480&cust_params=sample_ct%3Dlinear&ciu_szs=300x250%2C728x90&gdfp_req=1&output=vast&unviewed_position_start=1&env=vp&impl=s&correlator=")
        vikramExoVideoPlayer?.setMultiTvVideoPlayerSdkListener(this)
        vikramExoVideoPlayer?.prepareVideoPlayer()
    }

    override fun onPause() {
        super.onPause()

        /*   if (!CommonUtils.isAppIsInBackground(this))
               vikramExoVideoPlayer?.resumeVideoPlayer()
           else
               vikramExoVideoPlayer?.pauseVideoPlayer()*/

        vikramExoVideoPlayer?.pauseVideoPlayer()
        vikramExoVideoPlayer?.hideSpeedDailog()
    }

    override fun onResume() {
        super.onResume()
        if (!CommonUtils.isAppIsInBackground(this))
            vikramExoVideoPlayer?.resumeVideoPlayer()
    }

    override fun onDestroy() {
        super.onDestroy()
        vikramExoVideoPlayer?.releaseVideoPlayer()
        vikramExoVideoPlayer?.hideSpeedDailog()
    }

    override fun onPlayerReady(contentUrl: String?) {
        ToastMessage.showLogs(
            ToastMessage.LogType.ERROR,
            "Video Player Ready::::::::::",
            contentUrl
        )
        vikramExoVideoPlayer?.startVideoPlayer(true)
    }

    override fun onPlayNextVideo() {

    }

    override fun onPlayClick() {

    }

    override fun onControllerVisibilityChanges(isShown: Int) {

    }

    override fun onPlayerError(message: String?) {
        ToastMessage.showLogs(ToastMessage.LogType.ERROR, "Video Player Error::::", message)
    }

    override fun prepareVideoPlayer() {
        ToastMessage.showLogs(
            ToastMessage.LogType.ERROR,
            "Video Player State:::",
            " Prepare video player"
        )
    }
}