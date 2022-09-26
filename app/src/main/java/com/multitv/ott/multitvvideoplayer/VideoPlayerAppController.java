package com.multitv.ott.multitvvideoplayer;
import android.app.Application;
import android.text.TextUtils;
import android.util.Log;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.google.android.exoplayer2.util.Util;
import com.multitv.ott.multitvvideoplayer.utils.VideoPlayerNukeSSLCerts;

public class VideoPlayerAppController extends Application {
    public static final String TAG = VideoPlayerAppController.class.getSimpleName();
    private RequestQueue mRequestQueue;
    private static VideoPlayerAppController mInstance;
    protected String userAgent = "";

    @Override
    public void onCreate() {

        super.onCreate();
        mInstance = this;
        userAgent = Util.getUserAgent(this, "AdaptiveExoplayer");

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
