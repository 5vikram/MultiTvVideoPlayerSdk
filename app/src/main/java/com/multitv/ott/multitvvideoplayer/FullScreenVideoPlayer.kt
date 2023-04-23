package com.multitv.ott.multitvvideoplayer

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.PictureInPictureParams
import android.app.RemoteAction
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
import android.support.v4.media.session.MediaSessionCompat
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.text.TextUtils
import android.util.AttributeSet
import android.util.Log
import android.util.Rational
import android.view.*
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.Target
import com.github.rubensousa.previewseekbar.PreviewBar
import com.github.rubensousa.previewseekbar.PreviewLoader
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
import com.multitv.ott.multitvvideoplayer.cast.SessionAvailabilityListener
import com.multitv.ott.multitvvideoplayer.custom.CountDownTimerWithPause
import com.multitv.ott.multitvvideoplayer.database.SharedPreferencePlayer
import com.multitv.ott.multitvvideoplayer.fabbutton.FabButton
import com.multitv.ott.multitvvideoplayer.listener.VideoPlayerSdkCallBackListener
import com.multitv.ott.multitvvideoplayer.playerglide.GlideThumbnailTransformation
import com.multitv.ott.multitvvideoplayer.popup.TrackSelectionDialog
import com.multitv.ott.multitvvideoplayer.utils.*
import com.pallycon.widevinelibrary.*
import java.util.*

class FullScreenVideoPlayer(
    private val context: AppCompatActivity,
    attrs: AttributeSet?,
    defStyleAttr: Int
) : FrameLayout(
    context, attrs, defStyleAttr
), View.OnClickListener, SessionAvailabilityListener {
    private val sharedPreferencePlayer: SharedPreferencePlayer
    private var contentType: ContentType? = null
    private var mMediaPlayer: ExoPlayer? = null
    private var simpleExoPlayerView: StyledPlayerView? = null
    private var trackSelector: DefaultTrackSelector
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


    private lateinit var errorRetryLayout: LinearLayout
    private lateinit var videoMenuLayout: RelativeLayout
    private lateinit var resumedVideoTv: TextView
    private lateinit var volumeLayout: LinearLayout
    private lateinit var volumeLinearLayout: LinearLayout

    //  private lateinit var videoProgressLayout: LinearLayoutCompat
    private lateinit var bufferingProgressBarLayout: LinearLayout
    private lateinit var circularProgressLayout: LinearLayout
    private lateinit var overlayImageTransparent: View
    private lateinit var circularProgressRing: FabButton
    private lateinit var centerButtonLayout: LinearLayout
    private lateinit var pictureInPicture: ImageView
    private lateinit var previewImageView: ImageView
    private lateinit var videoLockButton: ImageView
    private lateinit var videoUnLockButton: ImageView
    private lateinit var volumeMuteAndUnMuteButton: ImageView
    private lateinit var volumeUnMuteButton: ImageView
    private lateinit var closeVideoPlayerButton: ImageView
    private lateinit var setting: ImageView
    private lateinit var videoRotationButton: ImageView
    private lateinit var videoPerviousButton: ImageView
    private lateinit var videoNextButton: ImageView
    private lateinit var VideoRenuButton: ImageView
    private lateinit var videoFarwardButton: ImageView
    private lateinit var videoPlayButton: ImageView
    private lateinit var videoPauseButton: ImageView
    private lateinit var previewTimeBar: DefaultTimeBar
    private lateinit var currentDurationPlayTv: TextView
    private lateinit var previewFrameLayout: FrameLayout
    private lateinit var videoTitle: TextView
    private lateinit var epsodeButton: ImageView
    private lateinit var epsodeNextButton: ImageView
    private lateinit var durationLinearLayout: LinearLayoutCompat
    private lateinit var repeatVideoLinearLayout: LinearLayout

    // private lateinit var seekBarLayout: ConstraintLayout
    private var videoControllerLayout: ConstraintLayout? = null
    private lateinit var exoRewLinearLayout: LinearLayout
    private lateinit var exoFfwdLinearLayout: LinearLayout


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
    private var isScreenLockEnable = false
    private var isControllerShown = false
    private var isAttachedToWindowStatus = false
    private var hideAtMs: Long = 0

    private var mGestureType = GestureType.NoGesture

    private var mWindow: Window? = null

    private var isWebSeries = false;
    private var userSubscriptionDtatus = false
    private var contentAccessType = ""


    @RequiresApi(Build.VERSION_CODES.O)
    private val mPictureInPictureParamsBuilder = PictureInPictureParams.Builder()
    private var spriteImageUrl = ""


    //  private lateinit var progressBarParent: FrameLayout
    private lateinit var volumeProgressBar: SeekBar
    //  private lateinit var brightnessProgressBar: ProgressBar

    private var isPipModeOn = false


    constructor(context: Context, attrs: AttributeSet?) : this(
        context as AppCompatActivity,
        attrs,
        0
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
            LayoutInflater.from(getContext()).inflate(R.layout.full_screen_video_player, this)

        epsodeButton = view.findViewById(R.id.epsodeButton)
        epsodeNextButton = view.findViewById(R.id.epsodeNextButton)

        repeatVideoLinearLayout = view.findViewById(R.id.repeatVideoLinearLayout)

        epsodeButton.setOnClickListener {
            videoPlayerSdkCallBackListener?.showEpisodeListData()
        }

        epsodeNextButton.setOnClickListener {
            if (userSubscriptionDtatus)
                videoPlayerSdkCallBackListener?.onPlayNextVideo()
            else if (contentAccessType.equals("paid") && !userSubscriptionDtatus)
                videoPlayerSdkCallBackListener?.subscriptionCallBack()
            else if (contentAccessType.equals("free"))
                videoPlayerSdkCallBackListener?.onPlayNextVideo()
            else
                videoPlayerSdkCallBackListener?.showThumbnailCallback()
        }


        contentRateLayout = view.findViewById(R.id.contentRateLayout)
        contentRatedTv = view.findViewById(R.id.contentRatedTv)
        languageTv = view.findViewById(R.id.languageTv)
        genureTv = view.findViewById(R.id.genureTv)
        videoTitle = view.findViewById(R.id.videoTitle)

        volumeMuteAndUnMuteButton = view.findViewById(R.id.volumeMuteAndUnMuteButton)
        overlayImageTransparent = view.findViewById(R.id.overlayImageTransparent)

        resumedVideoTv = view.findViewById(R.id.resumedVideoTv)
        //   progressBarParent = view.findViewById(R.id.progress_bar_parent)
        volumeProgressBar = view.findViewById(R.id.exo_volume_progress)
        //  brightnessProgressBar = view.findViewById(R.id.brightness_progress_bar)
        errorRetryLayout = view.findViewById(R.id.errorRetryLayout)
        videoMenuLayout = view.findViewById(R.id.videoMenuLayout)
        volumeUnMuteButton = view.findViewById(R.id.volumeUnMuteButton)
        bufferingProgressBarLayout = view.findViewById(R.id.bufferingProgressBarLayout)
        circularProgressLayout = view.findViewById(R.id.circularProgressLayout)
        setting = view.findViewById(R.id.settings_btn)
        volumeLayout = view.findViewById(R.id.volumeLayout)
        volumeLinearLayout = view.findViewById(R.id.volumeLinearLayout)

        videoControllerLayout = view.findViewById(R.id.videoControllerLayout)
        previewFrameLayout = view.findViewById(R.id.previewFrameLayout)
        durationLinearLayout = view.findViewById(R.id.durationLinearLayout)

        exoFfwdLinearLayout = view.findViewById(R.id.exoFfwdLinearLayout)
        exoRewLinearLayout = view.findViewById(R.id.exoRewLinearLayout)

        setting.setOnClickListener(this)
        centerButtonLayout = view.findViewById(R.id.centerButtonLayout)
        videoPerviousButton = view.findViewById(R.id.exo_prev)
        videoNextButton = view.findViewById(R.id.exo_next)
        VideoRenuButton = view.findViewById(R.id.exo_rew)
        videoFarwardButton = view.findViewById(R.id.exo_ffwd)

        videoPlayButton = view.findViewById(R.id.exo_play)
        videoPauseButton = view.findViewById(R.id.exo_pause)

        videoLockButton = view.findViewById(R.id.exo_lock)
        videoUnLockButton = view.findViewById(R.id.exo_unlock)
        previewTimeBar = findViewById<View>(R.id.exo_progress) as DefaultTimeBar
        currentDurationPlayTv = view.findViewById(R.id.exo_position)
        previewImageView = view.findViewById(R.id.imageView)
        videoNextButton.setVisibility(GONE)
        videoPerviousButton.setVisibility(GONE)
        simpleExoPlayerView = view.findViewById(R.id.videoPlayer)
        videoRotationButton = view.findViewById(R.id.enter_full_screen)
        closeVideoPlayerButton = view.findViewById(R.id.closeButton);
        pictureInPicture = view.findViewById(R.id.picture_in_picture)
        pictureInPicture.setVisibility(GONE)
        videoNextButton.setVisibility(GONE)
        videoPerviousButton.setVisibility(GONE)



        previewTimeBar.setAdMarkerColor(Color.argb(0x00, 0xFF, 0xFF, 0xFF))
        previewTimeBar.setPlayedAdMarkerColor(Color.argb(0x98, 0xFF, 0xFF, 0xFF))




        videoRotationButton.setOnClickListener(OnClickListener {
            context.finish()
        })

        volumeUnMuteButton.setOnClickListener {
            mMediaPlayer?.audioComponent?.volume = 0f
            volumeMuteAndUnMuteButton.visibility = View.VISIBLE
            volumeUnMuteButton.visibility = View.GONE
        }


        volumeMuteAndUnMuteButton.setOnClickListener {
            mMediaPlayer?.audioComponent?.volume = mMediaPlayer?.audioComponent?.volume!!
            mMediaPlayer?.audioComponent?.volume = 5f
            volumeMuteAndUnMuteButton.visibility = View.GONE
            volumeUnMuteButton.visibility = View.VISIBLE

        }



        closeVideoPlayerButton.setOnClickListener {
            context.finish()
        }



        videoControllerLayout?.setOnClickListener {
            if (isControllerShown) {
                hideController()
            } else {
                contentRateLayout.visibility = View.GONE
                showController()
            }
        }



        errorRetryLayout.setOnClickListener(OnClickListener {
            errorRetryLayout?.setVisibility(GONE)
            initializeMainPlayer(mContentUrl, true)
        })

        pictureInPicture.setOnClickListener(OnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                hideController()

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val aspectRatio = Rational(16, 9)
                    val actions: ArrayList<RemoteAction> = ArrayList()
                    //actions.add(remoteAction)
                    mPictureInPictureParamsBuilder.setAspectRatio(aspectRatio).setActions(actions)
                        .build()
                    context.enterPictureInPictureMode(mPictureInPictureParamsBuilder.build())

                } else {
                    context.enterPictureInPictureMode()
                }

                isPipModeOn = true
            }
        })



        exoRewLinearLayout.setOnClickListener {
            // rewind()
            VideoRenuButton.performClick()
        }

        exoFfwdLinearLayout.setOnClickListener {
            //fastForward()
            videoFarwardButton.performClick()
        }



        videoPlayButton.setOnClickListener(OnClickListener {
            mMediaPlayer?.playWhenReady = true
            simpleExoPlayerView?.player?.setPlayWhenReady(true)
            videoPlayButton.setVisibility(GONE)
            videoPauseButton.setVisibility(VISIBLE)
            videoPlayerSdkCallBackListener?.onPlayClick(1)
        })
        videoPauseButton.setOnClickListener(OnClickListener {
            mMediaPlayer?.playWhenReady = false
            simpleExoPlayerView?.player?.setPlayWhenReady(false)
            videoPlayButton.setVisibility(VISIBLE)
            videoPauseButton.setVisibility(GONE)
            videoPlayerSdkCallBackListener?.onPlayClick(0)
        })



        super.onFinishInflate()
    }


    fun getVolumeStatus(): Boolean {
        if (volumeMuteAndUnMuteButton.visibility == View.VISIBLE)
            return true
        else if (volumeUnMuteButton.visibility == View.VISIBLE)
            return false
        else
            return false

    }

    private var isVideoVolumeStatus = false

    fun setVolumeStatus(isVolume: Boolean) {
        this.isVideoVolumeStatus = isVideoVolumeStatus
        if (isVolume) {
            mMediaPlayer?.audioComponent?.volume = mMediaPlayer?.audioComponent?.volume!!
            mMediaPlayer?.audioComponent?.volume = 5f
            volumeMuteAndUnMuteButton.visibility = View.GONE
            volumeUnMuteButton.visibility = View.VISIBLE
        } else {
            mMediaPlayer?.audioComponent?.volume = 0f
            volumeMuteAndUnMuteButton.visibility = View.VISIBLE
            volumeUnMuteButton.visibility = View.GONE
        }
    }


    fun setWebSeriesEnable(
        isWebSeries: Boolean,
        UserLoginStatus: Boolean,
        contentAccessType: String
    ) {
        this.isWebSeries = isWebSeries
        this.userSubscriptionDtatus = UserLoginStatus
        this.contentAccessType = contentAccessType
    }


    fun showEpisodeButton(show: Boolean) {
        if (isWebSeries && show) {
            epsodeButton.visibility = View.VISIBLE
            epsodeNextButton.visibility = View.VISIBLE
        } else {
            epsodeButton.visibility = View.GONE
            epsodeNextButton.visibility = View.GONE
        }
    }


    fun showVolumeProgressBarButton() {
        volumeLayout?.visibility = View.VISIBLE
    }

    fun hideVolumeProgressBarButton() {
        volumeLayout?.visibility = View.GONE
    }


    fun showMuteUnMuteButton() {
        volumeLinearLayout?.visibility = View.VISIBLE
    }

    fun hideMuteUnMuteButton() {
        volumeLinearLayout?.visibility = View.GONE
    }

    fun showRotateButton() {
        videoRotationButton?.visibility = View.VISIBLE
    }

    fun hideRotateButton() {
        videoRotationButton?.visibility = View.GONE
    }


    fun showBackButton() {
        if (closeVideoPlayerButton != null)
            closeVideoPlayerButton?.visibility = View.VISIBLE
    }

    fun hideBackButton() {
        if (closeVideoPlayerButton != null)
            closeVideoPlayerButton?.visibility = View.GONE
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
        hideController()
    }


    fun isNeedVideoPlayerPause(): Boolean {
        return isPipModeOn;
    }

    fun disablePipMode(isPipModeOn: Boolean) {
        this.isPipModeOn = isPipModeOn;
    }


    fun setSpriteImageUrl(url: String) {
        this.spriteImageUrl = url;
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        val orientation = resources.configuration.orientation
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            if (contentTitle != null && !TextUtils.isEmpty(contentTitle)) {
                videoTitle?.visibility = View.VISIBLE
                videoTitle?.setText(contentTitle)
            } else
                videoTitle?.visibility = View.GONE
        } else if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            videoTitle?.visibility = View.GONE
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
        if (mContentUrl == null) throw Exception("Content type must not be null")

        initViews()
    }

    private fun videoLockUnlockStatus() {
/*
        if (isScreenLockEnable) {
            videoLockButton!!.visibility = VISIBLE
            videoUnLockButton!!.visibility = GONE
        } else {
            videoLockButton!!.visibility = GONE
            videoUnLockButton!!.visibility = VISIBLE
        }
*/
    }

    // init view and view group here
    private fun initViews() {
//        ToastMessage.showLogs(ToastMessage.LogType.ERROR, "Video Player:::", "initViews()");

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

        //volumeProgressBarSetUp()
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
        centerButtonLayout.visibility = GONE
        videoPlayerSdkCallBackListener?.prepareVideoPlayer()
        //        ToastMessage.showLogs(ToastMessage.LogType.DEBUG, TAG, "Content url is " + videoUrl);
        val customLoadControl: LoadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(1000, 50000, 1000, 1)
            .setAllocator(DefaultAllocator(true, 32 * 1024))
            .setTargetBufferBytes(C.LENGTH_UNSET)
            .setPrioritizeTimeOverSizeThresholds(true)
            .setBackBuffer(0, false)
            .build()




        if (adsUrl != null && !TextUtils.isEmpty(adsUrl)) {
            val dataSourceFactory: DataSource.Factory = DefaultDataSource.Factory(
                context
            )
            val mediaSourceFactory: MediaSourceFactory =
                DefaultMediaSourceFactory(dataSourceFactory)
                    .setAdsLoaderProvider { unusedAdTagUri: MediaItem.AdsConfiguration? -> adsLoader }
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
                mMediaPlayer?.setMediaSource(playerMediaSource!!)
            } else if (isOfflineContent) {
                mediaItem = MediaItem.Builder().setUri(videoUrl).build()
                val downloadRequest: DownloadRequest? =
                    VideoPlayerDownloadUtil.getDownloadTracker(context)
                        .getDownloadRequest(mediaItem.playbackProperties?.uri)
                VideoPlayerTracer.error(
                    "Offline Video Url:::",
                    "" + mediaItem.playbackProperties?.uri
                )
                val mediaSource = DownloadHelper.createMediaSource(
                    downloadRequest!!,
                    VideoPlayerDownloadUtil.getReadOnlyDataSourceFactory(context)
                )

                mMediaPlayer?.setMediaSource(mediaSource!!)

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

            val mediaSession = MediaSessionCompat(context, "com.lionsgacom.multitv.ottteplay")
            val mediaSessionConnector = MediaSessionConnector(mediaSession)
            mediaSessionConnector.setPlayer(mMediaPlayer)
            mediaSession.isActive = true

            seekTo(mMediaPlayer!!.currentPosition + watchDuration)

            if (isVideoVolumeStatus) {
                mMediaPlayer?.audioComponent?.volume = mMediaPlayer?.audioComponent?.volume!!
                mMediaPlayer?.audioComponent?.volume = 5f
                volumeMuteAndUnMuteButton.visibility = View.GONE
                volumeUnMuteButton.visibility = View.VISIBLE
            } else {
                mMediaPlayer?.audioComponent?.volume = 0f
                volumeMuteAndUnMuteButton.visibility = View.VISIBLE
                volumeUnMuteButton.visibility = View.GONE
            }


        }
    }

    var stateChangeCallback1: Player.Listener = object : Player.Listener {
        override fun onPlayerError(error: PlaybackException) {
            super.onPlayerError(error)
            if (mMediaPlayer != null && mMediaPlayer!!.currentPosition != 0L) seekPlayerTo =
                mMediaPlayer!!.currentPosition
                    .toInt() / 1000
            errorRetryLayout.bringToFront()
            errorRetryLayout.visibility = VISIBLE
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
            when (playbackState) {
                ExoPlayer.STATE_BUFFERING -> {
                    text += "buffering"
                    bufferingProgressBarLayout.bringToFront()
                    bufferingProgressBarLayout.visibility = VISIBLE
                    centerButtonLayout.visibility = GONE
                    if (contentType == ContentType.LIVE) startBufferingTimer()
                }
                ExoPlayer.STATE_ENDED -> {
                    text += "ended"

                    if (isNeedReplayIcon) {
                        repeatVideoLinearLayout.visibility = View.VISIBLE
                        circularProgressLayout.visibility = View.GONE
                        bufferingProgressBarLayout.visibility = GONE
                        repeatVideoLinearLayout.setOnClickListener {
                            repeatVideoLinearLayout.visibility = View.GONE
                            releaseVideoPlayer()
                            initializeMainPlayer(mContentUrl, true)
                        }
                    }


/*
                    if (contentType == ContentType.VOD) {
                        if (mMediaPlayer != null) contentPlayedTimeInMillis =
                            mMediaPlayer!!.currentPosition
                        releaseVideoPlayer()
                        bufferingProgressBarLayout!!.visibility = GONE
                        circularProgressRing =
                            findViewById<View>(R.id.circular_progress_ring) as FabButton
                        circularProgressRing.showProgress(true)
                        circularProgressRing.setProgress(0f)
                        circularProgressLayout.visibility = VISIBLE
                        // circularProgressLayout!!.bringToFront()
                        val totalDuration = 1200
                        val tickDuration = 300
                        countDownTimer = object : CountDownTimerWithPause(
                            totalDuration.toLong(),
                            (tickDuration / 10).toLong(),
                            true
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
                                    if (userSubscriptionDtatus)
                                        videoPlayerSdkCallBackListener?.onPlayNextVideo()
                                    else if (contentAccessType.equals("paid") && !userSubscriptionDtatus)
                                        videoPlayerSdkCallBackListener?.subscriptionCallBack()
                                    else if (contentAccessType.equals("free"))
                                        videoPlayerSdkCallBackListener?.onPlayNextVideo()
                                    else
                                        videoPlayerSdkCallBackListener?.showThumbnailCallback()

                                } else {
                                    if (contentAccessType.equals("free"))
                                        videoPlayerSdkCallBackListener?.onPlayNextVideo()
                                    else
                                        videoPlayerSdkCallBackListener?.subscriptionCallBack()
                                }

                            }
                        }.create()
                    }
*/
                }
                ExoPlayer.STATE_IDLE -> {
                    text += "idle"
                    if (!checkForAudioFocus()) return
                    bufferingProgressBarLayout.visibility = GONE
                    centerButtonLayout.visibility = VISIBLE
                    if (mMediaPlayer != null) {
                        contentPlayedTimeInMillis = mMediaPlayer!!.currentPosition
                        if (contentType == ContentType.LIVE) startBufferingTimer()
                    }
                    simpleExoPlayerView?.videoSurfaceView?.visibility = VISIBLE
                    simpleExoPlayerView?.visibility = VISIBLE
                    simpleExoPlayerView?.bringToFront()
                }
                ExoPlayer.STATE_READY -> {
                    text += "ready"
                    bufferingProgressBarLayout.visibility = GONE
                    centerButtonLayout.visibility = VISIBLE
                    videoNextButton.visibility = GONE
                    videoPerviousButton.visibility = GONE
                    videoPlayerSdkCallBackListener?.onVideoStartNow()
                }
                else -> text += "unknown"
            }

//            ToastMessage.showToastMsg(context, text, Toast.LENGTH_SHORT);
//            ToastMessage.showLogs(ToastMessage.LogType.ERROR, "Video Player:::", text);
        }

        override fun onRepeatModeChanged(repeatMode: Int) {}
        override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters) {}
    }


    private var isNeedReplayIcon = false
    fun setReplyVisiblityEnabled(isNeedReplayIcon: Boolean) {
        this.isNeedReplayIcon = isNeedReplayIcon;
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

        if (contentTitle != null && !TextUtils.isEmpty(contentTitle)) {
            videoTitle.visibility = View.VISIBLE
            videoTitle.setText(contentTitle)
        } else
            videoTitle.visibility = View.GONE
    }

    private fun showSystemBar() {
        val decorView = (getContext() as Activity).window.decorView
        val uiOptions = SYSTEM_UI_FLAG_VISIBLE
        decorView.systemUiVisibility = uiOptions
        //hideSystemUiFullScreen()

        videoTitle.visibility = View.GONE
    }

    @SuppressLint("InlinedApi")
    private fun hideSystemUiFullScreen() {

        simpleExoPlayerView!!.systemUiVisibility = (View.SYSTEM_UI_FLAG_LOW_PROFILE
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
    }


    private fun rewind() {
        seekTo(mMediaPlayer!!.currentPosition - DEFAULT_REWIND_MS)
    }


    private var watchDuration = 0L

    fun seekVideoPlayer(watch: Long) {
        this.watchDuration = watch;
    }

    fun showResumedVideoHint(isShow: Boolean) {
        if (isShow)
            resumedVideoTv.visibility = View.VISIBLE
        else
            resumedVideoTv.visibility = View.GONE
    }

    private fun fastForward() {
        seekTo(Math.max(mMediaPlayer!!.currentPosition + DEFAULT_FAST_FORWARD_MS, 0))
    }

    private fun seekTo(positionMs: Long) {
        mMediaPlayer!!.seekTo(positionMs)
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
            //isResumeFromPreviousPosition = true;
        }
    }

    private var maxLine = 0
    fun setSpriteImageThumbnailMaxLine(maxLine: Int) {
        this.maxLine = maxLine;
    }


    private val pallyconEventListener: PallyconEventListener = object : PallyconEventListener {
        override fun onDrmKeysLoaded(licenseInfo: Map<String, String>) {}
        override fun onDrmSessionManagerError(e: Exception) {
            Log.e("Pollycon Error:::", "" + e.message)
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


    enum class GestureType {
        NoGesture, SwipeGesture, DoubleTapGesture
    }

    override fun onCastSessionAvailable() {

    }

    override fun onCastSessionUnavailable() {

    }


    fun onKeyDownEvent() {
        volumeProgressBar.setProgress(audioManager?.getStreamVolume(AudioManager.STREAM_MUSIC)!!)

        val volume = audioManager?.getStreamVolume(AudioManager.STREAM_MUSIC) as Int
        mMediaPlayer?.audioComponent?.volume = volume.toFloat()
        if (volume < 1) {
            volumeMuteAndUnMuteButton.visibility = View.VISIBLE
            volumeUnMuteButton.visibility = View.GONE
        } else {
            volumeMuteAndUnMuteButton.visibility = View.GONE
            volumeUnMuteButton.visibility = View.VISIBLE
        }
    }

    fun onKeyUpEvent() {

        volumeProgressBar.setProgress(audioManager?.getStreamVolume(AudioManager.STREAM_MUSIC)!!)

        val volume = audioManager?.getStreamVolume(AudioManager.STREAM_MUSIC) as Int
        mMediaPlayer?.audioComponent?.volume = volume.toFloat()
        if (volume < 1) {
            volumeMuteAndUnMuteButton.visibility = View.VISIBLE
            volumeUnMuteButton.visibility = View.GONE
        } else {
            volumeMuteAndUnMuteButton.visibility = View.GONE
            volumeUnMuteButton.visibility = View.VISIBLE
        }
    }


    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (event.keyCode === KeyEvent.KEYCODE_VOLUME_DOWN) {
            volumeProgressBar.setProgress(audioManager?.getStreamVolume(AudioManager.STREAM_MUSIC)!!)
            Log.e(
                "Volume::::",
                "KEYCODE_VOLUME_DOWN:::" + audioManager?.getStreamVolume(AudioManager.STREAM_MUSIC)
            )
            val volume = audioManager?.getStreamVolume(AudioManager.STREAM_MUSIC) as Int
            mMediaPlayer?.audioComponent?.volume = volume.toFloat()

            if (volume < 1) {
                volumeMuteAndUnMuteButton.visibility = View.VISIBLE
                volumeUnMuteButton.visibility = View.GONE
            } else {
                volumeMuteAndUnMuteButton.visibility = View.GONE
                volumeUnMuteButton.visibility = View.VISIBLE
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        if (event.keyCode === KeyEvent.KEYCODE_VOLUME_UP) {
            volumeProgressBar.setProgress(audioManager?.getStreamVolume(AudioManager.STREAM_MUSIC)!!)
            val volume = audioManager?.getStreamVolume(AudioManager.STREAM_MUSIC) as Int
            mMediaPlayer?.audioComponent?.volume = volume.toFloat()
            Log.e(
                "Volume::::",
                "KEYCODE_VOLUME_UP::::" + audioManager?.getStreamVolume(AudioManager.STREAM_MUSIC)
            )

            if (volume < 1) {
                volumeMuteAndUnMuteButton.visibility = View.VISIBLE
                volumeUnMuteButton.visibility = View.GONE
            } else {
                volumeMuteAndUnMuteButton.visibility = View.GONE
                volumeUnMuteButton.visibility = View.VISIBLE
            }
        }

        return super.onKeyUp(keyCode, event)
    }

    private fun volumeProgressBarSetUp() {
        var startVolume: Int = 0
        var maxVolume: Int = 0
        maxVolume = audioManager?.getStreamMaxVolume(AudioManager.STREAM_MUSIC) ?: 100
        startVolume = audioManager?.getStreamVolume(AudioManager.STREAM_MUSIC) ?: 100
        volumeProgressBar.progress = startVolume
        volumeProgressBar.max = maxVolume
        volumeProgressBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                audioManager?.setStreamVolume(AudioManager.STREAM_MUSIC, p0?.progress!!, 0)

                Log.e("Volume::::", "" + p0?.progress)
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
                //showController()
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
                // volumeProgressBar.progress = p0?.progress!!
                audioManager?.setStreamVolume(AudioManager.STREAM_MUSIC, p0?.progress!!, 0)
                hideController()
            }

        })
    }

    private var countDownTimer1: CountDownTimerWithPause? = null

    private fun setTimerOnVideoPlayer(isShow: Boolean) {
        val tickDuration = 500
        if (isShow)
            contentRateLayout.visibility = View.VISIBLE
        else {
            contentRatedTv.visibility = View.GONE
            countDownTimer1?.cancel()
        }

        if (!TextUtils.isEmpty(parentalAge)) {
            contentRatedTv.setText("Rated U/A " + parentalAge + "+")
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

        countDownTimer1 =
            object : CountDownTimerWithPause(5000.toLong(), (tickDuration).toLong(), true) {
                override fun onTick(millisUntilFinished: Long) {

                }

                override fun onFinish() {
                    contentRateLayout.visibility = View.GONE
                    countDownTimer1?.cancel()

                }
            }.create()
    }

    private var parentalAge = ""
    private var genure = ""
    private var language = ""
    private lateinit var contentRateLayout: LinearLayoutCompat
    private lateinit var genureTv: TextView
    private lateinit var languageTv: TextView
    private lateinit var contentRatedTv: TextView

    fun setGenure(genure: String) {
        this.genure = genure
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

    fun onBackButtonPress() {
        val orientation = getContext().resources.configuration.orientation
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            (getContext() as Activity).requestedOrientation =
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            (getContext() as Activity).window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            showSystemBar()
            //  videoRotationButton?.setImageResource(R.drawable.ic_balaji_fullscreen)
            videoLockButton.setVisibility(GONE)
            videoUnLockButton.setVisibility(GONE)
        } else if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            (getContext() as Activity).requestedOrientation =
                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            (getContext() as Activity).window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            hideSystemBars()
            //  videoRotationButton?.setImageResource(R.drawable.ic_minimize)
            videoLockUnlockStatus()
        }
    }


    fun showEpisodeButtonVisiblityListener(size: Int) {
        if (isWebSeries && size > 1) {
            epsodeButton.visibility = View.VISIBLE
            epsodeNextButton.visibility = View.VISIBLE
        } else {
            epsodeButton.visibility = View.GONE
            epsodeNextButton.visibility = View.GONE
        }
    }


    fun hideController() {
        closeVideoPlayerButton.visibility = GONE
        overlayImageTransparent.visibility = GONE
        centerButtonLayout.visibility = GONE
        //    videoProgressLayout.visibility = GONE
        videoMenuLayout.visibility = GONE
        resumedVideoTv.visibility = View.GONE
        previewTimeBar.visibility = View.GONE
        durationLinearLayout.visibility = View.GONE
        removeCallbacks(hideAction)
        hideAtMs = C.TIME_UNSET
        isControllerShown = false
        setTimerOnVideoPlayer(true)
        updatePlayPauseButton()
    }

    fun showController() {
        closeVideoPlayerButton.visibility = VISIBLE
        overlayImageTransparent.visibility = VISIBLE
        centerButtonLayout.visibility = VISIBLE
        // videoProgressLayout.visibility = VISIBLE
        videoMenuLayout.visibility = VISIBLE
        resumedVideoTv.visibility = View.GONE

        previewTimeBar.visibility = View.VISIBLE
        durationLinearLayout.visibility = View.VISIBLE
        updatePlayPauseButton()
        hideAfterTimeout()
        isControllerShown = true
        setTimerOnVideoPlayer(false)
        updatePlayPauseButton()

    }

}