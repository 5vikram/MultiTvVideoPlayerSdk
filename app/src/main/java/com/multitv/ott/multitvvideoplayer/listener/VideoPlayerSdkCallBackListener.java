package com.multitv.ott.multitvvideoplayer.listener;

public interface VideoPlayerSdkCallBackListener {
    void onPlayerReady(String contentUrl);

    void onPlayNextVideo();

    void onPlayClick();
    void onPlayClick(int status);

    void onControllerVisibilityChanges(int isShown);

    void onPlayerError(String message);

    void prepareVideoPlayer();

    void subscriptionCallBack();

    void showThumbnailCallback();

    void showEpisodeListData();


    //void showSpeedControllMenu();
}
