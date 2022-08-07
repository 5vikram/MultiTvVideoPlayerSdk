package com.multitv.ott.multitvvideoplayer;


import static android.content.Context.TELEPHONY_SERVICE;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.SystemClock;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.*;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.Target;
import com.google.android.exoplayer2.*;
import com.google.android.exoplayer2.drm.DrmSessionManager;
import com.google.android.exoplayer2.ext.ima.ImaAdsLoader;
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory;
import com.google.android.exoplayer2.source.MediaSourceFactory;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.ui.StyledPlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultAllocator;
import com.google.android.exoplayer2.upstream.DefaultDataSource;
import com.google.android.exoplayer2.util.MimeTypes;
import com.google.android.exoplayer2.util.Util;
import com.google.android.exoplayer2.video.VideoSize;
import com.google.common.collect.ImmutableList;
import com.multitv.ott.multitvvideoplayer.cast.CastPlayer;
import com.multitv.ott.multitvvideoplayer.cast.SessionAvailabilityListener;
import com.multitv.ott.multitvvideoplayer.custom.CountDownTimerWithPause;
import com.multitv.ott.multitvvideoplayer.database.SharedPreferencePlayer;
import com.multitv.ott.multitvvideoplayer.fabbutton.FabButton;
import com.multitv.ott.multitvvideoplayer.listener.VideoPlayerSdkCallBackListener;
import com.multitv.ott.multitvvideoplayer.playerglide.GlideThumbnailTransformation;
import com.multitv.ott.multitvvideoplayer.popup.TrackSelectionDialog;
import com.multitv.ott.multitvvideoplayer.timebar.PreviewTimeBar;
import com.multitv.ott.multitvvideoplayer.timebar.previewseekbar.PreviewBar;
import com.multitv.ott.multitvvideoplayer.timebar.previewseekbar.PreviewLoader;
import com.multitv.ott.multitvvideoplayer.touchevent.OnSwipeTouchListener;
import com.multitv.ott.multitvvideoplayer.utils.*;
import com.multitv.ott.multitvvideoplayer.videoplayer.MyVideoPlayer;
import com.pallycon.widevinelibrary.*;

import java.util.*;

public class VideoTvPlayer extends FrameLayout implements View.OnClickListener, PreviewLoader, PreviewBar.OnScrubListener, SessionAvailabilityListener {

    private AppCompatActivity context;
    private SharedPreferencePlayer sharedPreferencePlayer;
    private ContentType contentType;
    private ExoPlayer mMediaPlayer;
    private StyledPlayerView simpleExoPlayerView;
    private DefaultTrackSelector trackSelector;
    private VideoPlayerSdkCallBackListener videoPlayerSdkCallBackListener;
    private boolean isShowingTrackSelectionDialog;
    private PallyconWVMSDK WVMAgent = null;
    private String analaticsUrl, token, userId, contentTitle, contentId;


    private long millisecondsForResume, adPlayedTimeInMillis, contentPlayedTimeInMillis, bufferingTimeInMillis;
    private int seekPlayerTo;
    private String mContentUrl, subTitleUri;
    private Handler bufferingTimeHandler;
    private CountDownTimerWithPause countDownTimer;
    private final String TAG = "VikramExoVideoPlayer";

    private LinearLayout errorRetryLayout, durationlayout, circularProgressLayout, bufferingProgressBarLayout, centerButtonLayout, videoMenuLayout, videoProgressLayout;
    private ImageView videoPlayButton, videoPauseButton, pictureInPicture, previewImageView, videoLockButton, setting, videoRotationButton,
            videoPerviousButton, videoNextButton, VideoRenuButton, videoFarwardButton, videoUnLockButton;
    private PreviewTimeBar playerProgress;
    private TextView currentDurationPlayTv;
    private ProgressBar volumeProgressBar, brightnessProgressBar;
    private FrameLayout progressBarParent, previewFrameLayout;


    public static final int DEFAULT_FAST_FORWARD_MS = 10000;
    public static final int DEFAULT_REWIND_MS = 10000;
    public static final int DEFAULT_TIMEOUT_MS = 5000;
    private StringBuilder formatBuilder;
    private Formatter formatter;
    private boolean isDrmContent;
    private String drmContentToken, drmdrmLicenseUrl, siteId, siteKey;
    private DrmSessionManager drmSessionManager = null;


    private ImaAdsLoader adsLoader;
    private String adsUrl;

    private boolean isScreenLockEnable = false;
    private boolean isControllerShown = false;
    private boolean isAttachedToWindowStatus = false;
    private boolean isAttachedToWindow = false;
    private long hideAtMs = 0;

    private Window mWindow;

    private CastPlayer mCastPlayer;
    private int mInitialTextureWidth = 0;
    private int mInitialTextureHeight = 0;


    public VideoTvPlayer(Context context, AttributeSet attrs) {
        this((AppCompatActivity) context, attrs, 0);
    }

    public VideoTvPlayer(AppCompatActivity context1,AttributeSet attrs, int defStyleAttr) {
        super(context1, attrs, defStyleAttr);
        this.context = context1;
        trackSelector = new DefaultTrackSelector(context);
        formatBuilder = new StringBuilder();
        formatter = new Formatter(formatBuilder, Locale.getDefault());
        CommonUtils.setDefaultCookieManager();

        TelephonyManager mgr = (TelephonyManager) context.getSystemService(TELEPHONY_SERVICE);
        sharedPreferencePlayer = new SharedPreferencePlayer();
        sharedPreferencePlayer.setPreferenceInt(context, "pos", 0);
        if (Build.VERSION.SDK_INT >= 31) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                mgr.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
            }
        } else {
            mgr.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
        }
    }


    @Override
    protected void onFinishInflate() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.player_layout, this);
        progressBarParent = view.findViewById(R.id.progress_bar_parent);
        volumeProgressBar = view.findViewById(R.id.volume_progress_bar);
        brightnessProgressBar = view.findViewById(R.id.brightness_progress_bar);
        errorRetryLayout = view.findViewById(R.id.errorRetryLayout);
        durationlayout = view.findViewById(R.id.durationlayout);
        videoMenuLayout = view.findViewById(R.id.videoMenuLayout);
        bufferingProgressBarLayout = view.findViewById(R.id.bufferingProgressBarLayout);
        circularProgressLayout = view.findViewById(R.id.circularProgressLayout);
        videoProgressLayout = findViewById(R.id.video_progress_layout);
        setting = view.findViewById(R.id.settings_btn);
        previewFrameLayout = view.findViewById(R.id.previewFrameLayout);
        setting.setOnClickListener(this);
        centerButtonLayout = view.findViewById(R.id.centerButtonLayout);
        videoPerviousButton = view.findViewById(R.id.exo_prev);
        videoNextButton = view.findViewById(R.id.exo_next);
        VideoRenuButton = view.findViewById(R.id.exo_rew);
        videoFarwardButton = view.findViewById(R.id.exo_ffwd);
        videoPlayButton = view.findViewById(R.id.exo_play);
        videoPauseButton = view.findViewById(R.id.exo_pause);
        videoLockButton = view.findViewById(R.id.exo_lock);
        videoUnLockButton = view.findViewById(R.id.exo_unlock);
        playerProgress = view.findViewById(R.id.exo_progress);
        currentDurationPlayTv = view.findViewById(R.id.exo_position);
        previewImageView = view.findViewById(R.id.previewImageView);
        simpleExoPlayerView = view.findViewById(R.id.videoPlayer);
        videoRotationButton = view.findViewById(R.id.enter_full_screen);
        playerProgress.addOnScrubListener(this);
        playerProgress.setPreviewLoader(this);
        pictureInPicture = view.findViewById(R.id.picture_in_picture);
        videoNextButton.setVisibility(GONE);
        videoPerviousButton.setVisibility(GONE);
        pictureInPicture.setVisibility(GONE);
        videoRotationButton.setVisibility(GONE);


        playerProgress.setAdMarkerColor(Color.argb(0x00, 0xFF, 0xFF, 0xFF));
        playerProgress.setPlayedAdMarkerColor(Color.argb(0x98, 0xFF, 0xFF, 0xFF));


        findViewById(R.id.speed_btn).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                showSpeedControlDailog();
            }
        });

        errorRetryLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                errorRetryLayout.setVisibility(GONE);
                initializeMainPlayer(mContentUrl, true, mMediaPlayer);
            }
        });

        videoUnLockButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                isScreenLockEnable = false;
                videoUnLockButton.setVisibility(GONE);
                videoLockButton.setVisibility(VISIBLE);
                showController();
            }
        });

        videoLockButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                isScreenLockEnable = true;
                videoUnLockButton.setVisibility(VISIBLE);
                videoLockButton.setVisibility(GONE);
                hideController();
            }
        });

        VideoRenuButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                rewind();
            }
        });

        videoFarwardButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                fastForward();
            }
        });

        videoPlayButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mMediaPlayer != null) {
                    mMediaPlayer.setPlayWhenReady(false);
                    videoPlayButton.setVisibility(GONE);
                    videoPauseButton.setVisibility(VISIBLE);
                }
            }
        });

        VideoRenuButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mMediaPlayer != null) {
                    mMediaPlayer.setPlayWhenReady(false);
                    videoPlayButton.setVisibility(VISIBLE);
                    videoPauseButton.setVisibility(GONE);
                }
            }
        });

        playerProgress.addOnPreviewVisibilityListener(new PreviewBar.OnPreviewVisibilityListener() {
            @Override
            public void onVisibilityChanged(PreviewBar previewBar, boolean isPreviewShowing) {
                Log.d("PreviewShowing::::", "" + isPreviewShowing);
            }
        });


        super.onFinishInflate();
    }


    private AudioManager audioManager = null;

    // init view and view group here
    private void initViews() {
//        ToastMessage.showLogs(ToastMessage.LogType.ERROR, "Video Player:::", "initViews()");
        trackSelector = new DefaultTrackSelector(context);
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

        if (videoPlayerSdkCallBackListener != null)
            videoPlayerSdkCallBackListener.onPlayerReady(mContentUrl);

        mWindow = context.getWindow();
    }

    private final Runnable hideAction = new Runnable() {
        @Override
        public void run() {
            VideoPlayerTracer.error("Controller Listener:::", "Stop Timer");
            hideController();
        }
    };

    @Override
    protected void onAttachedToWindow() {
        isAttachedToWindowStatus = true;
        updatePlayPauseButton();
        super.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        isAttachedToWindowStatus = false;
        removeCallbacks(hideAction);
        if (hideAtMs != C.TIME_UNSET) {
            long delayMs = hideAtMs - SystemClock.uptimeMillis();
            if (delayMs <= 0) {
                hideController();
            } else {
                postDelayed(hideAction, delayMs);
            }
        }
    }

    public  void setCastSessionAvailabilityListener(SessionAvailabilityListener sessionAvailabilityListener) {
        if (mCastPlayer != null) {
            mCastPlayer.setSessionAvailabilityListener(sessionAvailabilityListener);
        }
    }

    public  void setCastPlayer(CastPlayer castPlayer) {
        if (mCastPlayer == null) {
            mCastPlayer = castPlayer;
        }
    }

    private void hideAfterTimeout() {
        removeCallbacks(hideAction);
        if (5000 > 0) {
            VideoPlayerTracer.error("Controller Listener:::", "Start Timer");
            hideAtMs = SystemClock.uptimeMillis() + 5000;
            if (isAttachedToWindow) {
                postDelayed(hideAction, 5000);
            }
        } else {
            hideAtMs = C.TIME_UNSET;
        }
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            videoLockUnlockStatus();
        } else if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            videoLockButton.setVisibility(View.GONE);
            videoUnLockButton.setVisibility(View.GONE);
        }
        super.onConfigurationChanged(newConfig);
    }

    private void hideController() {
        centerButtonLayout.setVisibility(View.GONE);
        videoProgressLayout.setVisibility(View.GONE);
        durationlayout.setVisibility(View.GONE);
        videoMenuLayout.setVisibility(View.GONE);
        removeCallbacks(hideAction);
        hideAtMs = C.TIME_UNSET;
        isControllerShown = false;
    }

    private void showController() {
        if (!isScreenLockEnable) {
            centerButtonLayout.setVisibility(View.VISIBLE);
            videoProgressLayout.setVisibility(View.VISIBLE);
            durationlayout.setVisibility(View.VISIBLE);
            videoMenuLayout.setVisibility(View.VISIBLE);
        }
        updatePlayPauseButton();
        hideAfterTimeout();
        isControllerShown = true;
    }


    private void updatePlayPauseButton() {
        boolean requestPlayPauseFocus = false;
        boolean playing = mMediaPlayer != null && mMediaPlayer.getPlayWhenReady();
        if (videoPlayButton != null) {
            requestPlayPauseFocus =
                    requestPlayPauseFocus || (playing && videoPlayButton.isFocused());
            if (playing)
                videoPlayButton.setVisibility(View.GONE);
            else
                videoPlayButton.setVisibility(View.VISIBLE);
        }
        if (videoPauseButton != null) {
            requestPlayPauseFocus = requestPlayPauseFocus || (!playing && videoPauseButton.isFocused());

            if (!playing)
                videoPauseButton.setVisibility(View.GONE);
            else
                videoPauseButton.setVisibility(View.VISIBLE);
        }
        if (requestPlayPauseFocus) {
            requestPlayPauseFocus();
        }
    }

    private void requestPlayPauseFocus() {
        boolean playing = mMediaPlayer != null && mMediaPlayer.getPlayWhenReady();
        if (!playing && videoPlayButton != null) {
            videoPlayButton.requestFocus();
        } else if (playing && videoPauseButton != null) {
            videoPauseButton.requestFocus();
        }
    }

    final AudioManager.OnAudioFocusChangeListener audioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int focusChange) {
            if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {

                if ((mMediaPlayer != null && mMediaPlayer.getPlayWhenReady())) {
                    checkForAudioFocus();
                }
            }
        }
    };

    private boolean checkForAudioFocus() {
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

        // Request audio focus for playback
        int result = audioManager.requestAudioFocus(audioFocusChangeListener,
                // Use the music stream.
                AudioManager.STREAM_MUSIC,
                // Request permanent focus.
                AudioManager.AUDIOFOCUS_GAIN);

        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {

            return true;
        } else {
            // other app had stopped playing song now , so u can do u stuff now .
//            Toast.makeText(context, "Other audio player did not stop. Can't play video",
//                    Toast.LENGTH_LONG).show();
//            pause();
            return false;
        }
    }


    PhoneStateListener phoneStateListener = new PhoneStateListener() {
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            if (state == TelephonyManager.CALL_STATE_RINGING) {
                //INCOMING call
                //do all necessary action to pause the audio
                pauseVideoPlayer();

            } else if (state == TelephonyManager.CALL_STATE_IDLE) {
                //Not IN CALL
                //do anything if the phone-state is idle
                if (!CommonUtils.isAppIsInBackground(context))
                    resumeVideoPlayer();
            } else if (state == TelephonyManager.CALL_STATE_OFFHOOK) {
                //A call is dialing, active or on hold
                //do all necessary action to pause the audio
                //do something here
                pauseVideoPlayer();
            }
            super.onCallStateChanged(state, incomingNumber);
        }
    };


    public  void prepareVideoPlayer() {
        if (mContentUrl == null)
            return;

        initViews();
    }

    private void videoLockUnlockStatus() {
        if (isScreenLockEnable) {
            videoLockButton.setVisibility(View.VISIBLE);
            videoUnLockButton.setVisibility(View.GONE);
        } else {
            videoLockButton.setVisibility(View.GONE);
            videoUnLockButton.setVisibility(View.VISIBLE);
        }
    }

    public  void setMultiTvVideoPlayerSdkListener(VideoPlayerSdkCallBackListener videoPlayerSdkCallBackListener) {
        this.videoPlayerSdkCallBackListener = videoPlayerSdkCallBackListener;
    }

    public  void setContentFilePath(String path) {
        mContentUrl = path;
    }

    public void setContentType(ContentType type) {
        contentType = type;
    }

    public void setPreRollAdUrl(String adUrl) {
        adsUrl = adUrl;
    }

    public  void setAnalaticsUrl(String analaticsUrl) {
        this.analaticsUrl = analaticsUrl;
    }

    public  void setKeyToken(String token) {
        this.token = token;
    }

    public void setAuthDetails(String id) {
        userId = id;
    }

    public void setContentTitle(String title) {
        contentTitle = title;
    }

    public   void setContentId(String id) {
        contentId = id;
    }

    public  void setSubtitleVideoUri(String subtitleUri) {
        subTitleUri = subtitleUri;
    }

    public void setDrmEnabled(
            boolean drmContent,
            String siteId,
            String siteKey,
            String drmToekn,
            String drmLicenseUrl
    ) {
        isDrmContent = drmContent;
        drmContentToken = drmToekn;
        drmdrmLicenseUrl = drmLicenseUrl;
        this.siteId = siteId;
        this.siteKey = siteKey;
    }

    // get play duration of video in milli second
    public Long getContentPlayedTimeInMillis() {
        // contentPlayedTimeInMillis = 0;
        if (contentPlayedTimeInMillis == 0L) {
            if (seekPlayerTo != 0) contentPlayedTimeInMillis = seekPlayerTo;
            else if (mMediaPlayer != null) contentPlayedTimeInMillis =
                    mMediaPlayer.getCurrentPosition();
        } else {
            if (seekPlayerTo != 0) contentPlayedTimeInMillis =
                    seekPlayerTo;
            else if (mMediaPlayer != null) contentPlayedTimeInMillis =
                    mMediaPlayer.getCurrentPosition();
        }
        return contentPlayedTimeInMillis;
    }

    public void setContentPlayedTimeInMillis(long contentPlayedTimeInMillis) {
        this.contentPlayedTimeInMillis = contentPlayedTimeInMillis;
    }

    // start video player when player is ready state
    public void startVideoPlayer(boolean isNeedToPlayInstantly) {
        initializeMainPlayer(mContentUrl, true, mMediaPlayer);
    }

    // resume video player
    public void resumeVideoPlayer() {
        if (mMediaPlayer != null && simpleExoPlayerView != null) {
            simpleExoPlayerView.onResume();
            mMediaPlayer.setPlayWhenReady(true);
        }
    }

    // pause video player
    public void pauseVideoPlayer() {
        if (mMediaPlayer != null && simpleExoPlayerView != null) {
            simpleExoPlayerView.onPause();
            mMediaPlayer.setPlayWhenReady(true);
        }
    }

    // relase and destroy video player
    public void releaseVideoPlayer() {
        if (mMediaPlayer != null && simpleExoPlayerView != null) {
            sendAnalaticsData(context, userId, contentId, contentTitle, token);
            simpleExoPlayerView.getPlayer().release();
            mMediaPlayer.release();
            if (adsLoader != null) adsLoader.setPlayer(null);
        }
    }


    private void initializeMainPlayer(String videoUrl, boolean isNeedToPlayInstantly, Player currentPlayer) {
//        ToastMessage.showLogs(ToastMessage.LogType.ERROR, "Video Player:::", "initializeMainPlayer");
        if (currentPlayer != null) {
            currentPlayer.release();
            if (adsLoader != null) adsLoader.setPlayer(null);
//            mMediaPlayer = null
        }
        centerButtonLayout.setVisibility(View.GONE);
        videoPlayerSdkCallBackListener.prepareVideoPlayer();
        //        ToastMessage.showLogs(ToastMessage.LogType.DEBUG, TAG, "Content url is " + videoUrl);
        LoadControl customLoadControl = new DefaultLoadControl.Builder()
                .setBufferDurationsMs(1000, 50000, 1000, 1)
                .setAllocator(new DefaultAllocator(true, 32 * 1024))
                .setTargetBufferBytes(C.LENGTH_UNSET)
                .setPrioritizeTimeOverSizeThresholds(true)
                .setBackBuffer(0, false)
                .build();


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
            DataSource.Factory dataSourceFactory = new DefaultDataSource.Factory(context);


            MediaSourceFactory mediaSourceFactory = new DefaultMediaSourceFactory(dataSourceFactory)
                    .setAdsLoaderProvider(adsConfiguration -> adsLoader).setAdViewProvider(simpleExoPlayerView);


            mMediaPlayer = new ExoPlayer.Builder(context).setMediaSourceFactory(mediaSourceFactory)
                    .setTrackSelector(trackSelector).setLoadControl(customLoadControl).build();
            adsLoader = new ImaAdsLoader.Builder( /* context= */context).build();
        } else {
            mMediaPlayer = new ExoPlayer.Builder(context).setTrackSelector(trackSelector)
                    .setLoadControl(customLoadControl).build();
        }
        if (mMediaPlayer != null) {
            mMediaPlayer.addListener(stateChangeCallback1);
            simpleExoPlayerView.setPlayer(mMediaPlayer);
            simpleExoPlayerView.setControllerHideOnTouch(true);
            simpleExoPlayerView.setControllerAutoShow(false);
            simpleExoPlayerView.setControllerShowTimeoutMs(DEFAULT_TIMEOUT_MS);
            simpleExoPlayerView.setControllerHideDuringAds(true);
            MediaItem mediaItem = null;
            MediaItem.SubtitleConfiguration subtitle = null;

            if (subTitleUri != null && !TextUtils.isEmpty(subTitleUri)) {
                subtitle = new MediaItem.SubtitleConfiguration.Builder(Uri.parse(subTitleUri))
                        .setMimeType(MimeTypes.APPLICATION_SUBRIP) // The correct MIME type (required).
                        .setLanguage("en") // MUST, The subtitle language (optional).
                        .setSelectionFlags(C.SELECTION_FLAG_DEFAULT) //MUST,  Selection flags for the track (optional).
                        .build();
            }
            if (isDrmContent) {
                try {
                    WVMAgent = new PallyconWVMSDKFactory().getInstance(context);
                    WVMAgent.init(context, null, siteId, siteKey);
                    WVMAgent.setPallyconEventListener(pallyconEventListener);
                } catch (PallyconDrmException e) {
                    e.printStackTrace();
                } catch (UnAuthorizedDeviceException e) {
                    e.printStackTrace();
                }
                mediaItem = new MediaItem.Builder().setUri(videoUrl).build();
                try {
                    drmSessionManager = WVMAgent.createDrmSessionManagerByToken(
                            UUID.fromString(C.WIDEVINE_UUID.toString()),
                            drmdrmLicenseUrl,
                            Uri.parse(videoUrl),
                            drmContentToken);
                } catch (PallyconDrmException e) {
                    e.printStackTrace();
                }

                mMediaPlayer.setMediaSource(new ExoUttils().buildMediaSource(
                        context,
                        mediaItem,
                        videoUrl,
                        drmSessionManager
                ));
            } else {
                if (subtitle != null) {
                    /*MediaSource playerMediaSource = new ExoUttils().buildMediaSource(context, mediaItem, videoUrl, drmSessionManager);
                    MediaSource mediaSource = new MergingMediaSource(mediaSources);
                    mMediaPlayer.setMediaSource(mediaSource);*/
                    if (adsUrl != null && !TextUtils.isEmpty(adsUrl)) {
                        adsLoader.setPlayer(mMediaPlayer);
                        Uri adTagUri = Uri.parse(adsUrl);
                        mediaItem = new MediaItem.Builder()
                                .setSubtitleConfigurations(ImmutableList.of(subtitle))
                                .setUri(videoUrl)
                                .setAdsConfiguration(new MediaItem.AdsConfiguration.Builder(adTagUri).build())
                                .build();
                    } else {
                        mediaItem = new MediaItem.Builder()
                                .setSubtitleConfigurations(ImmutableList.of(subtitle))
                                .setUri(videoUrl)
                                .build();
                    }
                } else {
                    if (adsUrl != null && !TextUtils.isEmpty(adsUrl)) {
                        adsLoader.setPlayer(mMediaPlayer);
                        //adsLoader.focusSkipButton();
                        Uri adTagUri = Uri.parse(adsUrl);
                        new MediaItem.Builder()
                                .setUri(videoUrl)
                                .setAdsConfiguration(new MediaItem.AdsConfiguration.Builder(adTagUri).build())
                                .build();
                    } else {
                        new MediaItem.Builder()
                                .setUri(videoUrl)
                                .build();
                    }
                }
                mMediaPlayer.setMediaItem(mediaItem);
            }
            mMediaPlayer.prepare();
            if (isNeedToPlayInstantly) {
                mMediaPlayer.setPlayWhenReady(true);
            }
        }
    }

    Player.EventListener stateChangeCallback1 = new Player.Listener() {
        @Override
        public void onPlayerError(PlaybackException error) {
            Player.Listener.super.onPlayerError(error);
            if (mMediaPlayer != null && mMediaPlayer.getCurrentPosition() != 0L)
                seekPlayerTo = (int) mMediaPlayer.getCurrentPosition() / 1000;
            errorRetryLayout.bringToFront();
            errorRetryLayout.setVisibility(View.VISIBLE);
            videoPlayerSdkCallBackListener.onPlayerError(error.getMessage());
        }

        @Override
        public void onVideoSizeChanged(VideoSize videoSize) {
            Player.Listener.super.onVideoSizeChanged(videoSize);
            mInitialTextureWidth = videoSize.width;
            mInitialTextureHeight = videoSize.height;
        }

        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            //Player.Listener.super.onPlayerStateChanged(playWhenReady, playbackState);
            String text = "Main player";
            switch (playbackState) {
                case ExoPlayer.STATE_BUFFERING:
                    text += "buffering";
                    bufferingProgressBarLayout.bringToFront();
                    bufferingProgressBarLayout.setVisibility(View.VISIBLE);
                    centerButtonLayout.setVisibility(View.GONE);
                    if (contentType == ContentType.LIVE)
                        startBufferingTimer();
                    break;


                case ExoPlayer.STATE_IDLE:
                    text += "idle";
                    if (!checkForAudioFocus()) return;

                    if (bufferingProgressBarLayout != null)
                        bufferingProgressBarLayout.setVisibility(View.GONE);

                    centerButtonLayout.setVisibility(View.VISIBLE);
                    if (mMediaPlayer != null) {
                        contentPlayedTimeInMillis = mMediaPlayer.getCurrentPosition();

                        if (contentType == ContentType.LIVE)
                            startBufferingTimer();
                    }
                    simpleExoPlayerView.setVisibility(View.VISIBLE);
                    simpleExoPlayerView.setVisibility(View.VISIBLE);
                    simpleExoPlayerView.bringToFront();
                    break;

                case ExoPlayer.STATE_READY:
                    text += "ready";
                    bufferingProgressBarLayout.setVisibility(View.GONE);
                    centerButtonLayout.setVisibility(View.VISIBLE);
                    videoNextButton.setVisibility(View.GONE);
                    videoPerviousButton.setVisibility(View.GONE);
                    break;

                case ExoPlayer.STATE_ENDED:
                    text += "ended";
                    if (contentType == ContentType.VOD) {
                        if (mMediaPlayer != null) contentPlayedTimeInMillis =
                                mMediaPlayer.getCurrentPosition();
                        releaseVideoPlayer();
                        bufferingProgressBarLayout.setVisibility(View.GONE);
                        FabButton circularProgressRing =
                                circularProgressLayout.findViewById(R.id.circular_progress_ring);
                        circularProgressRing.showProgress(true);
                        circularProgressRing.setProgress(0f);
                        circularProgressLayout.setVisibility(View.VISIBLE);
                        circularProgressLayout.bringToFront();
                        long totalDuration = 10000;
                        long tickDuration = 1000;


                        countDownTimer = new CountDownTimerWithPause(totalDuration, tickDuration / 10, true) {
                            public void onTick(long millisUntilFinished) {
                                float progress = (float) millisUntilFinished / totalDuration;
                                progress = progress * 100;
                                progress = 100 - progress;
                                circularProgressRing.setProgress(progress);
                            }

                            public void onFinish() {
                                if (circularProgressLayout != null)
                                    circularProgressLayout.setVisibility(View.GONE);
                                try {
                                    prepareVideoPlayer();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    centerButtonLayout.setVisibility(View.VISIBLE);
                                }
                            }
                        }.create();

                    }
                default:
                    text += "unknown";

                    break;
            }
        }
    };


    private void startBufferingTimer() {
        if (bufferingTimeHandler == null) {
            bufferingTimeHandler = new Handler();
        }
        if (bufferingTimeRunnable != null)
            bufferingTimeHandler.postDelayed(bufferingTimeRunnable, 0);
    }

    private void stopBufferingTimer() {
        if (bufferingTimeHandler != null && bufferingTimeRunnable != null) {
            bufferingTimeHandler.removeCallbacks(bufferingTimeRunnable);
            bufferingTimeHandler.removeCallbacksAndMessages(null);
        }
    }

    private Runnable bufferingTimeRunnable = new Runnable() {
        @Override
        public void run() {
            bufferingTimeInMillis = bufferingTimeInMillis + 1000;

            //Log.e("Naseeb", "Buffering time " + bufferingTimeInMillis);

            if (bufferingTimeHandler != null)
                bufferingTimeHandler.postDelayed(this, 1000);
        }
    };

    private AlertDialog dialog = null;

    public void hideSpeedDailog() {
        new SpeedControllUttils().hideSpeedDailog();
    }

    void showSpeedControlDailog() {
        new SpeedControllUttils().showSpeedControlDailog(context, sharedPreferencePlayer, mMediaPlayer);
    }


    @Override
    public void onClick(View view) {
        if (view == setting) {
            if (!isShowingTrackSelectionDialog
                    && TrackSelectionDialog.willHaveContent(trackSelector)
            ) {
                isShowingTrackSelectionDialog = true;
                isShowingTrackSelectionDialog = new SpeedControllUttils().showTraksDailog(context, trackSelector, isShowingTrackSelectionDialog);
            }
        }
    }

    private void hideSystemBars() {
        View decorView = context.getWindow().getDecorView();
        int uiOptions = (SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | SYSTEM_UI_FLAG_FULLSCREEN);
        decorView.setSystemUiVisibility(uiOptions);
    }

    private void showSystemBar() {
        View decorView = context.getWindow().getDecorView();
        int uiOptions = SYSTEM_UI_FLAG_VISIBLE;
        decorView.setSystemUiVisibility(uiOptions);
    }

    private void rewind() {
        seekTo(Math.max(mMediaPlayer.getCurrentPosition() - DEFAULT_REWIND_MS, 0));
    }

    private void fastForward() {
        seekTo(Math.max(mMediaPlayer.getCurrentPosition() + DEFAULT_FAST_FORWARD_MS, 0));
    }

    private void seekTo(long positionMs) {
        mMediaPlayer.seekTo(positionMs);
    }


    private PallyconEventListener pallyconEventListener = new PallyconEventListener() {
        @Override
        public void onDrmKeysLoaded(Map<String, String> licenseInfo) {
        }

        @Override
        public void onDrmSessionManagerError(Exception e) {
//            Toast.makeText(context, /*e.getMessage()*/ "Error in DRM", Toast.LENGTH_LONG).show();
        }

        @Override
        public void onDrmKeysRestored() {
        }

        @Override
        public void onDrmKeysRemoved() {
        }
    };

    private void sendAnalaticsData(final AppCompatActivity activity, final String userId, String contentId, String contentTitle, String token) {
        String finalAnalaticsUrl = analaticsUrl;
        if (finalAnalaticsUrl == null || TextUtils.isEmpty(finalAnalaticsUrl))
            return;

        if (contentId != null && !TextUtils.isEmpty(contentId) && contentTitle != null && !TextUtils.isEmpty(contentTitle) && token != null && !TextUtils.isEmpty(token)) {
            long totalDuration = getDuration();
            long bufferDuration = getBufferingTimeInMillis();
            long palyedDuration = getContentPlayedTimeInMillis();
            new AppSessionUtil().sendHeartBeat(activity, userId, finalAnalaticsUrl, contentId, contentTitle, palyedDuration, bufferDuration, totalDuration, token);
        } else {
            VideoPlayerTracer.error("Analatics Error:::", "token or content id or content title is required field.");
        }

    }


    public long getDuration() {
        if (mMediaPlayer != null)
            return mMediaPlayer.getDuration();
        else
            return 0;
    }

    public long getCurrentPosition() {
        if (mMediaPlayer != null)
            return mMediaPlayer.getCurrentPosition();
        else
            return 0;
    }

    public long getBufferingTimeInMillis() {
        if (mMediaPlayer != null)
            return mMediaPlayer.getTotalBufferedDuration();
        else
            return 0;
    }

    public void resumeFromPosition(long millisecondsForResume) {
        if (millisecondsForResume != 0) {
            this.millisecondsForResume = millisecondsForResume;
            //isResumeFromPreviousPosition = true;
        }
    }

    @Override
    public void onScrubStart(PreviewBar previewBar) {
        previewFrameLayout.setVisibility(View.VISIBLE);
        pauseVideoPlayer();
        removeCallbacks(hideAction);
    }

    @Override
    public void onScrubMove(PreviewBar previewBar, int progress, boolean fromUser) {
        previewFrameLayout.setVisibility(View.VISIBLE);
        if (currentDurationPlayTv != null) {
            currentDurationPlayTv.setText(Util.getStringForTime(
                    formatBuilder,
                    formatter,
                    Long.valueOf(progress)
            ));
        }
    }

    @Override
    public void onScrubStop(PreviewBar previewBar) {
        previewFrameLayout.setVisibility(View.GONE);
        //findViewById(R.id.centerButtonLayout)?.setVisibility(View.VISIBLE);
        if (mMediaPlayer != null) {
            seekTo(Long.valueOf(previewBar.getProgress()));
        }
        resumeVideoPlayer();
    }

    @Override
    public void loadPreview(long currentPosition, long max) {
        pauseVideoPlayer();
        Glide.with(previewImageView)
                .load("http://103.253.175.13/rahuls/output-160x90-thumb-001.jpg")
                .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                .transform(new GlideThumbnailTransformation(currentPosition))
                .into(previewImageView);
    }


    @Override
    public void onCastSessionAvailable() {

    }

    @Override
    public void onCastSessionUnavailable() {

    }
}
