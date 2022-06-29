package com.multitv.ott.multitvvideoplayer.timebar.animator;

import android.widget.FrameLayout;

import com.multitv.ott.multitvvideoplayer.timebar.previewseekbar.PreviewBar;


public interface PreviewAnimator {

    /**
     * Use {@link PreviewBar#getProgress()} and {@link PreviewBar#getMax()}
     * to determine how much the preview should move
     *
     * @param previewView The view that displays the preview
     * @param previewBar  The PreviewBar that's responsible for this preview
     */
    void move(FrameLayout previewView, PreviewBar previewBar);

    /**
     * Animates the preview appearance.
     * <p>
     * Please note that any animations started by
     * {@link PreviewAnimator#move(FrameLayout, PreviewBar)}
     * or {@link PreviewAnimator#hide(FrameLayout, PreviewBar)} might still be running
     * <p>
     *
     * @param previewView The view that displays the preview
     * @param previewBar  The PreviewBar that's responsible for this preview
     */
    void show(FrameLayout previewView, PreviewBar previewBar);

    /**
     * Animates the preview disappearance.
     * <p>
     * Please note that any animations started by
     * {@link PreviewAnimator#move(FrameLayout, PreviewBar)}
     * or {@link PreviewAnimator#show(FrameLayout, PreviewBar)} might still be running
     * <p>
     *
     * @param previewView The view that displays the preview
     * @param previewBar  The PreviewBar that's responsible for this preview
     */
    void hide(FrameLayout previewView, PreviewBar previewBar);

    /**
     * Cancels any animation started by {@link PreviewAnimator#move(FrameLayout, PreviewBar)},
     * {@link PreviewAnimator#show(FrameLayout, PreviewBar)}
     * or {@link PreviewAnimator#hide(FrameLayout, PreviewBar)}
     *
     * @param previewView The view that displays the preview
     * @param previewBar  The PreviewBar that's responsible for this preview
     */
    void cancel(FrameLayout previewView, PreviewBar previewBar);

}