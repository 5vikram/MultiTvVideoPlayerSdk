package com.multitv.ott.multitvvideoplayer.timebar;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.google.android.exoplayer2.ui.DefaultTimeBar;
import com.google.android.exoplayer2.ui.TimeBar;
import com.multitv.ott.multitvvideoplayer.R;
import com.multitv.ott.multitvvideoplayer.timebar.animator.PreviewAnimator;
import com.multitv.ott.multitvvideoplayer.timebar.previewseekbar.PreviewBar;
import com.multitv.ott.multitvvideoplayer.timebar.previewseekbar.PreviewDelegate;
import com.multitv.ott.multitvvideoplayer.timebar.previewseekbar.PreviewLoader;


public class PreviewTimeBar extends DefaultTimeBar implements PreviewBar {

    private PreviewDelegate delegate;
    private int scrubProgress;
    private int duration;
    private int scrubberColor;
    private int previewId;
    private int scrubberPadding;

    public PreviewTimeBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray typedArray = context.getTheme().obtainStyledAttributes(attrs,
                com.google.android.exoplayer2.ui.R.styleable.DefaultTimeBar, 0, 0);
        scrubberColor = typedArray.getInt(
                com.google.android.exoplayer2.ui.R.styleable.DefaultTimeBar_scrubber_color,
                DEFAULT_SCRUBBER_COLOR);

        final Drawable scrubberDrawable = typedArray.getDrawable(
                com.google.android.exoplayer2.ui.R.styleable.DefaultTimeBar_scrubber_drawable);

        final int scrubberEnabledSize = typedArray.getDimensionPixelSize(
                com.google.android.exoplayer2.ui.R.styleable.DefaultTimeBar_scrubber_enabled_size,
                dpToPx(context.getResources().getDisplayMetrics(),
                        DEFAULT_SCRUBBER_ENABLED_SIZE_DP));

        final int scrubberDisabledSize = typedArray.getDimensionPixelSize(
                com.google.android.exoplayer2.ui.R.styleable.DefaultTimeBar_scrubber_disabled_size,
                dpToPx(context.getResources().getDisplayMetrics(),
                        DEFAULT_SCRUBBER_DISABLED_SIZE_DP));

        final int scrubberDraggedSize = typedArray.getDimensionPixelSize(
                com.google.android.exoplayer2.ui.R.styleable.DefaultTimeBar_scrubber_dragged_size,
                dpToPx(context.getResources().getDisplayMetrics(),
                        DEFAULT_SCRUBBER_DRAGGED_SIZE_DP));

        // Calculate the scrubber padding based on the maximum size the scrubber can have
        if (scrubberDrawable != null) {
            scrubberPadding = (scrubberDrawable.getMinimumWidth() + 1) / 2;
        } else {
            scrubberPadding =
                    (Math.max(scrubberDisabledSize,
                            Math.max(scrubberEnabledSize, scrubberDraggedSize)) + 1) / 2;
        }

        typedArray.recycle();

        typedArray = context.getTheme().obtainStyledAttributes(
                attrs, R.styleable.PreviewTimeBar, 0, 0);

        previewId = typedArray.getResourceId(
                R.styleable.PreviewTimeBar_previewFrameLayout, View.NO_ID);

        delegate = new PreviewDelegate(this);
        delegate.setPreviewEnabled(isEnabled());
        delegate.setAnimationEnabled(typedArray.getBoolean(
                R.styleable.PreviewTimeBar_previewAnimationEnabled, true));
        delegate.setPreviewEnabled(typedArray.getBoolean(
                R.styleable.PreviewTimeBar_previewEnabled, true));
        delegate.setAutoHidePreview(typedArray.getBoolean(
                R.styleable.PreviewTimeBar_previewAutoHide, true));

        typedArray.recycle();

        addListener(new TimeBarDefaultOnScrubListener());
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (!delegate.isPreviewViewAttached() && !isInEditMode()) {
            final FrameLayout previewView = PreviewDelegate.findPreviewView(
                    (ViewGroup) getParent(), previewId);
            if (previewView != null) {
                delegate.attachPreviewView(previewView);
            }
        }
    }

    @Override
    public void setPreviewThumbTint(int color) {
        setScrubberColor(color);
        scrubberColor = color;
    }

    @Override
    public void setPreviewThumbTintResource(int colorResource) {
        setPreviewThumbTint(ContextCompat.getColor(getContext(), colorResource));
    }

    @Override
    public void setPreviewLoader(PreviewLoader previewLoader) {
        delegate.setPreviewLoader(previewLoader);
    }

    @Override
    public void attachPreviewView(@NonNull FrameLayout previewView) {
        delegate.attachPreviewView(previewView);
    }

    @Override
    public void setDuration(long duration) {
        super.setDuration(duration);
        final int newDuration = (int) duration;
        if (newDuration != this.duration) {
            this.duration = newDuration;
            delegate.updateProgress(getProgress(), newDuration);
        }
    }

    @Override
    public void setPosition(long position) {
        super.setPosition(position);
        final int newPosition = (int) position;
        if (newPosition != scrubProgress) {
            this.scrubProgress = newPosition;
            delegate.updateProgress(newPosition, duration);
        }
    }

    @Override
    public boolean isShowingPreview() {
        return delegate.isShowingPreview();
    }

    @Override
    public boolean isPreviewEnabled() {
        return delegate.isPreviewEnabled();
    }

    @Override
    public void setPreviewEnabled(boolean enabled) {
        delegate.setPreviewEnabled(enabled);
    }

    @Override
    public void showPreview() {
        delegate.show();
    }

    @Override
    public void hidePreview() {
        delegate.hide();
    }

    @Override
    public void setAutoHidePreview(boolean autoHide) {
        delegate.setAutoHidePreview(autoHide);
    }

    @Override
    public int getProgress() {
        return scrubProgress;
    }

    @Override
    public int getMax() {
        return duration;
    }

    @Override
    public int getThumbOffset() {
        return scrubberPadding;
    }

    @Override
    public int getScrubberColor() {
        return scrubberColor;
    }

    @Override
    public void setScrubberColor(int scrubberColor) {
        super.setScrubberColor(scrubberColor);
        this.scrubberColor = scrubberColor;
    }

    @Override
    public void addOnScrubListener(PreviewBar.OnScrubListener listener) {
        delegate.addOnScrubListener(listener);
    }

    @Override
    public void removeOnScrubListener(PreviewBar.OnScrubListener listener) {
        delegate.removeOnScrubListener(listener);
    }

    @Override
    public void addOnPreviewVisibilityListener(PreviewBar.OnPreviewVisibilityListener listener) {
        delegate.addOnPreviewVisibilityListener(listener);
    }

    @Override
    public void removeOnPreviewVisibilityListener(PreviewBar.OnPreviewVisibilityListener listener) {
        delegate.removeOnPreviewVisibilityListener(listener);
    }

    @Override
    public void setPreviewAnimator(@NonNull PreviewAnimator animator) {
        delegate.setAnimator(animator);
    }

    @Override
    public void setPreviewAnimationEnabled(boolean enable) {
        delegate.setAnimationEnabled(enable);
    }

    private int dpToPx(DisplayMetrics displayMetrics, int dps) {
        return (int) (dps * displayMetrics.density + 0.5f);
    }

    /**
     * Listens for scrub events to show, hide or move the preview frame
     */
    private class TimeBarDefaultOnScrubListener implements TimeBar.OnScrubListener {

        @Override
        public void onScrubStart(TimeBar timeBar, long position) {
            scrubProgress = (int) position;
            delegate.onScrubStart();
        }

        @Override
        public void onScrubMove(TimeBar timeBar, long position) {
            scrubProgress = (int) position;
            delegate.onScrubMove((int) position, true);
        }

        @Override
        public void onScrubStop(TimeBar timeBar, long position, boolean canceled) {
            scrubProgress = (int) position;
            delegate.onScrubStop();
        }

    }
}