package com.multitv.ott.multitvvideoplayer.timebar.previewseekbar;

/**
 * Loads the previews for a {@link PreviewBar}
 */
public interface PreviewLoader {

    /**
     * This is called by a {@link PreviewBar} when the current progress
     * or the current maximum value has changed, either by user input or programmatically.
     *
     * This is only called when the preview is showing,
     * unlike {@link PreviewBar.OnScrubListener#onScrubMove(PreviewBar, int, boolean)}
     *
     * @param currentPosition the current position, between 0 and max
     * @param max             the maximum possible value
     */
    void loadPreview(long currentPosition, long max);

}