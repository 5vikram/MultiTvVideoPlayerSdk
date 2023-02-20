package com.multitv.ott.multitvvideoplayer

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.multitv.ott.multitvvideoplayer.listener.VideoPlayPauseCallBackListener
import com.multitv.ott.multitvvideoplayer.listener.VideoPlayerSdkCallBackListener
import com.multitv.ott.multitvvideoplayer.utils.ContentType

class MainActivity : AppCompatActivity(), VideoPlayerSdkCallBackListener,
    VideoPlayPauseCallBackListener {
    private lateinit var styledPlayerView: AltbalajiTvVideoPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //styledPlayerView?.releaseVideoPlayer()
        playVideo()
    }

    private fun playVideo() {
        styledPlayerView = findViewById(R.id.styledPlayerView)
        styledPlayerView.setContentType(ContentType.VOD)
        styledPlayerView.setContentId("123456")
        styledPlayerView.setKeyToken("VIkr453fdgs")
        styledPlayerView.setContentFilePath("http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4")
        styledPlayerView.setMultiTvVideoPlayerPausePlaySdkListener(this)
        styledPlayerView.setMultiTvVideoPlayerSdkListener(this)
        styledPlayerView.showEpisodeButton(false)
        styledPlayerView.showResumedVideoHint(false)
        styledPlayerView.prepareVideoPlayer()
    }

    override fun videoStart() {

    }

    override fun videoStop() {

    }

    override fun onPlayerReady(contentUrl: String?) {
        styledPlayerView.startVideoPlayer(true)
    }

    override fun onPlayNextVideo() {

    }

    override fun onPlayClick() {

    }

    override fun onPlayClick(status: Int) {

    }

    override fun onControllerVisibilityChanges(isShown: Int) {

    }

    override fun onPlayerError(message: String?) {

    }

    override fun prepareVideoPlayer() {

    }

    override fun subscriptionCallBack() {

    }

    override fun showThumbnailCallback() {

    }

    override fun showEpisodeListData() {

    }

    override fun fullScreenCallBack() {

    }

    override fun onVideoStartNow() {

    }

    override fun onAdPlay() {

    }

    override fun onAdCompleted() {

    }

    override fun onBufferStart() {

    }

    override fun onBUfferStop(bufferingTimeInMillis: Long) {

    }
}