package com.multitv.ott.multitvvideoplayer;

import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;
import android.text.TextUtils;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.multitv.ott.multitvvideoplayer.utils.VideoPlayerNukeSSLCerts;


public class VideoPlayerAppController extends MultiDexApplication {
    public static final String TAG = VideoPlayerAppController.class.getSimpleName();
    private RequestQueue mRequestQueue;
    private static VideoPlayerAppController mInstance;


    @Override
    public void onCreate() {
        super.onCreate();

        mInstance = this;
        MultiDex.install(this);
        new VideoPlayerNukeSSLCerts().nuke();

    }

    public static synchronized VideoPlayerAppController getInstance() {
        return mInstance;
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        }

        return mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req, String tag) {
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
        getRequestQueue().add(req);
    }

    public <T> void addToRequestQueue(Request<T> req) {
        req.setTag(TAG);
        Log.e(TAG, "AppController.addToRequestQueue() " + req);
        getRequestQueue().add(req);
    }

    public void cancelPendingRequests(Object tag) {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(tag);
        }
    }

}
