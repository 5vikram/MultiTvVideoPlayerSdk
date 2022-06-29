package com.multitv.ott.multitvvideoplayer;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

public class MultiTvPlayerSdk extends FrameLayout {

    private Context context;

    public MultiTvPlayerSdk(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MultiTvPlayerSdk(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
    }


    @Override
    protected void onFinishInflate() {
        //View view = LayoutInflater.from(getContext()).inflate(R.layout.player, this);
        super.onFinishInflate();
    }
}
