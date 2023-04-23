package com.multitv.ott.multitvvideoplayer.videoplayer

import android.Manifest
import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.drawable.Icon
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.SystemClock
import android.support.v4.media.session.MediaSessionCompat
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.text.TextUtils
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.util.Log
import android.util.Rational
import android.view.*
import android.widget.*
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.Target
import com.conviva.sdk.ConvivaAdAnalytics
import com.conviva.sdk.ConvivaAnalytics
import com.conviva.sdk.ConvivaSdkConstants
import com.conviva.sdk.ConvivaVideoAnalytics
import com.github.rubensousa.previewseekbar.PreviewBar
import com.github.rubensousa.previewseekbar.PreviewLoader
import com.github.rubensousa.previewseekbar.exoplayer.PreviewTimeBar
import com.google.ads.interactivemedia.v3.api.Ad
import com.google.ads.interactivemedia.v3.api.AdEvent
import com.google.ads.interactivemedia.v3.api.AdsManagerLoadedEvent
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.drm.DrmSessionManager
import com.google.android.exoplayer2.ext.ima.ImaAdsLoader
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.offline.DownloadHelper
import com.google.android.exoplayer2.offline.DownloadRequest
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.source.MediaSourceFactory
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.DefaultTimeBar
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultAllocator
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.google.android.exoplayer2.util.MimeTypes
import com.google.android.exoplayer2.util.Util
import com.google.android.exoplayer2.video.VideoSize
import com.google.common.collect.ImmutableList
import com.multitv.ott.multitvvideoplayer.R
import com.multitv.ott.multitvvideoplayer.cast.SessionAvailabilityListener
import com.multitv.ott.multitvvideoplayer.custom.CountDownTimerWithPause
import com.multitv.ott.multitvvideoplayer.database.SharedPreferencePlayer
import com.multitv.ott.multitvvideoplayer.fabbutton.FabButton
import com.multitv.ott.multitvvideoplayer.listener.VideoPlayPauseCallBackListener
import com.multitv.ott.multitvvideoplayer.listener.VideoPlayerSdkCallBackListener
import com.multitv.ott.multitvvideoplayer.models.SkipDuration
import com.multitv.ott.multitvvideoplayer.playerglide.GlideThumbnailTransformation
import com.multitv.ott.multitvvideoplayer.popup.TrackSelectionDialog
import com.multitv.ott.multitvvideoplayer.utils.*
import com.pallycon.widevinelibrary.*
import java.util.*

class AltAndroidTvVideoPlayer(
    private val context: AppCompatActivity, attrs: AttributeSet?, defStyleAttr: Int
) : FrameLayout(
    context, attrs, defStyleAttr
), PreviewBar.OnScrubListener, PreviewLoader, View.OnClickListener, SessionAvailabilityListener {
    private val sharedPreferencePlayer: SharedPreferencePlayer
    private var contentType: ContentType? = null
    private var mMediaPlayer: ExoPlayer? = null
    var simpleExoPlayerView: StyledPlayerView? = null
    private var trackSelector: DefaultTrackSelector

    private var videoPlayPauseCallBackListener: VideoPlayPauseCallBackListener? = null
    private var videoPlayerSdkCallBackListener: VideoPlayerSdkCallBackListener? = null


    private var isShowingTrackSelectionDialog = false
    private var WVMAgent: PallyconWVMSDK? = null
    private var analaticsUrl: String? = null
    private var token: String? = null
    private var userId: String? = null
    private var contentTitle: String? = null
    private var contentId: String? = null
    private var millisecondsForResume: Long = 0
    private val adPlayedTimeInMillis: Long = 0
    private var contentPlayedTimeInMillis: Long = 0


    private var mInitialTextureWidth: Int = 0
    private var mInitialTextureHeight: Int = 0

    // get buffer duration of video in milli second
    var bufferingTimeInMillis: Long = 0
    private var seekPlayerTo = 0
    private var mContentUrl: String? = null
    private var subTitleUri: String? = null
    private var bufferingTimeHandler: Handler? = null
    private var countDownTimer: CountDownTimerWithPause? = null
    private val TAG = "VikramExoVideoPlayer"

    private var videoAnalytics: ConvivaVideoAnalytics? = null
    private var adAnalytics: ConvivaAdAnalytics? = null


    private lateinit var errorRetryLayout: LinearLayout
    private lateinit var videoMenuLayout: RelativeLayout
    private lateinit var resumedVideoTv: TextView

    private lateinit var volumeLinearLayout: LinearLayout
    private lateinit var bufferingProgressBarLayout: LinearLayout
    private lateinit var circularProgressLayout: LinearLayout
    private lateinit var repeatVideoLinearLayout: LinearLayout

    private lateinit var circularProgressRing: FabButton
    private lateinit var centerButtonLayout: LinearLayout
    private lateinit var previewImageView: ImageView
    private lateinit var volumeMuteAndUnMuteButton: ImageView
    private lateinit var volumeUnMuteButton: ImageView
    private lateinit var closeVideoPlayerButton: ImageView
    private lateinit var settingButton: ImageView

    private lateinit var videoFarwardButton: ImageView
    private lateinit var videoRenuButton: ImageView

    private lateinit var videoPlayButton: ImageView
    private lateinit var videoPauseButton: ImageView
    private lateinit var previewTimeBar: PreviewTimeBar
    private lateinit var previewFrameLayout: FrameLayout
    private lateinit var videoTitle: TextView
    private lateinit var epsodeButton: ImageView
    private lateinit var exoTotalDuration: TextView
    private lateinit var exoCurrentPosition: TextView
    private lateinit var skipVideoButton: TextView
    private lateinit var durationLinearLayout: LinearLayoutCompat


    private lateinit var exoRewLinearLayout: LinearLayout
    private lateinit var exoFfwdLinearLayout: LinearLayout

    private var videoControllerLayout: ConstraintLayout? = null


    private val formatBuilder: StringBuilder
    private val formatter: Formatter
    private var isDrmContent = false
    private var isOfflineContent = false
    private var drmContentToken: String? = null
    private var drmdrmLicenseUrl: String? = null
    private var siteId: String? = null
    private var siteKey: String? = null
    private var drmSessionManager: DrmSessionManager? = null
    private var adsLoader: ImaAdsLoader? = null
    private var adsUrl: String? = null
    private var isControllerShown = false
    private var isAttachedToWindowStatus = false
    private var hideAtMs: Long = 0

    /** Intent action for media controls from Picture-in-Picture mode.  */
    private val ACTION_MEDIA_CONTROL = "media_control"

    /** Intent extra for media controls from Picture-in-Picture mode.  */
    private val EXTRA_CONTROL_TYPE = "control_type"


    private var mWindow: Window? = null

    private var isWebSeries = false;
    private var userSubscriptionDtatus = false
    private var contentAccessType = ""
    private var skipDurationArray = ArrayList<SkipDuration>()


    private var spriteImageUrl = ""


    private var isPipModeOn = false


    constructor(context: Context, attrs: AttributeSet?) : this(
        context as AppCompatActivity, attrs, 0
    ) {
    }


    /*
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }*/
    @SuppressLint("MissingInflatedId")
    override fun onFinishInflate() {
        val view =
            LayoutInflater.from(getContext()).inflate(R.layout.android_tv_alt_video_player, this)

        epsodeButton = view.findViewById(R.id.epsodeButton)

        durationLinearLayout = view.findViewById(R.id.durationLinearLayout)
        epsodeButton.setOnClickListener {
            videoPlayerSdkCallBackListener?.showEpisodeListData()
        }


        videoFarwardButton = view.findViewById(R.id.exo_ffwd)
        videoRenuButton = view.findViewById(R.id.exo_rew)

        exoTotalDuration = view.findViewById(R.id.exo_duration)
        exoCurrentPosition = view.findViewById(R.id.exo_position)
        skipVideoButton = view.findViewById(R.id.skipVideoButton)

        contentRateLayout = view.findViewById(R.id.contentRateLayout)
        contentRatedTv = view.findViewById(R.id.contentRatedTv)
        languageTv = view.findViewById(R.id.languageTv)
        genureTv = view.findViewById(R.id.genureTv)
        videoTitle = view.findViewById(R.id.videoTitle)

        volumeMuteAndUnMuteButton = view.findViewById(R.id.volumeMuteAndUnMuteButton)


        resumedVideoTv = view.findViewById(R.id.resumedVideoTv)
        errorRetryLayout = view.findViewById(R.id.errorRetryLayout)
        videoMenuLayout = view.findViewById(R.id.videoMenuLayout)
        volumeUnMuteButton = view.findViewById(R.id.volumeUnMuteButton)
        bufferingProgressBarLayout = view.findViewById(R.id.bufferingProgressBarLayout)
        circularProgressLayout = view.findViewById(R.id.circularProgressLayout)
        repeatVideoLinearLayout = view.findViewById(R.id.repeatVideoLinearLayout)
        settingButton = view.findViewById(R.id.settings_btn)
        volumeLinearLayout = view.findViewById(R.id.volumeLinearLayout)
        videoControllerLayout = view.findViewById(R.id.videoControllerLayout)
        previewFrameLayout = view.findViewById(R.id.previewFrameLayout)

        exoFfwdLinearLayout = view.findViewById(R.id.exoFfwdLinearLayout)
        exoRewLinearLayout = view.findViewById(R.id.exoRewLinearLayout)

        settingButton.setOnClickListener(this)
        centerButtonLayout = view.findViewById(R.id.centerButtonLayout)

        videoPlayButton = view.findViewById(R.id.exo_play)
        videoPauseButton = view.findViewById(R.id.exo_pause)


        previewTimeBar = view.findViewById(R.id.exo_progress)

        previewImageView = view.findViewById(R.id.imageView)
        simpleExoPlayerView = view.findViewById(R.id.videoPlayer)
        closeVideoPlayerButton = view.findViewById(R.id.closeButton);




        volumeMuteAndUnMuteButton.visibility = View.GONE


        previewTimeBar.setPreviewEnabled(true)
        previewTimeBar.addOnScrubListener(this)
        previewTimeBar.setPreviewLoader(this)


        volumeUnMuteButton.setOnClickListener {
            mMediaPlayer?.audioComponent?.volume = 0f
            volumeMuteAndUnMuteButton.visibility = View.VISIBLE
            volumeUnMuteButton.visibility = View.GONE
            isVolmueMute = false
        }


        volumeMuteAndUnMuteButton.setOnClickListener {
            mMediaPlayer?.audioComponent?.volume = 5f
            mMediaPlayer?.audioComponent?.volume = mMediaPlayer?.audioComponent?.volume!!
            volumeMuteAndUnMuteButton.visibility = View.GONE
            volumeUnMuteButton.visibility = View.VISIBLE
            isVolmueMute = true
        }


        closeVideoPlayerButton.setOnClickListener {
            context.finish()
        }


        simpleExoPlayerView?.setOnClickListener {
            if (isControllerShown) {
                hideController()
            } else {
                contentRateLayout.visibility = View.GONE
                showController()
            }
        }


        findViewById<View>(R.id.speed_btn)?.setOnClickListener { showSpeedControlDailog() }
        errorRetryLayout.setOnClickListener(OnClickListener {
            errorRetryLayout.setVisibility(GONE)
            initializeMainPlayer(mContentUrl, true)
        })

        exoRewLinearLayout.setOnClickListener {
            // rewind()
            videoRenuButton.performClick()
        }

        exoFfwdLinearLayout.setOnClickListener {
            //fastForward()
            videoFarwardButton.performClick()
        }


        videoPlayButton.setOnClickListener(OnClickListener {
            if (mMediaPlayer != null) {
                mMediaPlayer!!.playWhenReady = true
                videoPlayButton.setVisibility(GONE)
                videoPauseButton.setVisibility(VISIBLE)
                videoPlayerSdkCallBackListener?.onPlayClick(1)
            }
        })
        videoPauseButton.setOnClickListener(OnClickListener {
            if (mMediaPlayer != null) {
                mMediaPlayer!!.playWhenReady = false
                videoPlayButton.setVisibility(VISIBLE)
                videoPauseButton.setVisibility(GONE)
                videoPlayerSdkCallBackListener?.onPlayClick(0)
            }
        })

        val settings: MutableMap<String, Any> = HashMap()
        val gatewayUrl = "https://altbalaji-test.testonly.conviva.com"
        settings[ConvivaSdkConstants.GATEWAY_URL] = gatewayUrl
        settings[ConvivaSdkConstants.LOG_LEVEL] = ConvivaSdkConstants.LogLevel.DEBUG


        ConvivaAnalytics.init(context, "673e04f72dc817c2aedb7ff945d663eef99d1b5b", settings)
        videoAnalytics = ConvivaAnalytics.buildVideoAnalytics(context)
        adAnalytics = ConvivaAnalytics.buildAdAnalytics(context, videoAnalytics)

        super.onFinishInflate()
    }


    /* fun sendDeviceInfo() {
         val deviceInfo: MutableMap<String, Any> = HashMap()
         deviceInfo[ConvivaSdkConstants.DEVICEINFO.DEVICE_TYPE] = "Android"
         ConvivaAnalytics.setDeviceInfo(deviceInfo)
     }*/


    fun hideSeekBarLayout() {
        previewTimeBar.visibility = View.GONE
        durationLinearLayout.visibility = View.GONE
    }


    fun showSeekBarLayout() {
        previewTimeBar.visibility = View.VISIBLE
        durationLinearLayout.visibility = View.VISIBLE
    }


    fun releaseVideoAnalatics() {
        VideoPlayerTracer.error("CONVIVA SDK ADS:::", "releaseVideoAnalatics")
        videoAnalytics?.release()
    }

    fun onContentPlaybackStart(userId: String, contentTitle: String, url: String) {
        VideoPlayerTracer.error("CONVIVA SDK ADS:::", "onContentPlaybackStart")
        val contentInfo: MutableMap<String, Any> = HashMap()
        contentInfo[ConvivaSdkConstants.ASSET_NAME] = contentTitle
        contentInfo[ConvivaSdkConstants.STREAM_URL] = url
        contentInfo[ConvivaSdkConstants.PLAYER_NAME] = "ExoPlayer"
        contentInfo[ConvivaSdkConstants.DURATION] = getDuration()
        contentInfo[ConvivaSdkConstants.IS_LIVE] = "VOD"
        contentInfo[ConvivaSdkConstants.VIEWER_ID] = userId
        videoAnalytics?.setPlayer(simpleExoPlayerView!!.player)
        videoAnalytics?.setContentInfo(contentInfo)
        videoAnalytics?.reportPlaybackRequested(contentInfo)
    }

    fun onContentPlaybackEnded() {
        VideoPlayerTracer.error("CONVIVA SDK ADS:::", "onContentPlaybackEnded")
        videoAnalytics?.reportPlaybackEnded()
    }

    fun setPictureInPictureModeEnable(isEnable: Boolean) {
        isPipModeOn = isEnable

        if (isEnable) {
            countDownTimer1?.cancel()
            setTimerOnVideoPlayer(false)
            videoTitle.visibility = View.GONE
            contentRateLayout.visibility = View.GONE
        }
    }


    fun initAdSession() {

        if (adsLoader == null) VideoPlayerTracer.error("CONVIVA SDK ADS:::", "AD Loader Null")
        else VideoPlayerTracer.error("CONVIVA SDK ADS:::", "AD PLAYING")


        if (adAnalytics == null) adAnalytics =
            ConvivaAnalytics.buildAdAnalytics(context, videoAnalytics)

        val adMetadata: MutableMap<String, Any> = HashMap()
        adMetadata[ConvivaSdkConstants.AD_TAG_URL] = adsUrl!!
        adMetadata[ConvivaSdkConstants.AD_PLAYER] = ConvivaSdkConstants.AdPlayer.CONTENT.toString()
        adAnalytics?.setAdListener(adsLoader, adMetadata)
    }


    fun adBreakStarted() {
        videoAnalytics?.reportAdBreakStarted(
            ConvivaSdkConstants.AdPlayer.CONTENT, ConvivaSdkConstants.AdType.CLIENT_SIDE
        )
    }

    fun adBreakEnded() {
        videoAnalytics?.reportAdBreakEnded()
    }

    fun reportAdLoaded(mAd: Ad) {
        adAnalytics?.reportAdLoaded(getAdInfo(mAd))
    }

    fun reportAdStarted(mAd: Ad) {
        adAnalytics?.reportAdStarted(getAdInfo(mAd))
    }

    fun closeAdSession() {
        adAnalytics?.reportAdEnded()
    }

    fun skipAdSession() {
        adAnalytics?.reportAdSkipped()
    }

    fun reportAdError(message: String?) {
        //logic should be implemented to figure out the error severity
        adAnalytics?.reportAdError(message, ConvivaSdkConstants.ErrorSeverity.FATAL)
    }

    //Can be called directly if we want to report a ad fatal error and end the ad session together
    fun reportAdFailed(message: String?, mAd: Ad) {
        adAnalytics?.reportAdFailed(message, getAdInfo(mAd))
    }

    fun setAdPlayerState(key: String?, value: Enum<*>?) {
        adAnalytics?.reportAdMetric(key, value)
    }

    fun setAdBitrate(key: String?, value: Any?) {
        adAnalytics?.reportAdMetric(key, value)
    }

    fun setAdResolution(key: String?, width: Any?, height: Any?) {
        adAnalytics?.reportAdMetric(key, width, height)
    }

    fun setAdPlayerInfo() {
        val adPlayerInfo: MutableMap<String, Any> = HashMap()
        adPlayerInfo[ConvivaSdkConstants.FRAMEWORK_NAME] = "Google IMA"
        adPlayerInfo[ConvivaSdkConstants.FRAMEWORK_VERSION] = "3.23.0"
        adAnalytics?.setAdPlayerInfo(adPlayerInfo)
    }


    fun getAdInfo(ad: Ad): Map<String, Any> {
        //verify ad metadata w.r.t learning center documentation's instructions.
        val adInfo: MutableMap<String, Any> = HashMap()
        adInfo[ConvivaSdkConstants.ASSET_NAME] = ad.title
        //pass ad tag url for STREAM_URL
        adInfo[ConvivaSdkConstants.STREAM_URL] = adsUrl!!
        adInfo[ConvivaSdkConstants.IS_LIVE] = false
        adInfo[ConvivaSdkConstants.DEFAULT_RESOURCE] = "AD_AKAMAI"
        adInfo[ConvivaSdkConstants.DURATION] = ad.duration.toString()
        adInfo[ConvivaSdkConstants.ENCODED_FRAMERATE] = 25

        val podInfo = ad.adPodInfo
        val podPosition =
            if (podInfo.podIndex == 0) ConvivaSdkConstants.AdPosition.PREROLL.toString() else if (podInfo.podIndex == -1) ConvivaSdkConstants.AdPosition.POSTROLL.toString() else ConvivaSdkConstants.AdPosition.MIDROLL.toString()
        val firstAdSystem: String
        val firstAdId: String
        val firstCreativeId: String
        if (ad.adWrapperIds.size != 0) {
            val len = ad.adWrapperIds.size
            firstAdSystem = ad.adWrapperSystems[len - 1]
            firstAdId = ad.adWrapperIds[len - 1]
            firstCreativeId = ad.adWrapperCreativeIds[len - 1]
        } else {
            firstAdSystem = ad.adSystem
            firstAdId = ad.adId
            firstCreativeId = ad.creativeId
        }
        adInfo["c3.ad.technology"] = "Client Side"
        adInfo["c3.ad.id"] = ad.adId
        adInfo["c3.ad.system"] = ad.adSystem
        adInfo["c3.ad.position"] = podPosition
        adInfo["c3.ad.isSlate"] = "NA"
        adInfo["c3.ad.mediaFileApiFramework"] = "NA"
        adInfo["c3.ad.adStitcher"] = "NA"
        adInfo["c3.ad.firstAdSystem"] = firstAdSystem
        adInfo["c3.ad.firstAdId"] = firstAdId
        adInfo["c3.ad.firstCreativeId"] = firstCreativeId
        adInfo["c3.ad.creativeId"] = ad.creativeId
        setAdPlayerInfo()
        return adInfo
    }


    /**
     * Demonstration of ConvivaAdAnalytics cleanup.
     */
    fun releaseAdSession() {
        if (adAnalytics != null) {
            VideoPlayerTracer.error("CONVIVA SDK ADS:::", "releaseAdSession")
            adAnalytics!!.release()
        }
    }


    fun setSkipDuraionArrayList(skipDurationArray: ArrayList<SkipDuration>) {
        this.skipDurationArray = skipDurationArray;
    }

    fun getExoPlayer(): StyledPlayerView {
        return simpleExoPlayerView!!
    }


    fun skipVideo(duration: String) {
        val units = duration.split(":".toRegex()).toTypedArray()
    }


    fun setWebSeriesEnable(
        isWebSeries: Boolean, UserLoginStatus: Boolean, contentAccessType: String
    ) {
        this.isWebSeries = isWebSeries
        this.userSubscriptionDtatus = UserLoginStatus
        this.contentAccessType = contentAccessType
    }


    fun showEpisodeButton(show: Boolean) {
        if (isWebSeries && show) {
            epsodeButton.visibility = View.VISIBLE
        } else {
            epsodeButton.visibility = View.GONE
        }
    }


    fun getCurrentDurationFromTextView(): String {
        return exoCurrentPosition.text.toString()
    }


    fun showMuteUnMuteButton() {
        volumeLinearLayout.visibility = View.VISIBLE
    }

    fun hideMuteUnMuteButton() {
        volumeLinearLayout.visibility = View.GONE
    }


    fun showBackButton() {
        closeVideoPlayerButton.visibility = View.VISIBLE
    }

    fun hideBackButton() {
        closeVideoPlayerButton.visibility = View.GONE
    }


    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        isAttachedToWindowStatus = true
        updatePlayPauseButton()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        isAttachedToWindowStatus = false
        removeCallbacks(hideAction)
        if (hideAtMs != C.TIME_UNSET) {
            val delayMs = hideAtMs - SystemClock.uptimeMillis()
            if (delayMs <= 0) {
                hideController()
            } else {
                postDelayed(hideAction, delayMs)
            }
        }
    }

    private val hideAction = Runnable {
        isControllerShown = false
        VideoPlayerTracer.error("Controller Listener:::", "Stop Timer")
        //hideController()
    }


    fun isNeedVideoPlayerPause(): Boolean {
        return isPipModeOn;
    }

    fun disablePipMode(isPipModeOn: Boolean) {
        this.isPipModeOn = isPipModeOn

        if (isPipModeOn) {
            countDownTimer1?.cancel()
            videoTitle.visibility = View.GONE
            contentRateLayout.visibility = View.GONE
        }

    }


    fun setSpriteImageUrl(url: String) {
        this.spriteImageUrl = url;
    }

    fun hideTitleContanierView(isSHow: Boolean) {
        if (isSHow) videoTitle.visibility = View.VISIBLE
        else videoTitle.visibility = View.GONE
    }

    fun hideRatingContanierView(isSHow: Boolean) {
        if (isSHow) contentRateLayout.visibility = View.VISIBLE
        else contentRateLayout.visibility = View.GONE
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        val orientation = resources.configuration.orientation
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {

            if (isPipModeOn)
                hideSeekBarLayout()
            else
                showSeekBarLayout()



            if (contentTitle != null && !TextUtils.isEmpty(contentTitle) && !isPipModeOn) {
                videoTitle.visibility = View.VISIBLE
                videoTitle.setText(contentTitle)
            } else videoTitle.visibility = View.GONE
        } else if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            videoTitle.visibility = View.GONE
            hideSeekBarLayout()
        }
        super.onConfigurationChanged(newConfig)
    }

    private fun hideAfterTimeout() {
        removeCallbacks(hideAction)
        if (5000 > 0) {
            VideoPlayerTracer.error("Controller Listener:::", "Start Timer")
            hideAtMs = SystemClock.uptimeMillis() + 5000
            if (isAttachedToWindow) {
                postDelayed(hideAction, 5000)
            }
        } else {
            hideAtMs = C.TIME_UNSET
        }
    }


    fun hideController() {
        previewTimeBar.visibility = GONE
        durationLinearLayout.visibility = GONE
        closeVideoPlayerButton.visibility = GONE
        // overlayImageTransparent.visibility = GONE
        centerButtonLayout.visibility = GONE
        videoMenuLayout.visibility = GONE
        resumedVideoTv.visibility = GONE
        removeCallbacks(hideAction)
        hideAtMs = C.TIME_UNSET
        isControllerShown = false

        if (!isPipModeOn) {
            contentRateLayout.visibility = VISIBLE
            setTimerOnVideoPlayer(true)
        } else {
            videoTitle.visibility = View.GONE
            contentRateLayout.visibility = View.GONE
        }

        updatePlayPauseButton()


    }

    fun showController() {
        closeVideoPlayerButton.visibility = VISIBLE
        //overlayImageTransparent.visibility = VISIBLE
        centerButtonLayout.visibility = VISIBLE
        videoMenuLayout.visibility = VISIBLE
        resumedVideoTv.visibility = GONE
        updatePlayPauseButton()
        hideAfterTimeout()
        isControllerShown = true
        setTimerOnVideoPlayer(false)
        updatePlayPauseButton()
        contentRateLayout.visibility = View.GONE

        val orientation = getContext().resources.configuration.orientation

        if (orientation == Configuration.ORIENTATION_LANDSCAPE) showSeekBarLayout()
        else hideSeekBarLayout()
    }

    private fun updatePlayPauseButton() {
        var requestPlayPauseFocus = false
        val playing = mMediaPlayer != null && mMediaPlayer!!.playWhenReady
        requestPlayPauseFocus =
            requestPlayPauseFocus or (playing && videoPlayButton.isFocused)
        videoPlayButton.visibility = if (playing) GONE else VISIBLE
        requestPlayPauseFocus =
            requestPlayPauseFocus or (!playing && videoPauseButton.isFocused)
        videoPauseButton.visibility = if (!playing) GONE else VISIBLE
        if (requestPlayPauseFocus) {
            requestPlayPauseFocus()
        }
    }

    private fun requestPlayPauseFocus() {
        val playing = mMediaPlayer != null && mMediaPlayer!!.playWhenReady
        if (!playing) {
            videoPlayButton.requestFocus()
        } else if (playing) {
            videoPauseButton.requestFocus()
        }
    }

    val audioFocusChangeListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
        if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
            if (mMediaPlayer != null && mMediaPlayer!!.playWhenReady) {
                checkForAudioFocus()
            }
        }
    }


    private var audioManager: AudioManager? = null

    private fun checkForAudioFocus(): Boolean {


        // Request audio focus for playback
        val result = audioManager?.requestAudioFocus(
            audioFocusChangeListener,  // Use the music stream.
            AudioManager.STREAM_MUSIC,  // Request permanent focus.
            AudioManager.AUDIOFOCUS_GAIN
        )
        return if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            true
        } else {
            // other app had stopped playing song now , so u can do u stuff now .
//            Toast.makeText(context, "Other audio player did not stop. Can't play video",
//                    Toast.LENGTH_LONG).show();
//            pause();
            pauseVideoPlayer()
            false
        }
    }

    var phoneStateListener: PhoneStateListener = object : PhoneStateListener() {
        override fun onCallStateChanged(state: Int, incomingNumber: String) {
            if (state == TelephonyManager.CALL_STATE_RINGING) {
                //INCOMING call
                //do all necessary action to pause the audio
                pauseVideoPlayer()
            } else if (state == TelephonyManager.CALL_STATE_IDLE) {
                //Not IN CALL
                //do anything if the phone-state is idle
                if (!CommonUtils.isAppIsInBackground(context)) resumeVideoPlayer()
            } else if (state == TelephonyManager.CALL_STATE_OFFHOOK) {
                //A call is dialing, active or on hold
                //do all necessary action to pause the audio
                //do something here
                pauseVideoPlayer()
            }
            super.onCallStateChanged(state, incomingNumber)
        }
    }


    fun prepareVideoPlayer() {
        if (mContentUrl == null) throw Exception("Content type must not be null")

        initViews()
    }


    // init view and view group here
    private fun initViews() {

        if (mMediaPlayer != null && simpleExoPlayerView != null) {
            simpleExoPlayerView!!.player!!.release()
            mMediaPlayer!!.release()
            if (adsLoader != null) adsLoader!!.setPlayer(null)
        }

        trackSelector = DefaultTrackSelector(context)
        audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager


        if (videoPlayerSdkCallBackListener != null) videoPlayerSdkCallBackListener!!.onPlayerReady(
            mContentUrl
        )
        mWindow = context.window
    }

    fun setMultiTvVideoPlayerPausePlaySdkListener(videoPlayPauseCallBackListener: VideoPlayPauseCallBackListener?) {
        this.videoPlayPauseCallBackListener = videoPlayPauseCallBackListener
    }

    fun setMultiTvVideoPlayerSdkListener(videoPlayerSdkCallBackListener: VideoPlayerSdkCallBackListener?) {
        this.videoPlayerSdkCallBackListener = videoPlayerSdkCallBackListener
    }

    fun setContentFilePath(path: String?) {
        mContentUrl = path
    }

    fun setContentType(type: ContentType?) {
        contentType = type
    }

    fun setPreRollAdUrl(adUrl: String?) {
        adsUrl = adUrl
    }

    fun setAnalaticsUrl(analaticsUrl: String?) {
        this.analaticsUrl = analaticsUrl
    }

    fun setKeyToken(token: String?) {
        this.token = token
    }

    fun setAuthDetails(id: String?) {
        userId = id
    }

    fun setContentTitle(title: String?) {
        contentTitle = title
        videoTitle.setText(contentTitle)
    }

    fun setContentId(id: String?) {
        contentId = id
    }

    fun setSubtitleVideoUri(subtitleUri: String?) {
        subTitleUri = subtitleUri
    }

    fun setDrmEnabled(
        drmContent: Boolean,
        siteId: String?,
        siteKey: String?,
        drmToekn: String?,
        drmLicenseUrl: String?
    ) {
        isDrmContent = drmContent
        drmContentToken = drmToekn
        drmdrmLicenseUrl = drmLicenseUrl
        this.siteId = siteId
        this.siteKey = siteKey
    }


    fun setOfflineContentData(isOfflineContent: Boolean) {
        this.isOfflineContent = isOfflineContent
    }

    // get play duration of video in milli second
    fun getContentPlayedTimeInMillis(): Long {
        // contentPlayedTimeInMillis = 0;
        if (contentPlayedTimeInMillis == 0L) {
            if (seekPlayerTo != 0) contentPlayedTimeInMillis =
                seekPlayerTo.toLong() else if (mMediaPlayer != null) contentPlayedTimeInMillis =
                mMediaPlayer!!.currentPosition
        } else {
            if (seekPlayerTo != 0) contentPlayedTimeInMillis =
                seekPlayerTo.toLong() else if (mMediaPlayer != null) contentPlayedTimeInMillis =
                mMediaPlayer!!.currentPosition
        }
        return contentPlayedTimeInMillis
    }

    fun setContentPlayedTimeInMillis(contentPlayedTimeInMillis: Long) {
        this.contentPlayedTimeInMillis = contentPlayedTimeInMillis
    }

    // start video player when player is ready state
    fun startVideoPlayer(isNeedToPlayInstantly: Boolean) {
        initializeMainPlayer(mContentUrl, true)
    }

    // resume video player
    fun resumeVideoPlayer() {
        if (mMediaPlayer != null && simpleExoPlayerView != null) {
            simpleExoPlayerView!!.onResume()
            mMediaPlayer!!.playWhenReady = true
            if (isPipModeOn) videoPlayPauseCallBackListener?.videoStart()
        }
    }

    // pause video player
    fun pauseVideoPlayer() {
        if (mMediaPlayer != null && simpleExoPlayerView != null) {
            simpleExoPlayerView!!.onPause()
            mMediaPlayer!!.playWhenReady = false
            if (isPipModeOn) videoPlayPauseCallBackListener?.videoStop()
        }
    }

    // relase and destroy video player
    fun releaseVideoPlayer() {
        if (mMediaPlayer != null && simpleExoPlayerView != null) {
            //sendAnalaticsData(context, userId, contentId, contentTitle, token)
            simpleExoPlayerView!!.player!!.release()
            mMediaPlayer!!.release()
            if (adsLoader != null) adsLoader!!.setPlayer(null)
        }
    }

    fun getTrackSelector(): DefaultTrackSelector {
        return trackSelector
    }


    private fun initializeMainPlayer(videoUrl: String?, isNeedToPlayInstantly: Boolean) {
//        ToastMessage.showLogs(ToastMessage.LogType.ERROR, "Video Player:::", "initializeMainPlayer");
        if (mMediaPlayer != null) {
            mMediaPlayer!!.release()
            if (adsLoader != null) adsLoader!!.setPlayer(null)
            mMediaPlayer = null
        }

        videoTitle.setText(contentTitle)

        //var subtitleSource = SingleSampleMediaSource(subtitleUri, ...);
        videoControllerLayout?.visibility = GONE
        previewTimeBar.visibility = GONE
        durationLinearLayout.visibility = GONE
        videoPlayerSdkCallBackListener?.prepareVideoPlayer()
        //        ToastMessage.showLogs(ToastMessage.LogType.DEBUG, TAG, "Content url is " + videoUrl);
        val customLoadControl: LoadControl =
            DefaultLoadControl.Builder().setBufferDurationsMs(1000, 50000, 1000, 1)
                .setAllocator(DefaultAllocator(true, 32 * 1024))
                .setTargetBufferBytes(C.LENGTH_UNSET).setPrioritizeTimeOverSizeThresholds(true)
                .setBackBuffer(0, false).build()




        if (adsUrl != null && !TextUtils.isEmpty(adsUrl)) {
            val dataSourceFactory: DataSource.Factory = DefaultDataSource.Factory(
                context
            )
            val mediaSourceFactory: MediaSourceFactory =
                DefaultMediaSourceFactory(dataSourceFactory).setAdsLoaderProvider { unusedAdTagUri: MediaItem.AdsConfiguration? -> adsLoader }
                    .setAdViewProvider(simpleExoPlayerView)


            mMediaPlayer = ExoPlayer.Builder(context).setSeekBackIncrementMs(10000)
                .setSeekForwardIncrementMs(10000).setMediaSourceFactory(mediaSourceFactory)
                .setTrackSelector(trackSelector).setLoadControl(customLoadControl).build()
            adsLoader = ImaAdsLoader.Builder( /* context= */context).build()

        } else {
            mMediaPlayer = ExoPlayer.Builder(context).setSeekBackIncrementMs(10000)
                .setSeekForwardIncrementMs(10000).setTrackSelector(trackSelector)
                .setLoadControl(customLoadControl).build()
        }
        if (mMediaPlayer != null) {
            mMediaPlayer!!.addListener(stateChangeCallback1)
            simpleExoPlayerView!!.player = mMediaPlayer
            simpleExoPlayerView!!.controllerHideOnTouch = true
            simpleExoPlayerView!!.setControllerHideDuringAds(true)
            var mediaItem: MediaItem? = null
            var subtitle: MediaItem.SubtitleConfiguration? = null
            if (subTitleUri != null && !TextUtils.isEmpty(subTitleUri)) {
                subtitle = MediaItem.SubtitleConfiguration.Builder(Uri.parse(subTitleUri))
                    .setMimeType(MimeTypes.APPLICATION_SUBRIP) // The correct MIME type (required).
                    .setLanguage("en") // MUST, The subtitle language (optional).
                    .setSelectionFlags(C.SELECTION_FLAG_DEFAULT) //MUST,  Selection flags for the track (optional).
                    .build()
            }

            if (isDrmContent) {
                try {
                    WVMAgent = PallyconWVMSDKFactory.getInstance(context)
                    WVMAgent?.init(context, null, siteId, siteKey)
                    WVMAgent?.setPallyconEventListener(pallyconEventListener)
                } catch (e: PallyconDrmException) {
                    e.printStackTrace()
                } catch (e: UnAuthorizedDeviceException) {
                    e.printStackTrace()
                }
                mediaItem = MediaItem.Builder().setUri(videoUrl).build()
                val drmSchemeUuid = UUID.fromString(C.WIDEVINE_UUID.toString())
                val uri = Uri.parse(videoUrl)
                try {
                    drmSessionManager = WVMAgent!!.createDrmSessionManagerByToken(
                        drmSchemeUuid, drmdrmLicenseUrl, uri, drmContentToken
                    )
                } catch (e: PallyconDrmException) {
                    e.printStackTrace()
                }
                val playerMediaSource = ExoUttils().buildMediaSource(
                    context, mediaItem, videoUrl!!, drmSessionManager!!
                )
                mMediaPlayer!!.setMediaSource(playerMediaSource!!)
            } else if (isOfflineContent) {
                mediaItem = MediaItem.Builder().setUri(videoUrl).build()
                val downloadRequest: DownloadRequest? =
                    VideoPlayerDownloadUtil.getDownloadTracker(context)
                        .getDownloadRequest(mediaItem.playbackProperties?.uri)
                VideoPlayerTracer.error(
                    "Offline Video Url:::", "" + mediaItem.playbackProperties?.uri
                )
                val mediaSource = DownloadHelper.createMediaSource(
                    downloadRequest!!, VideoPlayerDownloadUtil.getReadOnlyDataSourceFactory(context)
                )

                mMediaPlayer!!.setMediaSource(mediaSource!!)

            } else {
                mediaItem = if (subtitle != null) {
                    /*MediaSource playerMediaSource = new ExoUttils().buildMediaSource(context, mediaItem, videoUrl, drmSessionManager);
                    MediaSource mediaSource = new MergingMediaSource(mediaSources);
                    mMediaPlayer.setMediaSource(mediaSource);*/
                    if (adsUrl != null && !TextUtils.isEmpty(adsUrl)) {
                        adsLoader!!.setPlayer(mMediaPlayer)
                        val adTagUri = Uri.parse(adsUrl)
                        MediaItem.Builder().setSubtitleConfigurations(ImmutableList.of(subtitle))
                            .setUri(videoUrl).setAdsConfiguration(
                                MediaItem.AdsConfiguration.Builder(adTagUri).build()
                            ).build()
                    } else {
                        MediaItem.Builder().setSubtitleConfigurations(ImmutableList.of(subtitle))
                            .setUri(videoUrl).build()
                    }
                } else {
                    if (adsUrl != null && !TextUtils.isEmpty(adsUrl)) {
                        adsLoader!!.setPlayer(mMediaPlayer)
                        adsLoader!!.skipAd()
                        adsLoader!!.focusSkipButton();
                        val adTagUri = Uri.parse(adsUrl)

                        MediaItem.Builder().setUri(videoUrl).setAdsConfiguration(
                            MediaItem.AdsConfiguration.Builder(adTagUri).build()
                        ).build()


                    } else {
                        MediaItem.Builder().setUri(videoUrl).build()
                    }
                }
                mMediaPlayer!!.setMediaItem(mediaItem)
            }
            mMediaPlayer!!.audioComponent!!.volume = mMediaPlayer!!.audioComponent!!.volume
            mMediaPlayer!!.prepare()
            if (isNeedToPlayInstantly) {
                mMediaPlayer!!.playWhenReady = true
            }

            val mediaSession = MediaSessionCompat(context, "com.lionsgacom.multitv.ottteplay")
            val mediaSessionConnector = MediaSessionConnector(mediaSession)
            mediaSessionConnector.setPlayer(mMediaPlayer)
            mediaSession.isActive = true

            val volume = audioManager?.getStreamVolume(AudioManager.STREAM_MUSIC) as Int
            mMediaPlayer?.audioComponent?.volume = volume.toFloat()
            if (volume < 1) {
                volumeMuteAndUnMuteButton.visibility = View.VISIBLE
                volumeUnMuteButton.visibility = View.GONE
            } else {
                volumeMuteAndUnMuteButton.visibility = View.GONE
                volumeUnMuteButton.visibility = View.VISIBLE
            }


            if (isWatchDurationEnable) seekTo(
                mMediaPlayer!!.currentPosition + watchDuration * 1000
            )


            if (adsUrl != null && !TextUtils.isEmpty(adsUrl)) {
                adsLoader?.adsLoader?.addAdsLoadedListener(object :
                    com.google.ads.interactivemedia.v3.api.AdsLoader.AdsLoadedListener {
                    override fun onAdsManagerLoaded(adsManagerLoadedEvent: AdsManagerLoadedEvent) {
                        val adsManager = adsManagerLoadedEvent.getAdsManager()
                        adsManager.addAdEventListener(object : AdEvent.AdEventListener {
                            override fun onAdEvent(adEvent: AdEvent) {
                                Log.e("Ads Event:::", "" + adEvent.type)
                                if (adEvent.type.equals("STARTED")) {
                                    videoPlayerSdkCallBackListener?.onAdPlay()
                                    if (!isPipModeOn) setTimerOnVideoPlayer(false)
                                } else if (adEvent.type.equals("COMPLETED")) {
                                    videoPlayerSdkCallBackListener?.onAdCompleted()
                                    if (!isPipModeOn) setTimerOnVideoPlayer(true)
                                }
                            }

                        })
                    }

                })


            } else {
                videoPlayerSdkCallBackListener?.onAdCompleted()
                if (!isPipModeOn) {
                    setTimerOnVideoPlayer(true)
                } else {
                    countDownTimer1?.cancel()
                }
            }

        }
    }

    var stateChangeCallback1: Player.Listener = object : Player.Listener {
        override fun onPlayerError(error: PlaybackException) {
            super.onPlayerError(error)
            if (mMediaPlayer != null && mMediaPlayer!!.currentPosition != 0L) seekPlayerTo =
                mMediaPlayer!!.currentPosition.toInt() / 1000
            errorRetryLayout.bringToFront()
            errorRetryLayout.visibility = VISIBLE
            videoControllerLayout?.visibility = GONE
            videoPlayerSdkCallBackListener!!.onPlayerError(error.message)
        }


        override fun onVideoSizeChanged(videoSize: VideoSize) {
            super.onVideoSizeChanged(videoSize)
            mInitialTextureWidth = videoSize.width
            mInitialTextureHeight = videoSize.height
        }

        override fun onLoadingChanged(isLoading: Boolean) {}
        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            var text = "Main player"
            updatePlayPauseButton()

            if (playbackState == Player.STATE_READY && playWhenReady) {
                previewTimeBar.hidePreview()
            }

            when (playbackState) {
                ExoPlayer.STATE_BUFFERING -> {
                    text += "buffering"
                    bufferingProgressBarLayout.bringToFront()
                    bufferingProgressBarLayout.visibility = VISIBLE
                    //hideController()
                    startBufferingTimer()
                }
                ExoPlayer.STATE_ENDED -> {
                    text += "ended"


                    if (isContentRepetPlaying) {
                        repeatVideoLinearLayout.visibility = View.VISIBLE
                        repeatVideoLinearLayout.setOnClickListener {
                            repeatVideoLinearLayout.visibility = View.GONE
                            initializeMainPlayer(mContentUrl, true)
                        }
                    } else {
                        if (contentType == ContentType.VOD) {
                            repeatVideoLinearLayout.visibility = View.GONE

                            if (mMediaPlayer != null) contentPlayedTimeInMillis =
                                mMediaPlayer!!.currentPosition
                            releaseVideoPlayer()
                            bufferingProgressBarLayout.visibility = GONE
                            circularProgressRing =
                                findViewById<View>(R.id.circular_progress_ring) as FabButton
                            circularProgressRing.showProgress(true)
                            circularProgressRing.setProgress(0f)
                            circularProgressLayout.visibility = VISIBLE
                            // circularProgressLayout!!.bringToFront()
                            val totalDuration = 1200
                            val tickDuration = 300
                            countDownTimer = object : CountDownTimerWithPause(
                                totalDuration.toLong(), (tickDuration / 10).toLong(), true
                            ) {
                                override fun onTick(millisUntilFinished: Long) {
                                    var progress = millisUntilFinished.toFloat() / totalDuration
                                    progress = progress * 100
                                    progress = 100 - progress
                                    circularProgressRing.setProgress(progress)
                                }


                                // comment

                                override fun onFinish() {
                                    circularProgressRing =
                                        findViewById<View>(R.id.circular_progress_ring) as FabButton

                                    circularProgressLayout.visibility = View.GONE

                                    if (isWebSeries) {
                                        if (userSubscriptionDtatus) videoPlayerSdkCallBackListener?.onPlayNextVideo()
                                        else if (contentAccessType.equals("paid") && !userSubscriptionDtatus) videoPlayerSdkCallBackListener?.subscriptionCallBack()
                                        else if (contentAccessType.equals("free")) videoPlayerSdkCallBackListener?.onPlayNextVideo()
                                        else videoPlayerSdkCallBackListener?.showThumbnailCallback()

                                    } else {
                                        if (contentAccessType.equals("free")) videoPlayerSdkCallBackListener?.onPlayNextVideo()
                                        else videoPlayerSdkCallBackListener?.subscriptionCallBack()
                                    }

                                }
                            }.create()
                        }

                    }

                }
                ExoPlayer.STATE_IDLE -> {
                    text += "idle"
                    if (!checkForAudioFocus()) return
                    bufferingProgressBarLayout!!.visibility = GONE
                    if (mMediaPlayer != null) {
                        contentPlayedTimeInMillis = mMediaPlayer!!.currentPosition
                        if (contentType == ContentType.LIVE) startBufferingTimer()
                    }
                    simpleExoPlayerView!!.videoSurfaceView!!.visibility = VISIBLE
                    simpleExoPlayerView!!.visibility = VISIBLE
                    simpleExoPlayerView!!.bringToFront()
                }
                ExoPlayer.STATE_READY -> {
                    text += "ready"
                    bufferingProgressBarLayout.visibility = GONE
                    videoControllerLayout?.visibility = VISIBLE

                    videoPlayerSdkCallBackListener?.onVideoStartNow()

                    if (mMediaPlayer!!.isPlayingAd()) {
                        videoPlayerSdkCallBackListener?.onAdPlay()
                    } else {
                        videoPlayerSdkCallBackListener?.onAdCompleted()
                    }

                    stopBufferingTimer()
                }
                else -> text += "unknown"
            }
        }

        override fun onRepeatModeChanged(repeatMode: Int) {}
        override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters) {}

    }


    fun isPlayingAds(): Boolean {
        Log.e("Vikram Content Status::", "" + mMediaPlayer!!.isPlayingAd())
        return mMediaPlayer!!.isPlayingAd()
    }

    private fun startBufferingTimer() {
        if (bufferingTimeHandler == null) {
            bufferingTimeHandler = Handler()
        }
        if (bufferingTimeRunnable != null) bufferingTimeHandler!!.postDelayed(
            bufferingTimeRunnable, 0
        )

        videoPlayerSdkCallBackListener?.onBufferStart()
    }


    fun stopBufferingTimer() {
        if (bufferingTimeHandler != null && bufferingTimeRunnable != null) {
            bufferingTimeHandler!!.removeCallbacks(bufferingTimeRunnable)
            bufferingTimeHandler!!.removeCallbacksAndMessages(null)
            videoPlayerSdkCallBackListener?.onBUfferStop(bufferingTimeInMillis)
        }
    }

    fun getVideoUrl(): Uri {
        return mMediaPlayer?.currentMediaItem?.playbackProperties?.uri!!
    }


    private val bufferingTimeRunnable: Runnable? = object : Runnable {
        override fun run() {
            bufferingTimeInMillis = bufferingTimeInMillis + 1000

            //Log.e("Naseeb", "Buffering time " + bufferingTimeInMillis);
            if (bufferingTimeHandler != null) bufferingTimeHandler!!.postDelayed(this, 1000)
        }
    }
    var dialog: AlertDialog? = null
    fun hideSpeedDailog() {
        if (dialog != null && dialog!!.isShowing) dialog!!.dismiss()
    }

    fun showSpeedControlDailog() {
        // setup the alert builder
        if (dialog != null && dialog!!.isShowing) dialog!!.dismiss()
        val position =
            sharedPreferencePlayer.getPreferencesInt(context, "player_playback_position", 2)
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Speed Control")
        val animals = arrayOf("0.25x", "0.5x", "Normal", "1.5x", "2x")
        builder.setSingleChoiceItems(animals, position) { dialog, which ->
            var speed = 1f
            var pitch = 1f
            when (which) {
                0 -> {
                    speed = 0.25f
                    pitch = 0.25f
                    sharedPreferencePlayer.setPreferenceInt(context, "player_playback_position", 0)
                }
                1 -> {
                    speed = 0.5f
                    pitch = 0.5f
                    sharedPreferencePlayer.setPreferenceInt(context, "player_playback_position", 1)
                }
                2 -> {
                    speed = 1f
                    pitch = 1f
                    sharedPreferencePlayer.setPreferenceInt(context, "player_playback_position", 2)
                }
                3 -> {
                    speed = 1.5f
                    pitch = 1.5f
                    sharedPreferencePlayer.setPreferenceInt(context, "player_playback_position", 3)
                }
                4 -> {
                    speed = 2f
                    pitch = 2f
                    sharedPreferencePlayer.setPreferenceInt(context, "player_playback_position", 4)
                }
            }
            val param = PlaybackParameters(speed, pitch)
            mMediaPlayer!!.playbackParameters = param
            dialog.dismiss()
        }
        dialog = builder.create()
        dialog?.show()
    }

    override fun onClick(view: View) {
        if (view === setting) {
            if (!isShowingTrackSelectionDialog && TrackSelectionDialog.willHaveContent(trackSelector)) {
                isShowingTrackSelectionDialog = true
                val trackSelectionDialog = TrackSelectionDialog.createForTrackSelector(
                    trackSelector
                )  /* onDismissListener= */
                { dismissedDialog: DialogInterface? -> isShowingTrackSelectionDialog = false }
                trackSelectionDialog.show(context.supportFragmentManager,  /* tag= */null)
            }
        }
    }


    private fun hideSystemBars() {
        val decorView = (getContext() as Activity).window.decorView
        val uiOptions = (SYSTEM_UI_FLAG_HIDE_NAVIGATION or SYSTEM_UI_FLAG_FULLSCREEN)
        decorView.systemUiVisibility = uiOptions

        if (contentTitle != null && !TextUtils.isEmpty(contentTitle) && !isPipModeOn) {
            videoTitle.visibility = View.VISIBLE
            videoTitle.setText(contentTitle)
        } else videoTitle.visibility = View.GONE
    }

    private fun showSystemBar() {
        val decorView = (getContext() as Activity).window.decorView
        val uiOptions = SYSTEM_UI_FLAG_VISIBLE
        decorView.systemUiVisibility = uiOptions
        //hideSystemUiFullScreen()

        videoTitle.visibility = View.GONE

        if (contentTitle != null && !TextUtils.isEmpty(contentTitle) && !isPipModeOn) {
            videoTitle.visibility = View.VISIBLE
            videoTitle.setText(contentTitle)
        } else videoTitle.visibility = View.GONE
    }

    /*  @SuppressLint("InlinedApi")
      private fun hideSystemUiFullScreen() {

          simpleExoPlayerView!!.systemUiVisibility = (View.SYSTEM_UI_FLAG_LOW_PROFILE
                  or View.SYSTEM_UI_FLAG_FULLSCREEN
                  or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                  or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                  or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                  or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
      }*/


    private fun rewind() {
        seekTo(mMediaPlayer!!.currentPosition - DEFAULT_REWIND_MS)
    }

    fun seekToVideoPlayer(skipDuration: Int) {
        if (skipDuration > 1) {
            seekTo(mMediaPlayer!!.currentPosition + skipDuration * 1000)
        }
    }

    fun skipVideo(watch: Int) {
        Log.e("Current position:::", "" + mMediaPlayer!!.currentPosition)
        val seek = watch * 1000 - mMediaPlayer!!.currentPosition
        Log.e("Seek position:::", "" + seek)
        Log.e("Total Skip position:::", "" + mMediaPlayer!!.currentPosition + seek)
        seekTo(mMediaPlayer!!.currentPosition + seek)
        showController()
    }

    private var isWatchDurationEnable = false
    private var watchDuration = 0

    fun seekVideoPlayer(watch: Int, isWatchDurationEnable: Boolean) {
        this.isWatchDurationEnable = isWatchDurationEnable
        this.watchDuration = watch;
    }

    fun showResumedVideoHint(isShow: Boolean) {
//        if (isShow)
//            resumedVideoTv?.visibility = View.VISIBLE
//        else
//            resumedVideoTv?.visibility = View.GONE
    }

    private fun fastForward() {
        seekTo(mMediaPlayer!!.currentPosition + DEFAULT_FAST_FORWARD_MS)
    }

    fun seekTo(positionMs: Long) {
        mMediaPlayer?.seekTo(positionMs)
    }


    fun getDuration(): Long {
        return if (mMediaPlayer != null) mMediaPlayer!!.duration else 0
    }

    fun getDurationFromPlayer(): Long {
        return if (mMediaPlayer != null) mMediaPlayer!!.duration else 0
    }


    val currentPosition: Long
        get() = if (mMediaPlayer != null) mMediaPlayer!!.currentPosition else 0

    fun resumeFromPosition(millisecondsForResume: Long) {
        if (millisecondsForResume != 0L) {
            this.millisecondsForResume = millisecondsForResume

        }
    }

    override fun onScrubStart(previewBar: PreviewBar) {
        if (!TextUtils.isEmpty(spriteImageUrl))
            previewFrameLayout.visibility = View.VISIBLE
        else
            previewFrameLayout.visibility = INVISIBLE

        pauseVideoPlayer()
    }

    override fun onScrubMove(previewBar: PreviewBar, progress: Int, fromUser: Boolean) {
        if (!TextUtils.isEmpty(spriteImageUrl))
            previewFrameLayout.visibility = View.VISIBLE
        else
            previewFrameLayout.visibility = INVISIBLE

        exoCurrentPosition.text = Util.getStringForTime(
            formatBuilder, formatter, progress.toLong()
        )
        pauseVideoPlayer()

    }

    override fun onScrubStop(previewBar: PreviewBar) {
        previewFrameLayout.visibility = INVISIBLE
        if (mMediaPlayer != null) {
            seekTo(previewBar.progress.toLong())
        }

        resumeVideoPlayer()
    }

    private var maxLine = 0
    fun setSpriteImageThumbnailMaxLine(maxLine: Int) {
        this.maxLine = maxLine;
    }


    override fun loadPreview(currentPosition: Long, max: Long) {
        /*    previewFrameLayout.visibility = View.VISIBLE
            val targetX = updatePreviewX(currentPosition.toInt(), mMediaPlayer!!.duration.toInt())
            previewFrameLayout.x = targetX.toFloat()
            Glide.with(previewImageView)
                .load(spriteImageUrl)
                .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                .transform(GlideThumbnailTransformation(currentPosition))
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .into(previewImageView)*/

        if (!TextUtils.isEmpty(spriteImageUrl)) {
            previewFrameLayout.visibility = View.VISIBLE
            Glide.with(previewImageView).load(spriteImageUrl)
                .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                .fitCenter()
                .transform(GlideThumbnailTransformation(currentPosition, maxLine))
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE).into(previewImageView)
        } else {
            previewFrameLayout.visibility = View.GONE
        }


    }


    private fun dpToPx(displayMetrics: DisplayMetrics, dps: Int): Int {
        return (dps * displayMetrics.density).toInt()
    }


    private val pallyconEventListener: PallyconEventListener = object : PallyconEventListener {
        override fun onDrmKeysLoaded(licenseInfo: Map<String, String>) {}
        override fun onDrmSessionManagerError(e: Exception) {
//            Toast.makeText(context, /*e.getMessage()*/ "Error in DRM", Toast.LENGTH_LONG).show();
        }

        override fun onDrmKeysRestored() {}
        override fun onDrmKeysRemoved() {}
    }

    private fun sendAnalaticsData(
        activity: AppCompatActivity,
        userId: String?,
        contentId: String?,
        contentTitle: String?,
        token: String?
    ) {
        val finalAnalaticsUrl = analaticsUrl
        if (finalAnalaticsUrl == null || TextUtils.isEmpty(finalAnalaticsUrl)) return
        if (contentId != null && !TextUtils.isEmpty(contentId) && contentTitle != null && !TextUtils.isEmpty(
                contentTitle
            ) && token != null && !TextUtils.isEmpty(token)
        ) {
            val totalDuration = getDuration()
            val bufferDuration = bufferingTimeInMillis
            val palyedDuration = getContentPlayedTimeInMillis()
            AppSessionUtil.sendHeartBeat(
                activity,
                userId,
                finalAnalaticsUrl,
                contentId,
                contentTitle,
                palyedDuration,
                bufferDuration,
                totalDuration,
                token
            )
        } else {
            VideoPlayerTracer.error(
                "Analatics Error:::", "token or content id or content title is required field."
            )
        }
    }


    companion object {
        const val DEFAULT_FAST_FORWARD_MS = 10000
        const val DEFAULT_REWIND_MS = 10000
        const val DEFAULT_TIMEOUT_MS = 5000
    }

    init {
        trackSelector = DefaultTrackSelector(context)
        formatBuilder = StringBuilder()
        formatter = Formatter(formatBuilder, Locale.getDefault())
        CommonUtils.setDefaultCookieManager()
        val mgr = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        sharedPreferencePlayer = SharedPreferencePlayer()
        sharedPreferencePlayer.setPreferenceInt(context, "pos", 0)
        if (Build.VERSION.SDK_INT >= 31) {
            if (ContextCompat.checkSelfPermission(
                    context, Manifest.permission.READ_PHONE_STATE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                mgr.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE)
            }
        } else {
            mgr.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE)
        }

    }


    enum class GestureType {
        NoGesture, SwipeGesture, DoubleTapGesture
    }

    override fun onCastSessionAvailable() {

    }

    override fun onCastSessionUnavailable() {

    }


    fun onKeyDownEvent() {

        val volume = audioManager?.getStreamVolume(AudioManager.STREAM_MUSIC) as Int
        mMediaPlayer?.audioComponent?.volume = volume.toFloat()

        if (volume < 1) {
            volumeMuteAndUnMuteButton.visibility = View.VISIBLE
            volumeUnMuteButton.visibility = View.GONE
            isVolmueMute = false
        } else {
            volumeMuteAndUnMuteButton.visibility = View.GONE
            volumeUnMuteButton.visibility = View.VISIBLE
            isVolmueMute = true
        }
    }

    fun onKeyUpEvent() {

        val volume = audioManager?.getStreamVolume(AudioManager.STREAM_MUSIC) as Int

        mMediaPlayer?.audioComponent?.volume = volume.toFloat()

        if (volume < 1) {
            volumeMuteAndUnMuteButton.visibility = View.VISIBLE
            volumeUnMuteButton.visibility = View.GONE
            isVolmueMute = false
        } else {
            volumeMuteAndUnMuteButton.visibility = View.GONE
            volumeUnMuteButton.visibility = View.VISIBLE
            isVolmueMute = true
        }
    }


    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (event.keyCode === KeyEvent.KEYCODE_VOLUME_DOWN) {
            volumeMuteAndUnMuteButton.visibility = View.VISIBLE
            volumeUnMuteButton.visibility = View.GONE
            isVolmueMute = false
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        if (event.keyCode === KeyEvent.KEYCODE_VOLUME_UP) {
            volumeMuteAndUnMuteButton.visibility = View.GONE
            volumeUnMuteButton.visibility = View.VISIBLE
            isVolmueMute = true
        }

        return super.onKeyUp(keyCode, event)
    }


    private var countDownTimer1: CountDownTimerWithPause? = null

    private fun setTimerOnVideoPlayer(isShow: Boolean) {


        val tickDuration = 500
        if (isShow) {
            contentRateLayout.visibility = View.VISIBLE
            closeVideoPlayerButton.visibility = GONE
            // overlayImageTransparent.visibility = GONE
            centerButtonLayout.visibility = GONE
            videoMenuLayout.visibility = GONE
            resumedVideoTv.visibility = View.GONE
            previewTimeBar.visibility = GONE
            durationLinearLayout.visibility = GONE
        } else {
            contentRateLayout.visibility = View.GONE
            return
        }


        if (!TextUtils.isEmpty(parentalAge)) {
            contentRatedTv.setText("Rated  " + parentalAge)
            contentRatedTv.visibility = View.VISIBLE
        } else {
            contentRatedTv.visibility = View.GONE
        }

        if (!TextUtils.isEmpty(genure)) {
            genureTv.setText(genure)
            genureTv.visibility = View.VISIBLE
        } else {
            genureTv.visibility = View.GONE
        }


        if (!TextUtils.isEmpty(language)) {
            languageTv.setText("Language : " + language)
            languageTv.visibility = View.VISIBLE
        } else {
            languageTv.visibility = View.GONE
        }

        var timerTotalSec = 0

        if (conentRatingTime > 0) timerTotalSec = conentRatingTime * 1000
        else timerTotalSec = 5 * 1000

        countDownTimer1 = object :
            CountDownTimerWithPause(timerTotalSec.toLong(), (tickDuration).toLong(), true) {
            override fun onTick(millisUntilFinished: Long) {

            }

            override fun onFinish() {
                contentRateLayout.visibility = View.GONE
                countDownTimer1?.cancel()
            }
        }.create()
    }


    fun setUATimeDuration(conentratingTime: Int) {
        this.conentRatingTime = conentratingTime;
    }


    private var parentalAge = ""
    private var genure = ""
    private var language = ""
    private var conentRatingTime = 0
    private lateinit var contentRateLayout: LinearLayoutCompat
    private lateinit var genureTv: TextView
    private lateinit var languageTv: TextView
    private lateinit var contentRatedTv: TextView

    private var isContentRepetPlaying = false

    fun setGenure(genure: String) {
        this.genure = genure
    }


    fun setContentRepeatModeEnabled(repeat: Boolean) {
        this.isContentRepetPlaying = repeat
    }


    fun setLanguage(language: String) {
        this.language = language;
    }

    fun setAgeGroup(age: String) {
        this.parentalAge = age
    }

    fun stopCounter() {
        countDownTimer1?.cancel()
    }


    fun showEpisodeButtonVisiblityListener(size: Int) {
        if (isWebSeries && size > 1)
            epsodeButton.visibility = View.VISIBLE
        else
            epsodeButton.visibility = View.GONE

    }


    fun getSkipButton(): TextView {
        return skipVideoButton;
    }

    fun getCurrentDurationTv(): TextView {
        return exoCurrentPosition;
    }


    fun getTotalDurationTv(): TextView {
        return exoTotalDuration;
    }


    private var isVolmueMute = true


    fun getVolumeStatus(): Boolean {
        return isVolmueMute
    }


}