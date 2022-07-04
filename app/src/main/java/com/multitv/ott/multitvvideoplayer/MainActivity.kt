package com.multitv.ott.multitvvideoplayer

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.widget.FrameLayout
import com.multitv.ott.multitvvideoplayer.R
import com.multitv.ott.multitvvideoplayer.custom.ToastMessage
import com.multitv.ott.multitvvideoplayer.listener.VideoPlayerSdkCallBackListener
import com.multitv.ott.multitvvideoplayer.utils.CommonUtils
import com.multitv.ott.multitvvideoplayer.utils.ContentType
import com.multitv.ott.multitvvideoplayer.utils.ScreenUtils

class MainActivity : AppCompatActivity(), VideoPlayerSdkCallBackListener {
    private var vikramExoVideoPlayer: MultiTvPlayerSdk? = null
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

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()



    }

    private fun startPlayer() {
        if (vikramExoVideoPlayer != null)
            vikramExoVideoPlayer?.releaseVideoPlayer()

        vikramExoVideoPlayer?.setContentType(ContentType.VOD)
        vikramExoVideoPlayer?.setContentFilePath("http://dpvruylq68d5u.cloudfront.net/1048/1048_624c3ec915773/1048_624c3ec915773_master.m3u8")
        /*vikramExoVideoPlayer?.setMultiTvPlayerListner(this)
        vikramExoVideoPlayer?.setMenuClickListener(this)
        */
        vikramExoVideoPlayer?.setMultiTvVideoPlayerSdkListener(this)
        vikramExoVideoPlayer?.prepareVideoPlayer()
    }

    override fun onPause() {
        super.onPause()

        if (!CommonUtils.isAppIsInBackground(this))
            vikramExoVideoPlayer?.resumeVideoPlayer()
        else
            vikramExoVideoPlayer?.pauseVideoPlayer()
    }

    override fun onResume() {
        super.onResume()
        if (!CommonUtils.isAppIsInBackground(this))
            vikramExoVideoPlayer?.resumeVideoPlayer()
    }

    override fun onDestroy() {
        super.onDestroy()
        vikramExoVideoPlayer?.releaseVideoPlayer()
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