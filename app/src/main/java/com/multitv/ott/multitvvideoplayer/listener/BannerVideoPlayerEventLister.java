package com.multitv.ott.multitvvideoplayer.listener;

public interface BannerVideoPlayerEventLister {
    void onPlayerReady(String contentUrl);

    void onPlayNextVideo();

    void onPlayClick(int play);

    void onPlayerError(String message);

    void prepareVideoPlayer();

    void subscriptionCallBack();

    void showThumbnailCallback();

    void fullScreenCallBack();

    void onVideoStartNow();

    void onVideoBufferCallBack();

    void moreButtonClickListener();

    void onVideoScreenResolution(int width, int height);

    void videoPlayerClick();
}
