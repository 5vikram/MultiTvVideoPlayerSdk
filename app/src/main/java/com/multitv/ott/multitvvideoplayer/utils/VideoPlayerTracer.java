package com.multitv.ott.multitvvideoplayer.utils;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;


/**
 * Created by naseeb on 8/25/2017.
 */

public class VideoPlayerTracer {
    private final static Boolean LOG_ENABLE = true;

    /**
     * Method to print debug log<br>
     * {@link Log} Information
     *
     * @param TAG
     * @param message
     */
    public static void debug(String TAG, String message) {
        if (LOG_ENABLE && !TextUtils.isEmpty(message)) {
            Log.d(TAG, message);
        }
    }

    /**
     * Method to print error log<br>
     * {@link Log} Error
     *
     * @param TAG
     * @param message
     */
    public static void error(String TAG, String message) {
        if (LOG_ENABLE && !TextUtils.isEmpty(message)) {
            Log.e(TAG, message);
        }
    }

    /**
     * Method to print information log<br>
     * {@link Log} Debug
     *
     * @param TAG
     * @param message
     */
    public static void info(String TAG, String message) {
        if (LOG_ENABLE && !TextUtils.isEmpty(message)) {
            Log.i(TAG, message);
        }
    }

    /**
     * Show Toast<br>
     *
     * @param TAG
     * @param message
     */
    public static void showToastUnderTesting(Context context, String TAG, String message) {
        if (LOG_ENABLE && !TextUtils.isEmpty(message)) {
            Toast toast = Toast.makeText(context, TAG + "\n" + message, Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        }
    }

    /**
     * Show Toast
     *
     * @param context
     * @param message
     * @param isLong  TRUE if show long toast else FALSE
     */
    public static void showToastProduction(Context context, String message, boolean isLong) {
        Toast toast = Toast.makeText(context, message, isLong ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }
}
