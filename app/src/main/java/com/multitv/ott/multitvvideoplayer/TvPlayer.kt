package com.multitv.ott.multitvvideoplayer

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Color
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.SystemClock
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.text.TextUtils
import android.util.AttributeSet
import android.util.Log
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.Target
import com.github.rubensousa.previewseekbar.PreviewBar
import com.github.rubensousa.previewseekbar.PreviewLoader
import com.github.rubensousa.previewseekbar.exoplayer.PreviewTimeBar
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.drm.DrmSessionManager
import com.google.android.exoplayer2.ext.ima.ImaAdsLoader
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.source.MediaSourceFactory
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultAllocator
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.google.android.exoplayer2.util.MimeTypes
import com.google.android.exoplayer2.util.Util
import com.google.android.exoplayer2.video.VideoSize
import com.google.common.collect.ImmutableList
import com.multitv.ott.multitvvideoplayer.cast.CastPlayer
import com.multitv.ott.multitvvideoplayer.cast.SessionAvailabilityListener
import com.multitv.ott.multitvvideoplayer.custom.CountDownTimerWithPause
import com.multitv.ott.multitvvideoplayer.database.SharedPreferencePlayer
import com.multitv.ott.multitvvideoplayer.fabbutton.FabButton
import com.multitv.ott.multitvvideoplayer.listener.VideoPlayerErrorListener
import com.multitv.ott.multitvvideoplayer.listener.VideoPlayerSdkCallBackListener
import com.multitv.ott.multitvvideoplayer.playerglide.GlideThumbnailTransformation
import com.multitv.ott.multitvvideoplayer.popup.TrackSelectionDialog
import com.multitv.ott.multitvvideoplayer.touchevent.OnSwipeTouchListener
import com.multitv.ott.multitvvideoplayer.utils.*
import com.multitv.ott.multitvvideoplayer.videoplayer.MyVideoPlayer
import com.pallycon.widevinelibrary.*
import java.util.*

class TvPlayer(
    private val context: AppCompatActivity,
    attrs: AttributeSet?,
    defStyleAttr: Int
) : FrameLayout(
    context, attrs, defStyleAttr
), PreviewLoader, PreviewBar.OnScrubListener, View.OnClickListener, SessionAvailabilityListener {


    constructor(context: Context, attrs: AttributeSet?) : this(
        context as AppCompatActivity,
        attrs,
        0
    ) {
    }


    private val sharedPreferencePlayer: SharedPreferencePlayer
    private var contentType: ContentType? = null
    private var mMediaPlayer: ExoPlayer? = null
    private var simpleExoPlayerView: StyledPlayerView? = null
    private var trackSelector: DefaultTrackSelector
    private var videoPlayerSdkCallBackListener: VideoPlayerSdkCallBackListener? = null
    private var videoPlayerErrorListener: VideoPlayerErrorListener? = null
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
    private var errorRetryLayout: LinearLayout? = null
    private var videoMenuLayout: LinearLayout? = null
    private var durationlayout: LinearLayout? = null
    private var videoProgressLayout: LinearLayout? = null
    private var bufferingProgressBarLayout: LinearLayout? = null
    private var circularProgressLayout: LinearLayout? = null
    private var centerButtonLayout: LinearLayout? = null
    private var pictureInPicture: ImageView? = null
    private var previewImageView: ImageView? = null
    private var videoLockButton: ImageView? = null
    private var videoUnLockButton: ImageView? = null
    private var setting: ImageView? = null
    private var videoRotationButton: ImageView? = null
    private var videoPerviousButton: ImageView? = null
    private var videoNextButton: ImageView? = null
    private var VideoRenuButton: ImageView? = null
    private var videoFarwardButton: ImageView? = null
    private var videoPlayButton: ImageView? = null
    private var videoPauseButton: ImageView? = null
    private var playerProgress: PreviewTimeBar? = null
    private var currentDurationPlayTv: TextView? = null
    private var previewFrameLayout: FrameLayout? = null

    private lateinit var closeButton: ImageView

    private lateinit var frameLayout: RelativeLayout

    private val formatBuilder: StringBuilder
    private val formatter: Formatter
    private var isDrmContent = false
    private var drmContentToken: String? = null
    private var drmdrmLicenseUrl: String? = null
    private var siteId: String? = null
    private var siteKey: String? = null
    private var drmSessionManager: DrmSessionManager? = null
    private var adsLoader: ImaAdsLoader? = null
    private var adsUrl: String? = null
    private var isScreenLockEnable = false
    private var isControllerShown = false
    private var isAttachedToWindowStatus = false
    private var hideAtMs: Long = 0

    private var mGestureType = GestureType.NoGesture

    private var mWindow: Window? = null

    private lateinit var progressBarParent: FrameLayout
    private lateinit var volumeProgressBar: ProgressBar
    private lateinit var brightnessProgressBar: ProgressBar


    private var mCastPlayer: CastPlayer? = null

    @SuppressLint("MissingInflatedId")
    override fun onFinishInflate() {
        val view = LayoutInflater.from(getContext()).inflate(R.layout.player_layout, this)
        progressBarParent = view.findViewById(R.id.progress_bar_parent)
        volumeProgressBar = view.findViewById(R.id.volume_progress_bar)
        brightnessProgressBar = view.findViewById(R.id.brightness_progress_bar)
        errorRetryLayout = view.findViewById(R.id.errorRetryLayout)
        durationlayout = view.findViewById(R.id.durationlayout)
        videoMenuLayout = view.findViewById(R.id.videoMenuLayout)
        bufferingProgressBarLayout = view.findViewById(R.id.bufferingProgressBarLayout)
        circularProgressLayout = view.findViewById(R.id.circularProgressLayout)
        videoProgressLayout = findViewById(R.id.video_progress_layout)
        setting = view.findViewById(R.id.settings_btn)

        closeButton = view.findViewById(R.id.closeButton)

        previewFrameLayout = view.findViewById(R.id.previewFrameLayout)
        setting?.setOnClickListener(this)
        centerButtonLayout = view.findViewById(R.id.centerButtonLayout)
        videoPerviousButton = view.findViewById(R.id.exo_prev)
        videoNextButton = view.findViewById(R.id.exo_next)
        VideoRenuButton = view.findViewById(R.id.exo_rew)
        videoFarwardButton = view.findViewById(R.id.exo_ffwd)
        videoPlayButton = view.findViewById(R.id.exo_play)
        videoPauseButton = view.findViewById(R.id.exo_pause)
        videoLockButton = view.findViewById(R.id.exo_lock)
        videoUnLockButton = view.findViewById(R.id.exo_unlock)
        playerProgress = findViewById<View>(R.id.exo_progress) as PreviewTimeBar
        currentDurationPlayTv = view.findViewById(R.id.exo_position)
        previewImageView = view.findViewById(R.id.previewImageView)
        simpleExoPlayerView = view.findViewById(R.id.videoPlayer)
        videoRotationButton = view.findViewById(R.id.enter_full_screen)
        playerProgress!!.addOnScrubListener(this)
        playerProgress!!.setPreviewLoader(this)
        pictureInPicture = view.findViewById(R.id.picture_in_picture)
        videoNextButton?.setVisibility(GONE)
        videoPerviousButton?.setVisibility(GONE)
        pictureInPicture?.setVisibility(GONE)
        videoRotationButton?.setVisibility(GONE)
        frameLayout = view.findViewById(R.id.frameLayout)


        closeButton.setOnClickListener {
            context.finish()
        }

        playerProgress!!.setAdMarkerColor(Color.argb(0x00, 0xFF, 0xFF, 0xFF))
        playerProgress!!.setPlayedAdMarkerColor(Color.argb(0x98, 0xFF, 0xFF, 0xFF))
        videoRotationButton?.setOnClickListener(OnClickListener {
            val orientation = getContext().resources.configuration.orientation
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                (getContext() as Activity).requestedOrientation =
                    ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                (getContext() as Activity).window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
                showSystemBar()
                videoRotationButton?.setImageResource(R.drawable.rotate)
                videoLockButton?.setVisibility(GONE)
                videoUnLockButton?.setVisibility(GONE)
            } else if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                (getContext() as Activity).requestedOrientation =
                    ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                (getContext() as Activity).window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
                hideSystemBars()
                videoRotationButton?.setImageResource(R.drawable.ic_minimize)
                videoLockUnlockStatus()
            }
        })

        frameLayout.setOnClickListener {
            if (isControllerShown)
                hideController()
            else
                showController()
        }




        findViewById<View>(R.id.speed_btn)?.setOnClickListener { showSpeedControlDailog() }
        // simpleExoPlayerView.set
        errorRetryLayout?.setOnClickListener(OnClickListener {
            errorRetryLayout?.setVisibility(GONE)
            initializeMainPlayer(mContentUrl, true, mMediaPlayer)
        })
        videoUnLockButton?.setOnClickListener(OnClickListener {
            isScreenLockEnable = false
            videoUnLockButton?.setVisibility(GONE)
            videoLockButton?.setVisibility(VISIBLE)
            showController()
        })
        videoLockButton?.setOnClickListener(OnClickListener {
            isScreenLockEnable = true
            videoUnLockButton?.setVisibility(VISIBLE)
            videoLockButton?.setVisibility(GONE)
            hideController()
        })
        pictureInPicture?.setOnClickListener(OnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                context.enterPictureInPictureMode()
            }
        })
        VideoRenuButton?.setOnClickListener(OnClickListener {
            Toast.makeText(context, "vvvvvvvvvvvvv", Toast.LENGTH_SHORT).show()
            rewind()
        })
        videoFarwardButton?.setOnClickListener(OnClickListener { fastForward() })
        videoPlayButton?.setOnClickListener(OnClickListener {
            if (mMediaPlayer != null) {
                mMediaPlayer!!.playWhenReady = true
                videoPlayButton?.setVisibility(GONE)
                videoPauseButton?.setVisibility(VISIBLE)
            }
        })
        videoPauseButton?.setOnClickListener(OnClickListener {
            if (mMediaPlayer != null) {
                mMediaPlayer!!.playWhenReady = false
                videoPlayButton?.setVisibility(VISIBLE)
                videoPauseButton?.setVisibility(GONE)
            }
        })
        playerProgress!!.addOnPreviewVisibilityListener { previewBar, isPreviewShowing ->
            Log.d(
                "PreviewShowing::::",
                isPreviewShowing.toString()
            )
        }


        super.onFinishInflate()
    }

    fun setCastSessionAvailabilityListener(sessionAvailabilityListener: SessionAvailabilityListener) {
        if (mCastPlayer != null) {
            mCastPlayer!!.setSessionAvailabilityListener(sessionAvailabilityListener)
        }
    }

    fun setCastPlayer(castPlayer: CastPlayer) {
        if (mCastPlayer == null) {
            mCastPlayer = castPlayer
        }
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
        VideoPlayerTracer.error("Controller Listener:::", "Stop Timer")
        hideController()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        val orientation = resources.configuration.orientation
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            videoLockUnlockStatus()
        } else if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            videoLockButton!!.visibility = GONE
            videoUnLockButton!!.visibility = GONE
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
        centerButtonLayout!!.visibility = GONE
        videoProgressLayout!!.visibility = GONE
        durationlayout!!.visibility = GONE
        videoMenuLayout!!.visibility = GONE
        removeCallbacks(hideAction)
        hideAtMs = C.TIME_UNSET
        isControllerShown = false
    }

    fun showController() {
        if (!isScreenLockEnable) {
            centerButtonLayout!!.visibility = VISIBLE
            videoProgressLayout!!.visibility = VISIBLE
            durationlayout!!.visibility = VISIBLE
            videoMenuLayout!!.visibility = VISIBLE
        }
        updatePlayPauseButton()
        hideAfterTimeout()
        isControllerShown = true
    }

    private fun updatePlayPauseButton() {
        var requestPlayPauseFocus = false
        val playing = mMediaPlayer != null && mMediaPlayer!!.playWhenReady
        if (videoPlayButton != null) {
            requestPlayPauseFocus =
                requestPlayPauseFocus or (playing && videoPlayButton!!.isFocused)
            videoPlayButton!!.visibility =
                if (playing) GONE else VISIBLE
        }
        if (videoPauseButton != null) {
            requestPlayPauseFocus =
                requestPlayPauseFocus or (!playing && videoPauseButton!!.isFocused)
            videoPauseButton!!.visibility =
                if (!playing) GONE else VISIBLE
        }
        if (requestPlayPauseFocus) {
            requestPlayPauseFocus()
        }
    }

    private fun requestPlayPauseFocus() {
        val playing = mMediaPlayer != null && mMediaPlayer!!.playWhenReady
        if (!playing && videoPlayButton != null) {
            videoPlayButton!!.requestFocus()
        } else if (playing && videoPauseButton != null) {
            videoPauseButton!!.requestFocus()
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
        if (contentType == null || mContentUrl == null) throw Exception("Content type must not be null")
        initViews()
    }

    private fun videoLockUnlockStatus() {
        if (isScreenLockEnable) {
            videoLockButton!!.visibility = VISIBLE
            videoUnLockButton!!.visibility = GONE
        } else {
            videoLockButton!!.visibility = GONE
            videoUnLockButton!!.visibility = VISIBLE
        }
    }

    // init view and view group here
    private fun initViews() {

        trackSelector = DefaultTrackSelector(context)
        audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        if (videoPlayerSdkCallBackListener != null) videoPlayerSdkCallBackListener!!.onPlayerReady(
            mContentUrl
        )

        mWindow = context.window
    }

    fun setMultiTvVideoPlayerSdkListener(videoPlayerSdkCallBackListener: VideoPlayerSdkCallBackListener?) {
        this.videoPlayerSdkCallBackListener = videoPlayerSdkCallBackListener
    }

    fun setMultiTvErrorListenr(errorListener: VideoPlayerErrorListener) {
        this.videoPlayerErrorListener = errorListener;
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
        initializeMainPlayer(mContentUrl, true, mMediaPlayer)
    }

    // resume video player
    fun resumeVideoPlayer() {
        if (mMediaPlayer != null && simpleExoPlayerView != null) {
            simpleExoPlayerView!!.onResume()
            mMediaPlayer!!.playWhenReady = true
        }
    }

    // pause video player
    fun pauseVideoPlayer() {
        if (mMediaPlayer != null && simpleExoPlayerView != null) {
            simpleExoPlayerView!!.onPause()
            mMediaPlayer!!.playWhenReady = false
        }
    }

    // relase and destroy video player
    fun releaseVideoPlayer() {
        if (mMediaPlayer != null && simpleExoPlayerView != null) {
            sendAnalaticsData(context, userId, contentId, contentTitle, token)
            simpleExoPlayerView!!.player!!.release()
            mMediaPlayer!!.release()
            if (adsLoader != null) adsLoader!!.setPlayer(null)
        }
    }


    private fun initializeMainPlayer(
        videoUrl: String?,
        isNeedToPlayInstantly: Boolean,
        currentPlayer: Player?
    ) {
//        ToastMessage.showLogs(ToastMessage.LogType.ERROR, "Video Player:::", "initializeMainPlayer");
        if (currentPlayer != null) {
            currentPlayer!!.release()
            if (adsLoader != null) adsLoader!!.setPlayer(null)
//            mMediaPlayer = null
        }
        centerButtonLayout!!.visibility = GONE
        videoPlayerSdkCallBackListener!!.prepareVideoPlayer()
        //        ToastMessage.showLogs(ToastMessage.LogType.DEBUG, TAG, "Content url is " + videoUrl);
        val customLoadControl: LoadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(1000, 50000, 1000, 1)
            .setAllocator(DefaultAllocator(true, 32 * 1024))
            .setTargetBufferBytes(C.LENGTH_UNSET)
            .setPrioritizeTimeOverSizeThresholds(true)
            .setBackBuffer(0, false)
            .build()


        // start

//        simpleExoPlayerView!!.player = currentPlayer
//        simpleExoPlayerView!!.controllerHideOnTouch = currentPlayer === mMediaPlayer
//
//        if (currentPlayer === mCastPlayer && mCastPlayer != null) {
//            simpleExoPlayerView!!.controllerShowTimeoutMs = 0
//            simpleExoPlayerView!!.showController()
//            simpleExoPlayerView!!.defaultArtwork = ResourcesCompat.getDrawable(
//                context.resources,
//                R.drawable.ic_baseline_cast_24,  /* theme= */
//                null
//            )
//        } else { // currentPlayer == localPlayer
//            simpleExoPlayerView!!.controllerShowTimeoutMs = StyledPlayerControlView.DEFAULT_SHOW_TIMEOUT_MS
//            simpleExoPlayerView!!.defaultArtwork = null
//        }

//        mMediaPlayer = currentPlayer

        if (adsUrl != null && !TextUtils.isEmpty(adsUrl)) {
            val dataSourceFactory: DataSource.Factory = DefaultDataSource.Factory(
                context
            )
            val mediaSourceFactory: MediaSourceFactory =
                DefaultMediaSourceFactory(dataSourceFactory)
                    .setAdsLoaderProvider { unusedAdTagUri: MediaItem.AdsConfiguration? ->
                        adsLoader
                    }
                    .setAdViewProvider(simpleExoPlayerView)


            mMediaPlayer = ExoPlayer.Builder(context).setMediaSourceFactory(mediaSourceFactory)
                .setTrackSelector(trackSelector).setLoadControl(customLoadControl).build()
            adsLoader = ImaAdsLoader.Builder( /* context= */context).build()
        } else {
            mMediaPlayer = ExoPlayer.Builder(context).setTrackSelector(trackSelector)
                .setLoadControl(customLoadControl).build()
        }
        if (mMediaPlayer != null) {
            mMediaPlayer!!.addListener(stateChangeCallback1)
            simpleExoPlayerView!!.player = mMediaPlayer
            simpleExoPlayerView!!.controllerHideOnTouch = true
            simpleExoPlayerView!!.controllerAutoShow = false
            simpleExoPlayerView!!.controllerShowTimeoutMs = DEFAULT_TIMEOUT_MS
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
                        drmSchemeUuid,
                        drmdrmLicenseUrl,
                        uri,
                        drmContentToken
                    )
                } catch (e: PallyconDrmException) {
                    e.printStackTrace()
                }
                val playerMediaSource = ExoUttils().buildMediaSource(
                    context,
                    mediaItem,
                    videoUrl!!,
                    drmSessionManager!!
                )
                mMediaPlayer!!.setMediaSource(playerMediaSource!!)
            } else {
                mediaItem = if (subtitle != null) {
                    /*MediaSource playerMediaSource = new ExoUttils().buildMediaSource(context, mediaItem, videoUrl, drmSessionManager);
                    MediaSource mediaSource = new MergingMediaSource(mediaSources);
                    mMediaPlayer.setMediaSource(mediaSource);*/
                    if (adsUrl != null && !TextUtils.isEmpty(adsUrl)) {
                        adsLoader!!.setPlayer(mMediaPlayer)
                        val adTagUri = Uri.parse(adsUrl)
                        MediaItem.Builder()
                            .setSubtitleConfigurations(ImmutableList.of(subtitle))
                            .setUri(videoUrl)
                            .setAdsConfiguration(
                                MediaItem.AdsConfiguration.Builder(adTagUri).build()
                            )
                            .build()
                    } else {
                        MediaItem.Builder()
                            .setSubtitleConfigurations(ImmutableList.of(subtitle))
                            .setUri(videoUrl)
                            .build()
                    }
                } else {
                    if (adsUrl != null && !TextUtils.isEmpty(adsUrl)) {
                        adsLoader!!.setPlayer(mMediaPlayer)
                        //adsLoader.focusSkipButton();
                        val adTagUri = Uri.parse(adsUrl)
                        MediaItem.Builder()
                            .setUri(videoUrl)
                            .setAdsConfiguration(
                                MediaItem.AdsConfiguration.Builder(adTagUri).build()
                            )
                            .build()
                    } else {
                        MediaItem.Builder()
                            .setUri(videoUrl)
                            .build()
                    }
                }
                mMediaPlayer!!.setMediaItem(mediaItem)
            }
            mMediaPlayer!!.prepare()
            if (isNeedToPlayInstantly) {
                mMediaPlayer!!.playWhenReady = true
            }
        }
    }

    var stateChangeCallback1: Player.Listener = object : Player.Listener {
        override fun onPlayerError(error: PlaybackException) {
            super.onPlayerError(error)
            if (mMediaPlayer != null && mMediaPlayer!!.currentPosition != 0L) seekPlayerTo =
                mMediaPlayer!!.currentPosition
                    .toInt() / 1000
//            errorRetryLayout!!.bringToFront()
//            errorRetryLayout!!.visibility = VISIBLE
            videoPlayerSdkCallBackListener!!.onPlayerError(error.message)

            if (error.errorCode != 200)
                videoPlayerErrorListener?.onErrorEvent(false)
            else
                videoPlayerErrorListener?.onErrorEvent(true)
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
            when (playbackState) {
                ExoPlayer.STATE_BUFFERING -> {
                    text += "buffering"
                    bufferingProgressBarLayout!!.bringToFront()
                    bufferingProgressBarLayout!!.visibility = VISIBLE
                    centerButtonLayout!!.visibility = GONE
                    if (contentType == ContentType.LIVE) startBufferingTimer()
                }
                ExoPlayer.STATE_ENDED -> {
                    text += "ended"
                    if (contentType == ContentType.VOD) {
                        if (mMediaPlayer != null) contentPlayedTimeInMillis =
                            mMediaPlayer!!.currentPosition
                        releaseVideoPlayer()
                        bufferingProgressBarLayout!!.visibility = GONE
                        videoPlayerSdkCallBackListener?.onPlayNextVideo()
                    }
                }
                ExoPlayer.STATE_IDLE -> {
                    text += "idle"
                    if (!checkForAudioFocus()) return
                    if (bufferingProgressBarLayout != null) bufferingProgressBarLayout!!.visibility =
                        GONE
                    centerButtonLayout!!.visibility = VISIBLE
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
                    bufferingProgressBarLayout!!.visibility = GONE
                    centerButtonLayout!!.visibility = VISIBLE
                    videoNextButton!!.visibility = GONE
                    videoPerviousButton!!.visibility = GONE
                }
                else -> text += "unknown"
            }

//            ToastMessage.showToastMsg(context, text, Toast.LENGTH_SHORT);
//            ToastMessage.showLogs(ToastMessage.LogType.ERROR, "Video Player:::", text);
        }

        override fun onRepeatModeChanged(repeatMode: Int) {}
        override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters) {}
    }


    private fun startBufferingTimer() {
        if (bufferingTimeHandler == null) {
            bufferingTimeHandler = Handler()
        }
        if (bufferingTimeRunnable != null) bufferingTimeHandler!!.postDelayed(
            bufferingTimeRunnable,
            0
        )
    }

    fun stopBufferingTimer() {
        if (bufferingTimeHandler != null && bufferingTimeRunnable != null) {
            bufferingTimeHandler!!.removeCallbacks(bufferingTimeRunnable)
            bufferingTimeHandler!!.removeCallbacksAndMessages(null)
        }
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
            if (!isShowingTrackSelectionDialog
                && TrackSelectionDialog.willHaveContent(trackSelector)
            ) {
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
        val uiOptions = (SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or SYSTEM_UI_FLAG_FULLSCREEN)
        decorView.systemUiVisibility = uiOptions
    }

    private fun showSystemBar() {
        val decorView = (getContext() as Activity).window.decorView
        val uiOptions = SYSTEM_UI_FLAG_VISIBLE
        decorView.systemUiVisibility = uiOptions
    }

    private fun rewind() {
        seekTo(Math.max(mMediaPlayer!!.currentPosition - DEFAULT_REWIND_MS, 0))
    }

    private fun fastForward() {
        seekTo(Math.max(mMediaPlayer!!.currentPosition + DEFAULT_FAST_FORWARD_MS, 0))
    }

    private fun seekTo(positionMs: Long) {
        mMediaPlayer!!.seekTo(positionMs)
    }

    val duration: Long
        get() = if (mMediaPlayer != null) mMediaPlayer!!.duration else 0
    val currentPosition: Long
        get() = if (mMediaPlayer != null) mMediaPlayer!!.currentPosition else 0

    fun resumeFromPosition(millisecondsForResume: Long) {
        if (millisecondsForResume != 0L) {
            this.millisecondsForResume = millisecondsForResume
            //isResumeFromPreviousPosition = true;
        }
    }

    override fun onScrubStart(previewBar: PreviewBar) {
        //findViewById(R.id.centerButtonLayout)?.setVisibility(View.GONE);
        previewFrameLayout!!.visibility = VISIBLE
        pauseVideoPlayer()
        removeCallbacks(hideAction)
    }

    override fun onScrubMove(previewBar: PreviewBar, progress: Int, fromUser: Boolean) {
        //findViewById(R.id.centerButtonLayout)?.setVisibility(View.GONE);
        previewFrameLayout!!.visibility = VISIBLE
        if (currentDurationPlayTv != null) {
            currentDurationPlayTv!!.text = Util.getStringForTime(
                formatBuilder,
                formatter,
                progress.toLong()
            )
        }
    }

    override fun onScrubStop(previewBar: PreviewBar) {
        previewFrameLayout!!.visibility = GONE
        //findViewById(R.id.centerButtonLayout)?.setVisibility(View.VISIBLE);
        if (mMediaPlayer != null) {
            seekTo(previewBar.progress.toLong())
        }
        resumeVideoPlayer()
    }

    override fun loadPreview(currentPosition: Long, max: Long) {
        pauseVideoPlayer()
        Glide.with(previewImageView!!)
            .load("http://103.253.175.13/rahuls/output-160x90-thumb-001.jpg")
            .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
            .transform(GlideThumbnailTransformation(currentPosition,1000))
            .into(previewImageView!!)
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
            val totalDuration = duration
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
                "Analatics Error:::",
                "token or content id or content title is required field."
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
                    context,
                    Manifest.permission.READ_PHONE_STATE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                mgr.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE)
            }
        } else {
            mgr.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE)
        }

    }


/*
    private var clickFrameSwipeListener = object : OnSwipeTouchListener(true) {
        // mGestureType=
        var diffTime = -1f
        var finalTime = -1f
        var startVolume: Int = 0
        var maxVolume: Int = 0
        var startBrightness: Int = 0
        var maxBrightness: Int = 0

        override fun onMove(dir: OnSwipeTouchListener.Direction, diff: Float) {
            finalTime = -1f

            if (initialX >= mInitialTextureWidth / 2 || mWindow == null) {
                // Right side swipe when up and down

                var diffVolume: Float
                var finalVolume: Int

                diffVolume = maxVolume.toFloat() * diff / (mInitialTextureHeight.toFloat() / 2)
                if (dir == OnSwipeTouchListener.Direction.DOWN) {
                    diffVolume = -diffVolume
                }
                finalVolume = startVolume + diffVolume.toInt()
                if (finalVolume < 0)
                    finalVolume = 0
                else if (finalVolume > maxVolume)
                    finalVolume = maxVolume

                */
/*val progressText = String.format(
                    resources.getString(R.string.volume), finalVolume
                )*//*

                // mPositionTextView.text = progressText
                volumeProgressBar.progress = finalVolume
                audioManager?.setStreamVolume(AudioManager.STREAM_MUSIC, finalVolume, 0)

            } else if (initialX < mInitialTextureWidth / 2) {
                // Left side swipe when up and down

                var diffBrightness: Float
                var finalBrightness: Int

                diffBrightness =
                    maxBrightness.toFloat() * diff / (mInitialTextureHeight.toFloat() / 2)
                if (dir == OnSwipeTouchListener.Direction.DOWN) {
                    diffBrightness = -diffBrightness
                }
                finalBrightness = startBrightness + diffBrightness.toInt()
                if (finalBrightness < 0)
                    finalBrightness = 0
                else if (finalBrightness > maxBrightness)
                    finalBrightness = maxBrightness


                val layout = mWindow?.attributes
                layout?.screenBrightness = finalBrightness.toFloat() / 100
                mWindow?.attributes = layout


                brightnessProgressBar.progress = finalBrightness


                */
/*PreferenceManager.getDefaultSharedPreferences(context)
                    .edit()
                    .putInt(BETTER_VIDEO_PLAYER_BRIGHTNESS, finalBrightness)
                    .apply()*//*

            }
        }

        override fun onClick() {
            if (isControllerShown)
                hideController()
            else
                showController()
        }

        override fun onDoubleTap(event: MotionEvent) {
*/
/*
            if (mGestureType == GestureType.DoubleTapGesture) {
                val seekSec = mDoubleTapSeekDuration / 1000
                viewForward.text = String.format(resources.getString(R.string.seconds), seekSec)
                viewBackward.text = String.format(resources.getString(R.string.seconds), seekSec)
                if (event.x > mInitialTextureWidth / 2) {
                    viewForward.let {
                        animateViewFade(it, 1)
                        Handler().postDelayed({
                            animateViewFade(it, 0)
                        }, 500)
                    }
                    seekTo(getCurrentPosition() + mDoubleTapSeekDuration)
                } else {
                    viewBackward.let {
                        animateViewFade(it, 1)
                        Handler().postDelayed({
                            animateViewFade(it, 0)
                        }, 500)
                    }
                    seekTo(getCurrentPosition() - mDoubleTapSeekDuration)
                }
            }
*//*

        }

        override fun onAfterMove() {
            */
/* if (finalTime >= 0 && mGestureType == GestureType.SwipeGesture) {
                 seekTo(finalTime.toLong())*//*

            //if (mWasPlaying) mPlayer?.start()
            //  }
            //  mPositionTextView.visibility = View.GONE

            volumeProgressBar.visibility = View.GONE
            brightnessProgressBar.visibility = View.GONE
            resumeVideoPlayer()
            hideController()

        }

        override fun onBeforeMove(dir: Direction) {
//            if (mGestureType != GestureType.SwipeGesture)
//                return
//            if (dir == Direction.LEFT || dir == Direction.RIGHT) {
//                val playing = mMediaPlayer != null && mMediaPlayer!!.playWhenReady
//                if (playing)
//                    pauseVideoPlayer()
//            } else {
//                maxBrightness = 100
//                startBrightness = (mWindow?.attributes?.screenBrightness!! * 100).toInt()
//                maxVolume = audioManager?.getStreamMaxVolume(AudioManager.STREAM_MUSIC) ?: 100
//                startVolume = audioManager?.getStreamVolume(AudioManager.STREAM_MUSIC) ?: 100
//
//                volumeProgressBar.max = maxVolume
//                brightnessProgressBar.max = maxBrightness
//            }


            if (initialX >= mInitialTextureWidth / 2 || mWindow == null) {
                // Right side swipe when up and down
                volumeProgressBar.visibility = View.VISIBLE
                volumeProgressBar.progress = startVolume
            } else if (initialX < mInitialTextureWidth / 2) {
                // Left side swipe when up and down
                brightnessProgressBar.visibility = View.VISIBLE
                brightnessProgressBar.progress = startBrightness
            }


            maxBrightness = 100
            startBrightness = (mWindow?.attributes?.screenBrightness!! * 100).toInt()
            maxVolume = audioManager?.getStreamMaxVolume(AudioManager.STREAM_MUSIC) ?: 100
            startVolume = audioManager?.getStreamVolume(AudioManager.STREAM_MUSIC) ?: 100

            volumeProgressBar.max = maxVolume
            brightnessProgressBar.max = maxBrightness

            hideController()

        }
    }
*/

    enum class GestureType {
        NoGesture, SwipeGesture, DoubleTapGesture
    }

    override fun onCastSessionAvailable() {

    }

    override fun onCastSessionUnavailable() {

    }

}