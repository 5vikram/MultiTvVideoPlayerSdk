package com.multitv.ott.multitvvideoplayer;

import static android.content.Context.TELEPHONY_SERVICE;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.Build;
import android.os.Handler;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.PlaybackException;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.ui.StyledPlayerView;
import com.multitv.ott.multitvvideoplayer.custom.CountDownTimerWithPause;
import com.multitv.ott.multitvvideoplayer.custom.ToastMessage;
import com.multitv.ott.multitvvideoplayer.database.SharedPreferencePlayer;
import com.multitv.ott.multitvvideoplayer.fabbutton.FabButton;
import com.multitv.ott.multitvvideoplayer.listener.VideoPlayerSdkCallBackListener;
import com.multitv.ott.multitvvideoplayer.popup.MyDialogFragment;
import com.multitv.ott.multitvvideoplayer.utils.CommonUtils;
import com.multitv.ott.multitvvideoplayer.utils.ContentType;
import com.multitv.ott.multitvvideoplayer.utils.ExoUttils;

import java.util.ArrayList;
import java.util.HashMap;

public class MultiTvPlayerSdk extends FrameLayout implements MyDialogFragment.ResolutionAudioSrtSelection {

    private Context context;
    private SharedPreferencePlayer sharedPreferencePlayer;
    private ContentType contentType;
    private ExoPlayer mMediaPlayer;
    private StyledPlayerView simpleExoPlayerView;
    private DefaultTrackSelector trackSelector;
    private VideoPlayerSdkCallBackListener videoPlayerSdkCallBackListener;

    private long millisecondsForResume, adPlayedTimeInMillis, contentPlayedTimeInMillis, bufferingTimeInMillis;
    private int seekPlayerTo;
    private String mContentUrl;
    private Handler bufferingTimeHandler;
    private CountDownTimerWithPause countDownTimer;
    private final String TAG = "VikramExoVideoPlayer";

    private LinearLayout errorRetryLayout, bufferingProgressBarLayout, circularProgressLayout;


    public ArrayList<String> availableResolutionContainerList, availableAudioTracksList,
            availableSrtTracksList;
    private HashMap<String, Integer> availableResolutionContainerMap;


    public MultiTvPlayerSdk(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MultiTvPlayerSdk(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        CommonUtils.setDefaultCookieManager();
        TelephonyManager mgr = (TelephonyManager) context.getSystemService(TELEPHONY_SERVICE);
//        if (mgr != null) {
//            mgr.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
//        }
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
        bufferingProgressBarLayout = view.findViewById(R.id.bufferingProgressBarLayout);
        circularProgressLayout = view.findViewById(R.id.circularProgressLayout);

        super.onFinishInflate();
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
        ToastMessage.showLogs(ToastMessage.LogType.ERROR, "Video Player:::", "initViews()");
        simpleExoPlayerView = this.findViewById(R.id.videoPlayer);
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

    // get buffer duration of video in milli second
    public long getBufferingTimeInMillis() {
        return bufferingTimeInMillis;
    }

    public void setBufferingTimeInMillis(long bufferingTimeInMillis) {
        this.bufferingTimeInMillis = bufferingTimeInMillis;
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
            simpleExoPlayerView.getPlayer().release();
            mMediaPlayer.release();
        }
    }

    private void initializeMainPlayer(String videoUrl, boolean isNeedToPlayInstantly) {
        ToastMessage.showLogs(ToastMessage.LogType.ERROR, "Video Player:::", "initializeMainPlayer");

        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            trackSelector = null;
            mMediaPlayer = null;
        }
        videoPlayerSdkCallBackListener.prepareVideoPlayer();
        ToastMessage.showLogs(ToastMessage.LogType.DEBUG, TAG, "Content url is " + videoUrl);
        mMediaPlayer = new ExoPlayer.Builder(context).build();
        if (mMediaPlayer != null) {
            mMediaPlayer.addListener(stateChangeCallback1);
            simpleExoPlayerView.setPlayer(mMediaPlayer);
            MediaSource playerMediaSource = new ExoUttils().buildMediaSource(1, context, videoUrl);
            mMediaPlayer.prepare(playerMediaSource);

            if (isNeedToPlayInstantly) {
                mMediaPlayer.setPlayWhenReady(true);
            }

        }


    }


    ExoPlayer.EventListener stateChangeCallback1 = new ExoPlayer.EventListener() {

        @Override
        public void onPlayerError(PlaybackException error) {
            Player.EventListener.super.onPlayerError(error);
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
            switch (playbackState) {
                case ExoPlayer.STATE_BUFFERING:
                    text += "buffering";
                    bufferingProgressBarLayout.bringToFront();
                    bufferingProgressBarLayout.setVisibility(VISIBLE);

                    if (contentType == ContentType.LIVE)
                        startBufferingTimer();

                    break;
                case ExoPlayer.STATE_ENDED:
                    text += "ended";
                    if (contentType == ContentType.VOD) {
                        if (mMediaPlayer != null)
                            contentPlayedTimeInMillis = mMediaPlayer.getCurrentPosition();

                        releaseVideoPlayer();

                        final FabButton circularProgressRing = (FabButton) circularProgressLayout.findViewById(R.id.circular_progress_ring);
                        circularProgressRing.showProgress(true);
                        circularProgressRing.setProgress(0);

                        circularProgressLayout.setVisibility(VISIBLE);
                        circularProgressLayout.bringToFront();

                        final int totalDuration = 30000, tickDuration = 1000;
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
                    //videoPlayerSdkCallBackListener.onPlayerReady(mContentUrl);
                    break;
                default:
                    text += "unknown";
                    break;

            }

            ToastMessage.showToastMsg(context, text, Toast.LENGTH_SHORT);
            ToastMessage.showLogs(ToastMessage.LogType.ERROR, "Video Player:::", text);

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

        int position = sharedPreferencePlayer.getPreferencesInt(context, "pos");


        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Speed Control");

// add a checkbox list
        String[] animals = {"1x", "0.5x", "0.75x", "1.25x", "1.5x", "2x"};

        builder.setSingleChoiceItems(animals, position, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                float speed = 1f;
                float pitch = 1f;
                switch (which) {
                    case 1:
                        speed = 0.5f;
                        pitch = 0.5f;
                        sharedPreferencePlayer.setPreferenceInt(context, "pos", 1);
                        break;
                    case 2:
                        speed = 0.75f;
                        pitch = 0.75f;
                        sharedPreferencePlayer.setPreferenceInt(context, "pos", 2);
                        break;
                    case 0:
                        speed = 1f;
                        pitch = 1f;
                        sharedPreferencePlayer.setPreferenceInt(context, "pos", 0);
                        break;
                    case 3:
                        speed = 1.25f;
                        pitch = 1.25f;
                        sharedPreferencePlayer.setPreferenceInt(context, "pos", 3);
                        break;

                    case 4:
                        speed = 1.5f;
                        pitch = 1.5f;
                        sharedPreferencePlayer.setPreferenceInt(context, "pos", 4);
                        break;

                    case 5:
                        speed = 2f;
                        pitch = 2f;
                        sharedPreferencePlayer.setPreferenceInt(context, "pos", 5);
                        break;
                    default:
                        speed = 1f;
                        pitch = 1f;
                        sharedPreferencePlayer.setPreferenceInt(context, "pos", 2);
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


    public void showMenuDailog() {
        //new ResolutionDailog().showResolutionDailog(context, this);
    }


    @Override
    public void onResolutionAudioSrtSelection(String index, int selectedItemPosition, int typeOfSelection) {
/*
        switch (typeOfSelection) {
            case 0:
                this.resolutionSelectedItemPosition = selectedItemPosition;
                handleResolutionItemClick(selectedItemPosition);
                break;
            case 1:
                this.audioSelectedItemPosition = selectedItemPosition;
                handleAudioItemClick(selectedItemPosition);
                break;
            case 2:
                this.srtSelectedItemPosition = selectedItemPosition;
                handleSrtItemClick(selectedItemPosition);
                break;
        }
*/
    }

/*
    private void handleResolutionItemClick(int selectedItemPosition) {
        if (mMediaPlayer == null || availableResolutionContainerList == null || availableResolutionContainerList.isEmpty()
                || availableResolutionContainerMap == null || availableResolutionContainerMap.isEmpty()
                || videoRendererIndex == -1 || videoTrackGroups == null
                || selectedItemPosition == previousResolutionSelectedItemPosition) {
            //Log.e("Naseeb", "All values are null & videoRendererIndex= " + String.valueOf(videoRendererIndex) + " & videoTrackGroups =" + String.valueOf(videoTrackGroups));

            return;
        }

        String resolution = availableResolutionContainerList.get(selectedItemPosition);
        //Log.e("Naseeb", "Selected resolution::::" + resolution);
        MappingTrackSelector.SelectionOverride selectionOverride = null;
        if (!resolution.equalsIgnoreCase("Auto") && !resolution.equalsIgnoreCase("Default")) {
            int trackIndex = availableResolutionContainerMap.get(resolution);
            //Log.e("Naseeb", "Selected trackIndex::::" + trackIndex);

            selectionOverride = new MappingTrackSelector.SelectionOverride(fixedFactory, videoRendererIndex, trackIndex);
        }

        if (selectionOverride != null) {
            //trackSelector.clearSelectionOverride(videoRendererIndex, videoTrackGroups);
            trackSelector.setSelectionOverride(videoRendererIndex, videoTrackGroups, selectionOverride);
            Log.e("Vikram ", "Resolution is set to " + resolution);
        } else {
            trackSelector.clearSelectionOverrides(videoRendererIndex);
            Log.e("Vikram ", "Resolution is set to auto");
        }

        previousResolutionSelectedItemPosition = selectedItemPosition;
    }
*/

}
