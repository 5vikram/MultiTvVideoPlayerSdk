package com.multitv.ott.multitvvideoplayer.timebar.animator;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.view.View;
import android.widget.FrameLayout;

import com.multitv.ott.multitvvideoplayer.timebar.previewseekbar.PreviewBar;


/**
 * A {@link PreviewAnimator} that just fades the preview frame
 */
public class PreviewFadeAnimator implements PreviewAnimator {

    private static final int FADE_DURATION = 350;

    private long showDuration;
    private long hideDuration;

    public PreviewFadeAnimator() {
        this(FADE_DURATION, FADE_DURATION);
    }

    public PreviewFadeAnimator(long showDuration, long hideDuration) {
        this.showDuration = showDuration;
        this.hideDuration = hideDuration;
    }

    @Override
    public void move(FrameLayout previewView, PreviewBar previewBar) {

    }

    @Override
    public void show(FrameLayout previewView, PreviewBar previewBar) {
        previewView.animate().setListener(null);
        previewView.animate().cancel();
        previewView.setAlpha(0f);
        previewView.setVisibility(View.VISIBLE);
        previewView.animate()
                .setDuration(showDuration)
                .alpha(1f);
    }

    @Override
    public void hide(final FrameLayout previewView, PreviewBar previewBar) {
        previewView.animate().setListener(null);
        previewView.animate().cancel();
        previewView.setVisibility(View.VISIBLE);
        previewView.setAlpha(1f);
        previewView.animate()
                .setDuration(hideDuration)
                .alpha(0f)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        previewView.setAlpha(1.0f);
                        previewView.setVisibility(View.INVISIBLE);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        super.onAnimationCancel(animation);
                        previewView.setAlpha(1.0f);
                        previewView.setVisibility(View.INVISIBLE);
                    }
                });
    }

    @Override
    public void cancel(FrameLayout previewView, PreviewBar previewBar) {
        previewView.animate().setListener(null);
        previewView.animate().cancel();
    }

}