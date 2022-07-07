package com.multitv.ott.multitvvideoplayer.utils;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.DisplayMetrics;

import androidx.core.os.ConfigurationCompat;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;


public class AppSessionUtil {

    public static void sendHeartBeat(final Activity activity, final String userId, final String url, final String contentId, final String contentName,
                                     final long contentPlayedDuration, final long contentBufferDuration,
                                     final long totalDuration, final String token) {

        VideoPlayerTracer.error("App heart beat", "App heart beat url : " + url);

        StringRequest jsonObjReq = new StringRequest(Request.Method.POST,
                url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                VideoPlayerTracer.error("App heart beat api---", response);

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VideoPlayerTracer.error("App heart beat", "Error: " + error.getMessage());
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();

                try {
                    if (!TextUtils.isEmpty(userId))
                        params.put("u_id", userId);
                    else
                        params.put("u_id", "0");

                    params.put("token", token);
                    params.put("c_id", contentId);

                    if (contentBufferDuration != 0)
                        params.put("buff_d", "" + contentBufferDuration);
                    else
                        params.put("buff_d", "0");

                    String ddJson = "";
                    JSONObject ddJsonObject = new JSONObject();
                    ddJsonObject.put("make_model", Build.MODEL);
                    ddJsonObject.put("os", "android");
                    //Resolution
                    DisplayMetrics displaymetrics = new DisplayMetrics();
                    activity.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
                    int height = displaymetrics.heightPixels;
                    int width = displaymetrics.widthPixels;
                    ddJsonObject.put("screen_resolution", height + "*" + width);
                    //Firebase token
                    String firebaseToken = "";
                    ddJsonObject.put("push_device_token", !TextUtils.isEmpty(firebaseToken) ? firebaseToken : "");
                    ddJsonObject.put("device_type", "app");
                    ddJsonObject.put("platform", "android");
                    //IMEI
                    String imei = Settings.Secure.getString(activity.getContentResolver(), Settings.Secure.ANDROID_ID);
                    //String imei = ((TelephonyManager) activity.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
                    ddJsonObject.put("device_unique_id", !TextUtils.isEmpty(imei) ? imei : "");

                    ddJson = ddJsonObject.toString();
                    if (!TextUtils.isEmpty(ddJson))
                        params.put("dd", ddJson);


                    JSONObject jsonObject = new JSONObject();
                    final int os_version_code = Build.VERSION.SDK_INT;
                    jsonObject.put("os_version", "" + os_version_code);

                    String appVersion = "";

                    PackageInfo packageInfo = activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0);
                    appVersion = packageInfo.versionName;


                    if (!TextUtils.isEmpty(appVersion))
                        jsonObject.put("app_version", appVersion);

                    final String networkType = "";

                    String carrierName = "Unknown";

                    jsonObject.put("network_type", networkType);
                    jsonObject.put("network_provider", carrierName);

                    String dodjson = jsonObject.toString();
                    params.put("dod", dodjson);


                    params.put("type", "2");


                    Locale locale = ConfigurationCompat.getLocales(Resources.getSystem().getConfiguration()).get(0);
                    String countryName = locale.getDisplayCountry();
                    String countryCode = locale.getCountry();
                    params.put("country", countryName);
                    params.put("country_code", countryCode);


                    String ageGroup = "other";


                    String totalPlayerDuration = "";
                    String contentPlayDurationSecond = "";
                    if (contentPlayedDuration > 0) {
                        long seconds = TimeUnit.MILLISECONDS.toSeconds(contentPlayedDuration);
                        if (seconds > 0)
                            contentPlayDurationSecond = contentPlayDurationSecond + "" + seconds;
                    }

                    if (totalDuration > 0) {
                        long hours = TimeUnit.MILLISECONDS.toHours(totalDuration);
                        long minutes = TimeUnit.MILLISECONDS.toMinutes(totalDuration);
                        long seconds = TimeUnit.MILLISECONDS.toSeconds(totalDuration);


                        if (seconds > 0)
                            totalPlayerDuration = totalPlayerDuration + "" + seconds;
                        else
                            totalPlayerDuration = "0";

                        VideoPlayerTracer.error("Player total duration1111===", totalPlayerDuration);
                    }


                    //long contentPlayedDuration = AppController.getInstance().getContentPlayedDuration() / 1000;
                    if (!TextUtils.isEmpty(contentPlayDurationSecond) && !contentPlayDurationSecond.equalsIgnoreCase("0")) {
                        params.put("pd", "" + contentPlayDurationSecond);
                    }


                    params.put("customer_name", "");
                    params.put("content_title", contentName);
                    params.put("total_duration", "" + totalPlayerDuration);
                    params.put("age_group", ageGroup);

                    Set<String> keys = params.keySet();
                    for (String key : keys) {
                        VideoPlayerTracer.error("com.android.playersdk.utils.AppSessionUtil", "sendHeartBeat().getParams: " + key + "      " + params.get(key));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    VideoPlayerTracer.error("App heart beat", "Error: " + e.getMessage());
                }

                return params;
            }
        };

        jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(20 * 1000, 3,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        // Adding request to request queue


        //AppController.getInstance().getSessionHandler().postDelayed(this, 1000 * 60 * 10);
    }
}
