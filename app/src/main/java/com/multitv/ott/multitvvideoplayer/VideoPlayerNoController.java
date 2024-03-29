package com.multitv.ott.multitvvideoplayer;

import static android.content.Context.TELEPHONY_SERVICE;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackException;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.drm.DrmSessionManager;
import com.google.android.exoplayer2.ext.ima.ImaAdsLoader;
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.MediaSourceFactory;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.ui.StyledPlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSource;
import com.google.android.exoplayer2.util.MimeTypes;
import com.google.common.collect.ImmutableList;
import com.multitv.ott.multitvvideoplayer.custom.CountDownTimerWithPause;
import com.multitv.ott.multitvvideoplayer.database.SharedPreferencePlayer;
import com.multitv.ott.multitvvideoplayer.listener.VideoPlayerSdkCallBackListener;
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

import java.util.Formatter;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class VideoPlayerNoController extends FrameLayout implements View.OnClickListener {

    private Activity context;
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

    private LinearLayout errorRetryLayout, bufferingProgressBarLayout, centerButtonLayout;



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

    public VideoPlayerNoController(Context context, AttributeSet attrs) {
        this((AppCompatActivity) context, attrs, 0);
    }

    public VideoPlayerNoController(AppCompatActivity context, AttributeSet attrs, int defStyleAttr) {

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
        View view = LayoutInflater.from(getContext()).inflate(R.layout.short_video_player_layout, this);
        errorRetryLayout = view.findViewById(R.id.errorRetryLayout);
        bufferingProgressBarLayout = view.findViewById(R.id.bufferingProgressBarLayout);
        centerButtonLayout = view.findViewById(R.id.centerButtonLayout);
        centerButtonLayout.setVisibility(View.GONE);



        findViewById(R.id.mute).requestFocus();

      /*  findViewById(R.id.unmute).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {

                mMediaPlayer.getAudioComponent().setVolume(mMediaPlayer.getAudioComponent().getVolume());
                findViewById(R.id.unmute).setVisibility(View.GONE);
                findViewById(R.id.mute).setVisibility(View.VISIBLE);
                findViewById(R.id.mute).requestFocus();
            }
        });

        findViewById(R.id.mute).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                findViewById(R.id.unmute).requestFocus();
                mMediaPlayer.getAudioComponent().setVolume(0f);
                findViewById(R.id.unmute).setVisibility(View.VISIBLE);
                findViewById(R.id.mute).setVisibility(View.GONE);
            }
        });*/



        super.onFinishInflate();
    }

    public void unmuteVideo(){
        mMediaPlayer.getAudioComponent().setVolume(mMediaPlayer.getAudioComponent().getVolume());
        mMediaPlayer.getAudioComponent().setVolume(2f);
    }

    public void muteVideo(){
        mMediaPlayer.getAudioComponent().setVolume(0f);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        updateAll();
    }

    private void updateAll() {
        updatePlayPauseButton();
    }

    private void updatePlayPauseButton() {

        boolean requestPlayPauseFocus = false;
        boolean playing = mMediaPlayer != null && mMediaPlayer.getPlayWhenReady();

        if (requestPlayPauseFocus) {
            requestPlayPauseFocus();
        }
    }

    private void requestPlayPauseFocus() {
        boolean playing = mMediaPlayer != null && mMediaPlayer.getPlayWhenReady();

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

    // init view and view group here
    private void initViews() {
        simpleExoPlayerView = this.findViewById(R.id.videoPlayer);
        trackSelector = new DefaultTrackSelector(context);
        simpleExoPlayerView.setControllerShowTimeoutMs(5000);
        simpleExoPlayerView.setControllerHideOnTouch(true);
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

           // sendAnalaticsData(context, userId, contentId, contentTitle, contentTitle);

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


        if (adsUrl != null && !TextUtils.isEmpty(adsUrl)) {
            DataSource.Factory dataSourceFactory = new DefaultDataSource.Factory(context);
            MediaSourceFactory mediaSourceFactory =
                    new DefaultMediaSourceFactory(dataSourceFactory)
                            .setAdsLoaderProvider(unusedAdTagUri -> adsLoader)
                            .setAdViewProvider(simpleExoPlayerView);
            mMediaPlayer = new ExoPlayer.Builder(context).setMediaSourceFactory(mediaSourceFactory).setTrackSelector(trackSelector).build();
            adsLoader = new ImaAdsLoader.Builder(/* context= */ context).build();
        } else {
            mMediaPlayer = new ExoPlayer.Builder(context).setTrackSelector(trackSelector).build();
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
                if (subtitle != null && subtitle.uri != null) {
                    /*MediaSource playerMediaSource = new ExoUttils().buildMediaSource(context, mediaItem, videoUrl, drmSessionManager);
                    MediaSource mediaSource = new MergingMediaSource(mediaSources);
                    mMediaPlayer.setMediaSource(mediaSource);*/

                    if (adsUrl != null && !TextUtils.isEmpty(adsUrl)) {
                        adsLoader.setPlayer(mMediaPlayer);
                        Uri adTagUri = Uri.parse(adsUrl);
                        mediaItem = new MediaItem.Builder()
                                /*.setSubtitleConfigurations(ImmutableList.of(subtitle))*/
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
                        Uri adTagUri = Uri.parse(adsUrl);
                        mediaItem = new MediaItem.Builder()
                                .setSubtitleConfigurations(ImmutableList.of(subtitle))
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
                    /*bufferingProgressBarLayout.bringToFront();
                    bufferingProgressBarLayout.setVisibility(VISIBLE);*/
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
                        videoPlayerSdkCallBackListener.onPlayNextVideo();
                        /*bufferingProgressBarLayout.setVisibility(GONE);*/

                    }


                    break;
                case ExoPlayer.STATE_IDLE:
                    text += "idle";

                    if (!checkForAudioFocus())
                        return;

                    if (bufferingProgressBarLayout != null)
                        bufferingProgressBarLayout.setVisibility(GONE);

                    centerButtonLayout.setVisibility(View.GONE);

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
                    centerButtonLayout.setVisibility(View.GONE);

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

    public void resumeFromPosition(long millisecondsForResume) {
        if (millisecondsForResume != 0) {
            this.millisecondsForResume = millisecondsForResume;
            //isResumeFromPreviousPosition = true;
        }
    }

}
