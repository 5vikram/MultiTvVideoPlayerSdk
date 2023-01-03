package com.multitv.ott.multitvvideoplayer

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.text.TextUtils
import android.util.AttributeSet
import android.util.Log
import android.view.*
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.drm.DrmSessionManager
import com.google.android.exoplayer2.ext.ima.ImaAdsLoader
import com.google.android.exoplayer2.offline.DownloadHelper
import com.google.android.exoplayer2.offline.DownloadRequest
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.source.MediaSourceFactory
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultAllocator
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.google.android.exoplayer2.util.MimeTypes
import com.google.android.exoplayer2.video.VideoSize
import com.google.common.collect.ImmutableList
import com.multitv.ott.multitvvideoplayer.database.SharedPreferencePlayer
import com.multitv.ott.multitvvideoplayer.download.DownloadUtil
import com.multitv.ott.multitvvideoplayer.listener.BannerVideoPlayerEventLister
import com.multitv.ott.multitvvideoplayer.listener.MoreInfoListener
import com.multitv.ott.multitvvideoplayer.utils.CommonUtils
import com.multitv.ott.multitvvideoplayer.utils.ContentType
import com.multitv.ott.multitvvideoplayer.utils.ExoUttils
import com.multitv.ott.multitvvideoplayer.utils.VideoPlayerTracer
import com.pallycon.widevinelibrary.*
import java.util.*


class BalajiCarsolVideoPlayer(
    private val context: Activity,
    attrs: AttributeSet?,
    defStyleAttr: Int
) : FrameLayout(
    context, attrs, defStyleAttr
) {
    private val sharedPreferencePlayer: SharedPreferencePlayer
    private var contentType: ContentType? = null
    private var mMediaPlayer: ExoPlayer? = null
    private var trackSelector: DefaultTrackSelector
    private var bannerVideoPlayerEventLister: BannerVideoPlayerEventLister? = null
    private var MoreInfoListener: MoreInfoListener? = null
    private var WVMAgent: PallyconWVMSDK? = null
    private var token: String? = null
    private var userId: String? = null
    private var contentTitle: String? = null
    private var contentId: String? = null
    private var millisecondsForResume: Long = 0
    private var contentPlayedTimeInMillis: Long = 0
    private var playPauseButtonEnable = true;
    private var mInitialTextureWidth: Int = 0
    private var mInitialTextureHeight: Int = 0

    var bufferingTimeInMillis: Long = 0
    private var seekPlayerTo = 0
    private var mContentUrl: String? = null
    private var subTitleUri: String? = null
    private var bufferingTimeHandler: Handler? = null

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
    private var isAttachedToWindowStatus = false
    private var mWindow: Window? = null
    private var isPipModeOn = false

    private var simpleExoPlayerView: StyledPlayerView? = null
    private lateinit var videoRotationButton: ImageView
    private lateinit var moreInfoLinearLayout: LinearLayoutCompat
    private lateinit var videoPlayButton: ImageView
    private lateinit var videoPauseButton: ImageView
    private lateinit var volumeMuteButton: ImageView
    private lateinit var volumeUnMuteButton: ImageView
    private lateinit var videoPlayerControllerRealtiveLayout: RelativeLayout


    constructor(context: Context, attrs: AttributeSet?) : this(
        context as AppCompatActivity,
        attrs,
        0
    ) {
    }


    override fun onFinishInflate() {
        val view =
            LayoutInflater.from(getContext())
                .inflate(R.layout.banner_video_player, this)

        simpleExoPlayerView = view.findViewById(R.id.videoPlayer)
        videoPlayerControllerRealtiveLayout =
            view.findViewById(R.id.videoPlayerControllerRealtiveLayout)
        moreInfoLinearLayout = view.findViewById(R.id.moreInfoLinearLayout)
        videoRotationButton = view.findViewById(R.id.videoRotationButton)
        videoPlayButton = view.findViewById(R.id.exo_play)
        videoPauseButton = view.findViewById(R.id.exo_pause)
        volumeMuteButton = view.findViewById(R.id.volumeMuteButton)
        volumeUnMuteButton = view.findViewById(R.id.volumeUnMuteButton)


        volumeMuteButton.visibility = View.VISIBLE
        volumeUnMuteButton.visibility = View.GONE

        videoPlayerControllerRealtiveLayout.setOnClickListener {
            if (mMediaPlayer?.isPlaying!!) {
                videoPlayButton.visibility = View.VISIBLE
                videoPauseButton.visibility = View.GONE
                mMediaPlayer?.playWhenReady = false
            } else {
                videoPlayButton.visibility = View.GONE
                videoPauseButton.visibility = View.VISIBLE
                mMediaPlayer?.playWhenReady = true
            }
        }

        videoRotationButton.setOnClickListener {
            bannerVideoPlayerEventLister?.fullScreenCallBack()
        }

        moreInfoLinearLayout.setOnClickListener {
            bannerVideoPlayerEventLister?.moreButtonClickListener()
        }

        videoPlayButton.setOnClickListener(OnClickListener {
            mMediaPlayer?.playWhenReady = true
            videoPlayButton.setVisibility(GONE)
            videoPauseButton.setVisibility(VISIBLE)
            bannerVideoPlayerEventLister?.onPlayClick(1)
        })
        videoPauseButton.setOnClickListener(OnClickListener {
            mMediaPlayer?.playWhenReady = false
            videoPlayButton.setVisibility(VISIBLE)
            videoPauseButton.setVisibility(GONE)
            bannerVideoPlayerEventLister?.onPlayClick(0)
        })

        volumeUnMuteButton.setOnClickListener {
            mMediaPlayer?.audioComponent?.volume = 0f
            volumeMuteButton.visibility = View.VISIBLE
            volumeUnMuteButton.visibility = View.GONE
        }


        volumeMuteButton.setOnClickListener {
            mMediaPlayer?.audioComponent?.volume = 0f
            mMediaPlayer?.audioComponent?.volume = mMediaPlayer?.audioComponent?.volume!!
            mMediaPlayer?.audioComponent?.volume = 2f
            volumeMuteButton.visibility = View.GONE
            volumeUnMuteButton.visibility = View.VISIBLE

        }



        super.onFinishInflate()
    }


    fun setControllerEnabled(isChromeCastConnected: Boolean) {

        if (isChromeCastConnected) {
            videoPlayButton.isFocusable = false
            videoPlayButton.isClickable = false
            videoPauseButton.isFocusable = false
            videoPauseButton.isClickable = false
            volumeMuteButton.isFocusable = false
            volumeMuteButton.isClickable = false
            volumeUnMuteButton.isFocusable = false
            volumeUnMuteButton.isClickable = false
            videoRotationButton.isFocusable = false
            videoRotationButton.isClickable = false
            moreInfoLinearLayout.isFocusable = false
            moreInfoLinearLayout.isClickable = false

        } else {
            videoPlayButton.isFocusable = true
            videoPlayButton.isClickable = true
            videoPauseButton.isFocusable = true
            videoPauseButton.isClickable = true
            volumeMuteButton.isFocusable = true
            volumeMuteButton.isClickable = true
            volumeUnMuteButton.isFocusable = true
            volumeUnMuteButton.isClickable = true
            videoRotationButton.isFocusable = true
            videoRotationButton.isClickable = true
            moreInfoLinearLayout.isFocusable = true
            moreInfoLinearLayout.isClickable = true

        }
    }

    fun getAudioManager(): AudioManager {
        return audioManager!!
    }

    fun getMediaPlayer(): ExoPlayer {
        return mMediaPlayer!!
    }

    fun muteVolume() {
        mMediaPlayer?.audioComponent?.volume = 0f
    }

    fun unmuteVolume() {
        var volume = audioManager?.getStreamVolume(AudioManager.STREAM_MUSIC) as Int
        mMediaPlayer?.audioComponent?.volume = volume.toFloat()

    }


    fun restoreCarsoulVideoPlayer() {
        mMediaPlayer?.audioComponent?.volume = 0f
    }


    fun playPauseEnable(playPauseButtonEnable: Boolean) {
        this.playPauseButtonEnable = playPauseButtonEnable;
    }


    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        isAttachedToWindowStatus = true
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        isAttachedToWindowStatus = false
    }


    fun isNeedVideoPlayerPause(): Boolean {
        return isPipModeOn;
    }

    fun disablePipMode(isPipModeOn: Boolean) {
        this.isPipModeOn = isPipModeOn;
    }


    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
    }


    fun getVideoPlayerStatus(): Boolean {
        return mMediaPlayer != null && mMediaPlayer!!.playWhenReady
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
        val result = audioManager?.requestAudioFocus(
            audioFocusChangeListener,  // Use the music stream.
            AudioManager.STREAM_MUSIC,  // Request permanent focus.
            AudioManager.AUDIOFOCUS_GAIN
        )
        return if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            true
        } else {
            pauseVideoPlayer()
            false
        }
    }

    var phoneStateListener: PhoneStateListener = object : PhoneStateListener() {
        override fun onCallStateChanged(state: Int, incomingNumber: String) {
            if (state == TelephonyManager.CALL_STATE_RINGING) {
                pauseVideoPlayer()
            } else if (state == TelephonyManager.CALL_STATE_IDLE) {
                if (!CommonUtils.isAppIsInBackground(context)) resumeVideoPlayer()
            } else if (state == TelephonyManager.CALL_STATE_OFFHOOK) {
                pauseVideoPlayer()
            }
            super.onCallStateChanged(state, incomingNumber)
        }
    }


    fun prepareVideoPlayer() {
        if (mContentUrl == null) throw Exception("Content type must not be null")

        initViews()
    }

    private fun initViews() {

        if (mMediaPlayer != null && simpleExoPlayerView != null) {
            simpleExoPlayerView!!.player!!.release()
            mMediaPlayer!!.release()
            if (adsLoader != null) adsLoader!!.setPlayer(null)
        }

        trackSelector = DefaultTrackSelector(context)
        audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager


        if (bannerVideoPlayerEventLister != null) bannerVideoPlayerEventLister!!.onPlayerReady(
            mContentUrl
        )

        mWindow = context.window

    }

    fun setMultiTvVideoPlayerSdkListener(videoPlayerSdkCallBackListener: BannerVideoPlayerEventLister?) {
        this.bannerVideoPlayerEventLister = videoPlayerSdkCallBackListener
    }

    fun setMultiTvMoreInfoVideoListener(listener: MoreInfoListener) {
        this.MoreInfoListener = listener
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
            var volume = audioManager?.getStreamVolume(AudioManager.STREAM_MUSIC) as Int
            mMediaPlayer?.audioComponent?.volume = volume.toFloat()
            if (volume < 1) {
                volumeMuteButton.visibility = View.VISIBLE
                volumeUnMuteButton?.visibility = View.GONE
            } else {
                volumeMuteButton.visibility = View.GONE
                volumeUnMuteButton.visibility = View.VISIBLE
            }
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
            simpleExoPlayerView!!.player!!.release()
            mMediaPlayer!!.release()
            if (adsLoader != null) adsLoader!!.setPlayer(null)
        }
    }


    private fun initializeMainPlayer(videoUrl: String?, isNeedToPlayInstantly: Boolean) {
        if (mMediaPlayer != null) {
            mMediaPlayer!!.release()
            if (adsLoader != null) adsLoader!!.setPlayer(null)
            mMediaPlayer = null
        }

        bannerVideoPlayerEventLister?.prepareVideoPlayer()

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
                    DownloadUtil.getDownloadTracker(context)
                        .getDownloadRequest(mediaItem.playbackProperties?.uri)
                VideoPlayerTracer.error(
                    "Offline Video Url:::",
                    "" + mediaItem.playbackProperties?.uri
                )
                val mediaSource = DownloadHelper.createMediaSource(
                    downloadRequest!!,
                    DownloadUtil.getReadOnlyDataSourceFactory(context)
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
            mMediaPlayer?.audioComponent?.volume = 0f
            volumeMuteButton.visibility = View.VISIBLE
            volumeUnMuteButton.visibility = View.GONE

            //mMediaPlayer?.setRepeatMode(Player.REPEAT_MODE_ONE)
            mMediaPlayer?.prepare()
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
            bannerVideoPlayerEventLister?.onPlayerError(error.message)
        }

        override fun onTracksChanged(
            trackGroups: TrackGroupArray,
            trackSelections: TrackSelectionArray
        ) {
        }


        override fun onVideoSizeChanged(videoSize: VideoSize) {
            super.onVideoSizeChanged(videoSize)
            mInitialTextureWidth = videoSize.width
            mInitialTextureHeight = videoSize.height
            bannerVideoPlayerEventLister?.onVideoScreenResolution(
                mInitialTextureWidth,
                mInitialTextureHeight
            )
        }

        override fun onLoadingChanged(isLoading: Boolean) {}
        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            mMediaPlayer?.setRepeatMode(Player.REPEAT_MODE_ONE)
            var text = "Main player"
            when (playbackState) {
                ExoPlayer.STATE_BUFFERING -> {
                    text += "buffering"
                    bannerVideoPlayerEventLister?.onVideoBufferCallBack()
                }
                ExoPlayer.STATE_ENDED -> {
                    text += "ended"
                    if (contentType == ContentType.VOD) {
                        releaseVideoPlayer()
                        bannerVideoPlayerEventLister?.onPlayNextVideo()
                        //mMediaPlayer?.setRepeatMode(Player.REPEAT_MODE_ONE)
                    }
                }
                ExoPlayer.STATE_IDLE -> {
                    text += "idle"
                    if (!checkForAudioFocus()) return

                    if (mMediaPlayer != null) {
                        contentPlayedTimeInMillis = mMediaPlayer!!.currentPosition
                    }
                    simpleExoPlayerView?.videoSurfaceView!!.visibility = VISIBLE
                    simpleExoPlayerView?.visibility = VISIBLE

                }
                ExoPlayer.STATE_READY -> {
                    text += "ready"
                    videoPauseButton.visibility = View.VISIBLE
                    videoPlayButton.visibility = View.VISIBLE
                    mMediaPlayer?.audioComponent?.volume = 0f
                    bannerVideoPlayerEventLister?.onVideoStartNow()
                }
                else -> text += "unknown"
            }
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

        }
    }


    private val pallyconEventListener: PallyconEventListener = object : PallyconEventListener {
        override fun onDrmKeysLoaded(licenseInfo: Map<String, String>) {}
        override fun onDrmSessionManagerError(e: Exception) {
        }

        override fun onDrmKeysRestored() {}
        override fun onDrmKeysRemoved() {}
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


    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (event.keyCode === KeyEvent.KEYCODE_VOLUME_DOWN) {
            volumeMuteButton.visibility = View.VISIBLE
            volumeUnMuteButton.visibility = View.GONE
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        if (event.keyCode === KeyEvent.KEYCODE_VOLUME_UP) {
            volumeMuteButton.visibility = View.GONE
            volumeUnMuteButton.visibility = View.VISIBLE
        }
        return super.onKeyUp(keyCode, event)
    }


    fun onKeyDownEvent() {

        var volume = audioManager?.getStreamVolume(AudioManager.STREAM_MUSIC) as Int
        mMediaPlayer?.audioComponent?.volume = volume.toFloat()
        if (volume < 1) {
            volumeMuteButton.visibility = View.VISIBLE
            volumeUnMuteButton.visibility = View.GONE
        } else {
            volumeMuteButton.visibility = View.GONE
            volumeUnMuteButton.visibility = View.VISIBLE
        }
    }

    fun onKeyUpEvent() {

        var volume = audioManager?.getStreamVolume(AudioManager.STREAM_MUSIC) as Int
        mMediaPlayer?.audioComponent?.volume = volume.toFloat()
        if (volume < 1) {
            volumeMuteButton.visibility = View.VISIBLE
            volumeUnMuteButton?.visibility = View.GONE
        } else {
            volumeMuteButton.visibility = View.GONE
            volumeUnMuteButton.visibility = View.VISIBLE
        }
    }


}