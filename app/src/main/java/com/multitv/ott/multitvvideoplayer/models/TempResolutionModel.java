package com.multitv.ott.multitvvideoplayer.models;

public class TempResolutionModel {


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

    public TempResolutionModel(int width, int height) {
        this.width = width;
        this.height = height;
    }


}
