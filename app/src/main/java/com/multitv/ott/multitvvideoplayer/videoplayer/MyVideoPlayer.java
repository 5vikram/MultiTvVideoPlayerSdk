package com.multitv.ott.multitvvideoplayer.videoplayer;


import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.google.android.exoplayer2.ui.StyledPlayerView;

public class MyVideoPlayer extends StyledPlayerView {

    private boolean isTouching;

    public MyVideoPlayer(Context context) {
        super(context);
    }

    public MyVideoPlayer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyVideoPlayer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                isTouching = true;
                return true;
            case MotionEvent.ACTION_UP:
                if (isTouching) {
                    isTouching = false;
                    performClick();
                    return true;
                }
                return false;
            default:
                return false;
        }
    }
}
