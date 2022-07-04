package com.multitv.ott.multitvvideoplayer.models;

public class TrackResolution {

    private int width;

    private int height;

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }


    public TrackResolution(int width, int height) {
        this.width = width;
        this.height = height;
    }

}
