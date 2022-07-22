package com.multitv.ott.multitvvideoplayer;

import static android.content.Context.TELEPHONY_SERVICE;
import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.Target;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackException;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.TracksInfo;

import com.google.android.exoplayer2.drm.DrmSessionManager;
import com.google.android.exoplayer2.ext.ima.ImaAdsLoader;
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.MediaSourceFactory;
import com.google.android.exoplayer2.source.MergingMediaSource;
import com.google.android.exoplayer2.source.TrackGroup;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.ui.StyledPlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultAllocator;
import com.google.android.exoplayer2.upstream.DefaultDataSource;
import com.google.android.exoplayer2.util.MimeTypes;
import com.google.common.collect.ImmutableList;
import com.google.android.exoplayer2.util.Util;
import com.multitv.ott.multitvvideoplayer.custom.CountDownTimerWithPause;
import com.multitv.ott.multitvvideoplayer.custom.ToastMessage;
import com.multitv.ott.multitvvideoplayer.database.SharedPreferencePlayer;
import com.multitv.ott.multitvvideoplayer.fabbutton.FabButton;
import com.multitv.ott.multitvvideoplayer.listener.VideoPlayerSdkCallBackListener;
import com.multitv.ott.multitvvideoplayer.models.TempResolutionModel;
import com.multitv.ott.multitvvideoplayer.models.TrackResolution;
import com.multitv.ott.multitvvideoplayer.playerglide.GlideThumbnailTransformation;
import com.multitv.ott.multitvvideoplayer.popup.TrackSelectionDialog;
import com.multitv.ott.multitvvideoplayer.timebar.PreviewTimeBar;
import com.multitv.ott.multitvvideoplayer.timebar.previewseekbar.PreviewBar;
import com.multitv.ott.multitvvideoplayer.timebar.previewseekbar.PreviewLoader;
import com.multitv.ott.multitvvideoplayer.touchevent.OnSwipeTouchListener;
import com.multitv.ott.multitvvideoplayer.utils.AppSessionUtil;
import com.multitv.ott.multitvvideoplayer.utils.CommonUtils;
import com.multitv.ott.multitvvideoplayer.utils.ContentType;
import com.multitv.ott.multitvvideoplayer.utils.ExoUttils;
import com.multitv.ott.multitvvideoplayer.utils.VideoPlayerTracer;
import com.pallycon.widevinelibrary.PallyconDrmException;
import com.pallycon.widevinelibrary.PallyconEventListener;
import com.pallycon.widevinelibrary.PallyconWVMSDK;
import com.pallycon.widevinelibrary.PallyconWVMSDKFactory;
import com.pallycon.widevinelibrary.UnAuthorizedDeviceException;

import java.util.ArrayList;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;


public class MultiTvPlayerSdk extends FrameLayout implements PreviewLoader, PreviewBar.OnScrubListener,
        View.OnClickListener {

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

    private LinearLayout errorRetryLayout, videoMenuLayout, durationlayout, videoProgressLayout, bufferingProgressBarLayout, circularProgressLayout, centerButtonLayout;
    private ImageView pictureInPicture, previewImageView, videoLockButton, videoUnLockButton, setting, videoRotationButton, videoPerviousButton, videoNextButton, VideoRenuButton, videoFarwardButton, videoPlayButton, videoPauseButton;
    private PreviewTimeBar playerProgress;
    private TextView currentDurationPlayTv;
    private FrameLayout previewFrameLayout;


    public static final int DEFAULT_FAST_FORWARD_MS = 10000;
    public static final int DEFAULT_REWIND_MS = 10000;
    public static final int DEFAULT_TIMEOUT_MS = 5000;
    private final StringBuilder formatBuilder;
    private final Formatter formatter;
    private boolean isDrmContent;
    private String drmContentToken, drmdrmLicenseUrl, siteId, siteKey;
    private DrmSessionManager drmSessionManager = null;


    private ImaAdsLoader adsLoader;
    private String adsUrl;
    private boolean isScreenLockEnable;
    private boolean isAttachedToWindow;
    private long hideAtMs;


    public MultiTvPlayerSdk(Context context, AttributeSet attrs) {
        this((AppCompatActivity) context, attrs, 0);
    }

    public MultiTvPlayerSdk(AppCompatActivity context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
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

        playerProgress = (PreviewTimeBar) findViewById(R.id.exo_progress);
        currentDurationPlayTv = view.findViewById(R.id.exo_position);
        previewImageView = view.findViewById(R.id.previewImageView);
        videoNextButton.setVisibility(View.GONE);
        videoPerviousButton.setVisibility(View.GONE);


        playerProgress.addOnScrubListener(this);
        playerProgress.setPreviewLoader(this);

        pictureInPicture = view.findViewById(R.id.picture_in_picture);

        videoNextButton.setVisibility(View.GONE);
        videoPerviousButton.setVisibility(View.GONE);

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

        pictureInPicture.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    context.enterPictureInPictureMode();
                }
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
                    mMediaPlayer.setPlayWhenReady(true);
                    videoPlayButton.setVisibility(View.GONE);
                    videoPauseButton.setVisibility(View.VISIBLE);
                }
            }
        });

        videoPauseButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mMediaPlayer != null) {
                    mMediaPlayer.setPlayWhenReady(false);
                    videoPlayButton.setVisibility(View.VISIBLE);
                    videoPauseButton.setVisibility(View.GONE);
                }
            }
        });


        playerProgress.addOnPreviewVisibilityListener(new PreviewBar.OnPreviewVisibilityListener() {
            @Override
            public void onVisibilityChanged(PreviewBar previewBar, boolean isPreviewShowing) {
                Log.d("PreviewShowing::::", String.valueOf(isPreviewShowing));
            }
        });


        super.onFinishInflate();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        isAttachedToWindow = true;
        updateAll();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        isAttachedToWindow = false;
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


    private final Runnable hideAction = new Runnable() {
        @Override
        public void run() {
            hideController();
        }
    };

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


    private void hideAfterTimeout() {
        removeCallbacks(hideAction);
        if (5000 > 0) {
            hideAtMs = SystemClock.uptimeMillis() + 5000;
            if (isAttachedToWindow) {
                postDelayed(hideAction, 5000);
            }
        } else {
            hideAtMs = C.TIME_UNSET;
        }
    }

    public void hideController() {
        centerButtonLayout.setVisibility(View.GONE);
        videoProgressLayout.setVisibility(View.GONE);
        durationlayout.setVisibility(View.GONE);
        videoMenuLayout.setVisibility(View.GONE);

        removeCallbacks(hideAction);
        hideAtMs = C.TIME_UNSET;
    }

    public void showController() {
        if (!isScreenLockEnable) {
            centerButtonLayout.setVisibility(View.VISIBLE);
            videoProgressLayout.setVisibility(View.VISIBLE);
            durationlayout.setVisibility(View.VISIBLE);
            videoMenuLayout.setVisibility(View.VISIBLE);
        }
        updateAll();
        requestPlayPauseFocus();
        hideAfterTimeout();

    }

    private void updateAll() {
        updatePlayPauseButton();
    }

    private void updatePlayPauseButton() {

        boolean requestPlayPauseFocus = false;
        boolean playing = mMediaPlayer != null && mMediaPlayer.getPlayWhenReady();
        if (videoPlayButton != null) {
            requestPlayPauseFocus |= playing && videoPlayButton.isFocused();
            videoPlayButton.setVisibility(playing ? View.GONE : View.VISIBLE);
        }
        if (videoPauseButton != null) {
            requestPlayPauseFocus |= !playing && videoPauseButton.isFocused();
            videoPauseButton.setVisibility(!playing ? View.GONE : View.VISIBLE);
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
        final AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

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

    // prepare video player and init views and view group here
    public void prepareVideoPlayer() throws Exception {
        if (contentType == null || mContentUrl == null)
            throw new Exception("Content type must not be null");

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

    // init view and view group here
    private void initViews() {
//        ToastMessage.showLogs(ToastMessage.LogType.ERROR, "Video Player:::", "initViews()");
        simpleExoPlayerView = this.findViewById(R.id.videoPlayer);
        videoRotationButton = this.findViewById(R.id.enter_full_screen);
        trackSelector = new DefaultTrackSelector(context);

        videoRotationButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                int orientation = getContext().getResources().getConfiguration().orientation;

                if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    ((Activity) getContext()).setRequestedOrientation(SCREEN_ORIENTATION_PORTRAIT);
                    ((Activity) getContext()).getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                    showSystemBar();
                    videoRotationButton.setImageResource(R.drawable.rotate);
                    videoLockButton.setVisibility(View.GONE);
                    videoUnLockButton.setVisibility(View.GONE);

                } else if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                    ((Activity) getContext()).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                    ((Activity) getContext()).getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                    hideSystemBars();
                    videoRotationButton.setImageResource(R.drawable.minimize);
                    videoLockUnlockStatus();

                }

            }
        });

        simpleExoPlayerView.setOnTouchListener(new OnSwipeTouchListener(context) {

            @Override
            public void onClick() {
                super.onClick();
                if (isScreenLockEnable) {
                    hideController();
                } else {
                    showController();
                }
                VideoPlayerTracer.error("Swipe:::", "onClick()");
            }

            @Override
            public void onSwipeLeft() {
                VideoPlayerTracer.error("Swipe:::", "onSwipeLeft()");
                super.onSwipeLeft();
            }

            @Override
            public void onSwipeRight() {
                VideoPlayerTracer.error("Swipe:::", "onSwipeRight()");
                super.onSwipeRight();
            }

            @Override
            public void onSwipeDown() {
                VideoPlayerTracer.error("Swipe:::", "onSwipeDown()");
                super.onSwipeDown();
            }

            @Override
            public void onSwipeUp() {
                VideoPlayerTracer.error("Swipe:::", "onSwipeUp()");
                super.onSwipeUp();
            }
        });

        this.findViewById(R.id.speed_btn).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                showSpeedControlDailog();
            }
        });
        // simpleExoPlayerView.set
        errorRetryLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                errorRetryLayout.setVisibility(GONE);
                initializeMainPlayer(mContentUrl, true);
            }
        });

        if (videoPlayerSdkCallBackListener != null)
            videoPlayerSdkCallBackListener.onPlayerReady(mContentUrl);

    }

    public void setMultiTvVideoPlayerSdkListener(VideoPlayerSdkCallBackListener videoPlayerSdkCallBackListener) {
        this.videoPlayerSdkCallBackListener = videoPlayerSdkCallBackListener;
    }

    public void setContentFilePath(String path) {
        this.mContentUrl = path;
    }

    public void setContentType(ContentType type) {
        this.contentType = type;
    }

    public void setPreRollAdUrl(String adUrl) {
        this.adsUrl = adUrl;

    }


    public void setAnalaticsUrl(String analaticsUrl) {
        this.analaticsUrl = analaticsUrl;
    }

    public void setKeyToken(String token) {
        this.token = token;
    }

    public void setAuthDetails(String id) {
        this.userId = id;
    }

    public void setContentTitle(String title) {
        this.contentTitle = title;
    }

    public void setContentId(String id) {
        this.contentId = id;
    }

    // get buffer duration of video in milli second
    public long getBufferingTimeInMillis() {
        return bufferingTimeInMillis;
    }

    public void setBufferingTimeInMillis(long bufferingTimeInMillis) {
        this.bufferingTimeInMillis = bufferingTimeInMillis;
    }

    public void setSubtitleVideoUri(String subtitleUri) {
        this.subTitleUri = subtitleUri;
    }

    public void setDrmEnabled(boolean drmContent, String siteId, String siteKey, String drmToekn, String drmLicenseUrl) {
        this.isDrmContent = drmContent;
        this.drmContentToken = drmToekn;
        this.drmdrmLicenseUrl = drmLicenseUrl;
        this.siteId = siteId;
        this.siteKey = siteKey;

    }


    // get play duration of video in milli second
    public long getContentPlayedTimeInMillis() {
        // contentPlayedTimeInMillis = 0;
        if (contentPlayedTimeInMillis == 0) {
            if (seekPlayerTo != 0)
                contentPlayedTimeInMillis = seekPlayerTo;
            else if (mMediaPlayer != null)
                contentPlayedTimeInMillis = mMediaPlayer.getCurrentPosition();
        } else {
            if (seekPlayerTo != 0)
                contentPlayedTimeInMillis = seekPlayerTo;
            else if (mMediaPlayer != null)
                contentPlayedTimeInMillis = mMediaPlayer.getCurrentPosition();
        }
        return contentPlayedTimeInMillis;
    }

    public void setContentPlayedTimeInMillis(long contentPlayedTimeInMillis) {
        this.contentPlayedTimeInMillis = contentPlayedTimeInMillis;
    }


    // start video player when player is ready state
    public void startVideoPlayer(boolean isNeedToPlayInstantly) {
        initializeMainPlayer(mContentUrl, isNeedToPlayInstantly);
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
            mMediaPlayer.setPlayWhenReady(false);
        }
    }

    // relase and destroy video player
    public void releaseVideoPlayer() {
        if (mMediaPlayer != null && simpleExoPlayerView != null) {

            sendAnalaticsData(context, userId, contentId, contentTitle, token);

            simpleExoPlayerView.getPlayer().release();
            mMediaPlayer.release();
            if (adsLoader != null)
                adsLoader.setPlayer(null);
        }
    }

    private void initializeMainPlayer(String videoUrl, boolean isNeedToPlayInstantly) {
//        ToastMessage.showLogs(ToastMessage.LogType.ERROR, "Video Player:::", "initializeMainPlayer");

        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            if (adsLoader != null)
                adsLoader.setPlayer(null);

            mMediaPlayer = null;
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


        if (adsUrl != null && !TextUtils.isEmpty(adsUrl)) {
            DataSource.Factory dataSourceFactory = new DefaultDataSource.Factory(context);
            MediaSourceFactory mediaSourceFactory =
                    new DefaultMediaSourceFactory(dataSourceFactory)
                            .setAdsLoaderProvider(unusedAdTagUri -> adsLoader)
                            .setAdViewProvider(simpleExoPlayerView);
            mMediaPlayer = new ExoPlayer.Builder(context).setMediaSourceFactory(mediaSourceFactory).setTrackSelector(trackSelector).setLoadControl(customLoadControl).build();
            adsLoader = new ImaAdsLoader.Builder(/* context= */ context).build();

        } else {
            mMediaPlayer = new ExoPlayer.Builder(context).setTrackSelector(trackSelector).setLoadControl(customLoadControl).build();
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
                subtitle =
                        new MediaItem.SubtitleConfiguration.Builder(Uri.parse(subTitleUri))
                                .setMimeType(MimeTypes.APPLICATION_SUBRIP) // The correct MIME type (required).
                                .setLanguage("en") // MUST, The subtitle language (optional).
                                .setSelectionFlags(C.SELECTION_FLAG_DEFAULT) //MUST,  Selection flags for the track (optional).
                                .build();
            }


            if (isDrmContent) {
                try {
                    WVMAgent = PallyconWVMSDKFactory.getInstance(context);
                    WVMAgent.init(context, null, siteId, siteKey);
                    WVMAgent.setPallyconEventListener(pallyconEventListener);
                } catch (PallyconDrmException e) {
                    e.printStackTrace();
                } catch (UnAuthorizedDeviceException e) {
                    e.printStackTrace();
                }

                mediaItem = new MediaItem.Builder().setUri(videoUrl).build();

                UUID drmSchemeUuid = UUID.fromString((C.WIDEVINE_UUID).toString());
                Uri uri = Uri.parse(videoUrl);


                try {
                    drmSessionManager = WVMAgent.createDrmSessionManagerByToken(
                            drmSchemeUuid,
                            drmdrmLicenseUrl,
                            uri,
                            drmContentToken);
                } catch (PallyconDrmException e) {
                    e.printStackTrace();
                }

                MediaSource playerMediaSource = new ExoUttils().buildMediaSource(context, mediaItem, videoUrl, drmSessionManager);
                mMediaPlayer.setMediaSource(playerMediaSource);
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
                        mediaItem = new MediaItem.Builder()
                                .setUri(videoUrl)
                                .setAdsConfiguration(new MediaItem.AdsConfiguration.Builder(adTagUri).build())
                                .build();
                    } else {
                        mediaItem = new MediaItem.Builder()
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


    Player.Listener stateChangeCallback1 = new ExoPlayer.Listener() {

        @Override
        public void onPlayerError(PlaybackException error) {
            Player.Listener.super.onPlayerError(error);
            if (mMediaPlayer != null && mMediaPlayer.getCurrentPosition() != 0)
                seekPlayerTo = (int) mMediaPlayer.getCurrentPosition() / 1000;

            errorRetryLayout.bringToFront();
            errorRetryLayout.setVisibility(VISIBLE);

            videoPlayerSdkCallBackListener.onPlayerError(error.getMessage());
        }

        @Override
        public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

        }

        @Override
        public void onLoadingChanged(boolean isLoading) {
        }

        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            String text = "Main player";

            updatePlayPauseButton();

            switch (playbackState) {
                case ExoPlayer.STATE_BUFFERING:
                    text += "buffering";
                    bufferingProgressBarLayout.bringToFront();
                    bufferingProgressBarLayout.setVisibility(VISIBLE);
                    centerButtonLayout.setVisibility(View.GONE);

                    if (contentType == ContentType.LIVE)
                        startBufferingTimer();

                    break;
                case ExoPlayer.STATE_ENDED:
                    text += "ended";
                    if (contentType == ContentType.VOD) {
                        if (mMediaPlayer != null)
                            contentPlayedTimeInMillis = mMediaPlayer.getCurrentPosition();

                        releaseVideoPlayer();
                        bufferingProgressBarLayout.setVisibility(GONE);
                        final FabButton circularProgressRing = (FabButton) circularProgressLayout.findViewById(R.id.circular_progress_ring);
                        circularProgressRing.showProgress(true);
                        circularProgressRing.setProgress(0);

                        circularProgressLayout.setVisibility(VISIBLE);
                        circularProgressLayout.bringToFront();

                        final int totalDuration = 10000, tickDuration = 1000;
                        countDownTimer = new CountDownTimerWithPause(totalDuration, tickDuration / 10, true) {
                            public void onTick(long millisUntilFinished) {
                                float progress = (float) millisUntilFinished / totalDuration;
                                progress = progress * 100;
                                progress = 100 - progress;
                                circularProgressRing.setProgress(progress);
                            }

                            public void onFinish() {
                                if (circularProgressLayout != null)
                                    circularProgressLayout.setVisibility(GONE);
                                try {
                                    prepareVideoPlayer();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    centerButtonLayout.setVisibility(View.VISIBLE);

                                }
                            }
                        }.create();
                    }


                    break;
                case ExoPlayer.STATE_IDLE:
                    text += "idle";

                    if (!checkForAudioFocus())
                        return;

                    if (bufferingProgressBarLayout != null)
                        bufferingProgressBarLayout.setVisibility(GONE);

                    centerButtonLayout.setVisibility(View.VISIBLE);

                    if (mMediaPlayer != null) {
                        contentPlayedTimeInMillis = mMediaPlayer.getCurrentPosition();
                        if (contentType == ContentType.LIVE)
                            startBufferingTimer();
                    }

                    simpleExoPlayerView.getVideoSurfaceView().setVisibility(VISIBLE);
                    simpleExoPlayerView.setVisibility(VISIBLE);
                    simpleExoPlayerView.bringToFront();


                    break;
                case ExoPlayer.STATE_READY:
                    text += "ready";
                    bufferingProgressBarLayout.setVisibility(GONE);
                    centerButtonLayout.setVisibility(View.VISIBLE);

                    videoNextButton.setVisibility(View.GONE);
                    videoPerviousButton.setVisibility(View.GONE);
                    break;
                default:
                    text += "unknown";
                    break;

            }

//            ToastMessage.showToastMsg(context, text, Toast.LENGTH_SHORT);
//            ToastMessage.showLogs(ToastMessage.LogType.ERROR, "Video Player:::", text);

        }

        @Override
        public void onRepeatModeChanged(int repeatMode) {
        }

        @Override
        public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

        }
    };

/*
    private void updateResolutionAudioSrtViewVisibility() {
        boolean isNeedToShowResolutionSelector = haveTracks(C.TRACK_TYPE_VIDEO);
        if (isNeedToShowResolutionSelector) {
            //simpleExoPlayerView.enableResolutionSelection();

            if (trackSelector == null || videoRendererIndex == -1)
                return;
            availableResolutionContainerList = new ArrayList<>();
            availableResolutionContainerMap = new HashMap<>();

            MappingTrackSelector.MappedTrackInfo mappedTrackInfo = trackSelector.getCurrentMappedTrackInfo();
            if (mappedTrackInfo != null) {
                videoTrackGroups = mappedTrackInfo.getTrackGroups(videoRendererIndex);
                TrackGroup group = videoTrackGroups.get(videoRendererIndex);
                if (group == null || group.length == 0)
                    return;
                boolean haveSupportedTracks = false;
                for (int trackIndex = 0; trackIndex < group.length; trackIndex++) {
                    String resolution = buildTrackName(group.getFormat(trackIndex));
                    if (!TextUtils.isEmpty(resolution) && !resolution.trim().equalsIgnoreCase("unknown")
                            && mappedTrackInfo.getTrackSupport(videoRendererIndex, videoRendererIndex, trackIndex)
                            == RendererCapabilities.FORMAT_HANDLED) {
                        haveSupportedTracks = true;

                        availableResolutionContainerMap.put(resolution, trackIndex);
                        availableResolutionContainerList.add(resolution);
                    }
                }

                if (availableResolutionContainerList != null && availableResolutionContainerList.size() > 1) {
                    if (haveSupportedTracks)
                        availableResolutionContainerList.add(0, "Auto");
                    else
                        availableResolutionContainerList.add(0, "Default");

                    Collections.sort(availableResolutionContainerList, Collections.<String>reverseOrder());

                    if (availableResolutionContainerList.contains("1080p")) {
                        availableResolutionContainerList.remove("1080p");
                        if (availableResolutionContainerList.contains("Auto"))
                            availableResolutionContainerList.add(1, "1080p");
                        else
                            availableResolutionContainerList.add(0, "1080p");
                    }
                }
            }
            */
    /*}*//*


            if (availableResolutionContainerList != null && !availableResolutionContainerList.isEmpty())
                handleResolutionItemClick(resolutionSelectedItemPosition);
        } else {
            ToastMessage.showToastMsg(context, "No Tracks available.", 1000);
        }
    }
*/

/*
    private boolean haveTracks(int type) {
        if (mMediaPlayer == null || trackSelector == null || trackSelector.getCurrentMappedTrackInfo() == null)
            return false;

        MappingTrackSelector.MappedTrackInfo mappedTrackInfo = trackSelector.getCurrentMappedTrackInfo();

        for (int i = 0; i < mappedTrackInfo.length; i++) {
            TrackGroupArray trackGroups = mappedTrackInfo.getTrackGroups(i);
            if (trackGroups.length != 0) {
                if (mMediaPlayer.getRendererType(i) == type) {
                    switch (type) {
                        case C.TRACK_TYPE_VIDEO:
                            videoRendererIndex = i;
                            break;
                        case C.TRACK_TYPE_AUDIO:
                            audioRendererIndex = i;
                            break;
                        case C.TRACK_TYPE_TEXT:
                            srtRendererIndex = i;
                            break;
                        default:
                            return false;
                    }

                    return true;
                }
            }
        }

        return false;
    }
*/

    private void startBufferingTimer() {
        if (bufferingTimeHandler == null) {
            bufferingTimeHandler = new Handler();
        }
        if (bufferingTimeRunnable != null)
            bufferingTimeHandler.postDelayed(bufferingTimeRunnable, 0);
    }

    public void stopBufferingTimer() {
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


    AlertDialog dialog = null;

    public void hideSpeedDailog() {
        if (dialog != null && dialog.isShowing())
            dialog.dismiss();
    }

    public void showSpeedControlDailog() {
        // setup the alert builder
        if (dialog != null && dialog.isShowing())
            dialog.dismiss();

        int position = sharedPreferencePlayer.getPreferencesInt(context, "player_playback_position", 2);


        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Speed Control");

        String[] animals = {"0.25x", "0.5x", "Normal", "1.5x", "2x"};

        builder.setSingleChoiceItems(animals, position, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                float speed = 1f;
                float pitch = 1f;
                switch (which) {
                    case 0:
                        speed = 0.25f;
                        pitch = 0.25f;
                        sharedPreferencePlayer.setPreferenceInt(context, "player_playback_position", 0);
                        break;
                    case 1:
                        speed = 0.5f;
                        pitch = 0.5f;
                        sharedPreferencePlayer.setPreferenceInt(context, "player_playback_position", 1);
                        break;
                    case 2:
                        speed = 1f;
                        pitch = 1f;
                        sharedPreferencePlayer.setPreferenceInt(context, "player_playback_position", 2);
                        break;
                    case 3:
                        speed = 1.5f;
                        pitch = 1.5f;
                        sharedPreferencePlayer.setPreferenceInt(context, "player_playback_position", 3);
                        break;
                    case 4:
                        speed = 2f;
                        pitch = 2f;
                        sharedPreferencePlayer.setPreferenceInt(context, "player_playback_position", 4);
                        break;
                }
                PlaybackParameters param = new PlaybackParameters(speed, pitch);
                mMediaPlayer.setPlaybackParameters(param);
                dialog.dismiss();
            }
        });

        dialog = builder.create();
        dialog.show();
    }



/*    public ArrayList<TrackResolution> getTrackResolution() {

        if (trackResolutionsList != null && trackResolutionsList.size() != 0)
            trackResolutionsList.clear();

        ArrayList<TempResolutionModel> allTrackResolutionsList = new ArrayList<>();
        TracksInfo trackInfo = mMediaPlayer.getCurrentTracksInfo();
        ImmutableList<TracksInfo.TrackGroupInfo> trackGroupInfo = trackInfo.getTrackGroupInfos();

        if (trackGroupInfo.size() != 0) {
            TracksInfo.TrackGroupInfo trackGroupResolution = trackGroupInfo.get(0);
            TrackGroup trackGroup = trackGroupResolution.getTrackGroup();

            for (int i = 0; i < trackGroup.length; i++) {
                if (trackGroupResolution.isTrackSupported(i)) {
                    allTrackResolutionsList.add(new TempResolutionModel(trackGroup.getFormat(i).width, trackGroup.getFormat(i).height));
                }
            }
            Log.e("TrackGroup lenght", trackGroup.length + "");
        }

        if (allTrackResolutionsList != null && allTrackResolutionsList.size() != 0) {
            trackResolutionsList.add(0, new TrackResolution(String.valueOf("Auto"), "Auto", "Auto"));
            for (int i = 0; i < allTrackResolutionsList.size(); i++) {
                String heightStr = String.valueOf(allTrackResolutionsList.get(i).getHeight());
                String widthStr = String.valueOf(allTrackResolutionsList.get(i).getWidth());
                if (trackResolutionsList != null && trackResolutionsList.size() <= 4)
                    trackResolutionsList.add(new TrackResolution(String.valueOf(allTrackResolutionsList.get(i).getWidth()), String.valueOf(allTrackResolutionsList.get(i).getHeight()), ""));


                for (int j = 0; j < trackResolutionsList.size(); j++) {
                    if (heightStr != null && heightStr.contains("1080"))
                        trackResolutionsList.remove(j);
                }


                if (heightStr != null && heightStr.contains("1080")) {
                    if (trackResolutionsList != null)
                        trackResolutionsList.add(trackResolutionsList.size() - 1, new TrackResolution(String.valueOf(widthStr), String.valueOf(heightStr), ""));
                }

            }
        }


        return trackResolutionsList;
    }*/

  /*  public void getSubTitle(){
        TracksInfo trackInfo = mMediaPlayer.getCurrentTracksInfo();
        ImmutableList<TracksInfo.TrackGroupInfo> trackGroupInfo = trackInfo.getTrackGroupInfos();
        TracksInfo.TrackGroupInfo  trackGroupResolution = trackGroupInfo.get(1);
    }*/


    @Override
    public void onClick(View view) {
        if (view == setting) {

            if (!isShowingTrackSelectionDialog
                    && TrackSelectionDialog.willHaveContent(trackSelector)) {
                isShowingTrackSelectionDialog = true;
                TrackSelectionDialog trackSelectionDialog =
                        TrackSelectionDialog.createForTrackSelector(
                                trackSelector,
                                /* onDismissListener= */ dismissedDialog -> isShowingTrackSelectionDialog = false);
                trackSelectionDialog.show(context.getSupportFragmentManager(), /* tag= */ null);
            }

        }
    }


    private void hideSystemBars() {
        final View decorView = ((Activity) getContext()).getWindow().getDecorView();
        final int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
    }


    private void showSystemBar() {
        View decorView = ((Activity) getContext()).getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_VISIBLE;
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


    @Override
    public void onScrubStart(PreviewBar previewBar) {
        //findViewById(R.id.centerButtonLayout).setVisibility(View.GONE);
        previewFrameLayout.setVisibility(View.VISIBLE);
        pauseVideoPlayer();
        removeCallbacks(hideAction);
    }

    @Override
    public void onScrubMove(PreviewBar previewBar, int progress, boolean fromUser) {
        //findViewById(R.id.centerButtonLayout).setVisibility(View.GONE);
        previewFrameLayout.setVisibility(View.VISIBLE);
        if (currentDurationPlayTv != null) {
            currentDurationPlayTv.setText(Util.getStringForTime(formatBuilder, formatter, progress));
        }
    }


    @Override
    public void onScrubStop(PreviewBar previewBar) {
        previewFrameLayout.setVisibility(View.GONE);
        //findViewById(R.id.centerButtonLayout).setVisibility(View.VISIBLE);
        if (mMediaPlayer != null) {
            seekTo(previewBar.getProgress());
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

    public void resumeFromPosition(long millisecondsForResume) {
        if (millisecondsForResume != 0) {
            this.millisecondsForResume = millisecondsForResume;
            //isResumeFromPreviousPosition = true;
        }
    }

}
