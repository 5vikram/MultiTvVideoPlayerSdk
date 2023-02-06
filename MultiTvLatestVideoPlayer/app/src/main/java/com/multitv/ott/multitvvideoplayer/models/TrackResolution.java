package com.multitv.ott.multitvvideoplayer.models;

public class TrackResolution {



    private String heightStr;
    private String widthStr;
    private String selction;


    public TrackResolution(String width, String height, String selction) {
        this.widthStr = width;
        this.heightStr = height;
        this.selction = selction;
    }

    public String getHeightStr() {
        return heightStr;
    }

    public void setHeightStr(String heightStr) {
        this.heightStr = heightStr;
    }

    public String getWidthStr() {
        return widthStr;
    }

    public void setWidthStr(String widthStr) {
        this.widthStr = widthStr;
    }

    public String getSelction() {
        return selction;
    }

    public void setSelction(String selction) {
        this.selction = selction;
    }
}
