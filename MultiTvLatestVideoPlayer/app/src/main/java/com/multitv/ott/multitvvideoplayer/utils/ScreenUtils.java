package com.multitv.ott.multitvvideoplayer.utils;

import android.content.Context;
import android.graphics.Point;
import android.view.Display;
import android.view.WindowManager;

/**
 * Created by naseeb on 10/6/2016.
 */

public class ScreenUtils {

    public static int getScreenWidth(Context context) {
        int screenWidth = 0;

        try {
            WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            Display display = wm.getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            screenWidth = size.x;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return screenWidth;
    }

    public static int getScreenHeight(Context context) {
        int screenHeight = 0;

        try {
            WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            Display display = wm.getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            screenHeight = size.y;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return screenHeight;
    }


    public static String getMobileNumber(String mobileNumber) {
        if (mobileNumber.startsWith("0")) {
            mobileNumber.replaceFirst("(?:0)+", "");
        } else if (mobileNumber.startsWith("+91")) {
            mobileNumber.replaceFirst("(?:+91)+", "");
        }else if (mobileNumber.startsWith("91")) {
            mobileNumber.replaceFirst("(?:91)+", "");
        }
        return mobileNumber;
    }
}
