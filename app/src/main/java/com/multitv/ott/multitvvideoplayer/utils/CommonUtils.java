package com.multitv.ott.multitvvideoplayer.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Build;
import android.view.Display;


import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class CommonUtils {
    //	private String trackerKey = "http://admin.multitvsolution.com/multitvfinal/api/impression/ck/token/41c37b827b209dd38fbb79c/publisherKey/";
//	private String ak = "f0f5026439f45dd9aebf3227657a74e8";
//	private String category = "";
//    private String deviceIdFormet = "dpid";
//    private String appBundle = "com.multitv.ibn";
//    private String appName = "IBN";
    private String MY_PREF = "MYSDK_M";
    private static String TAG = "CommonUtils";
//    private String adFormet = "preappvideo";

    private static final CookieManager defaultCookieManager;

    static {
        defaultCookieManager = new CookieManager();
        defaultCookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ORIGINAL_SERVER);
    }



    public Bitmap getColoredImage(Bitmap bitmap, String ColorCode) {
        final Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(output);

        final int color = Color.parseColor(ColorCode);
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

        final RectF rectF = new RectF(rect);

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 255, 255, 255);
        paint.setColor(color);
        canvas.drawOval(rectF, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_ATOP));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        bitmap.recycle();

        return output;
    }

    /*public RendererBuilder getRendererBuilder(Context context, String url, Uri subtitleUri,
                                              boolean isHLS) {
        String userAgent = Util.getUserAgent(context, "MultiTVPlayer");
        if (subtitleUri != null) {
            if (isHLS) {
                return new HlsRendererBuilder(context, userAgent, url, subtitleUri);
            } else {
                return new ExtractorRendererBuilder(context, userAgent,
                        Uri.parse(url), subtitleUri);
            }
        } else {
            if (isHLS) {
                return new HlsRendererBuilder(context, userAgent, url);
            } else {
                return new ExtractorRendererBuilder(context, userAgent,
                        Uri.parse(url));
            }
        }
    }*/

    /*public String prepareAdUrl(String networkID, String baseUrl,
                               String advertisingId, Context ctx)
            throws UnsupportedEncodingException {
        String requestUrl = null;
        if (networkID == null)
            return null;
        switch (networkID) {
            case "2":
                DeviceInfo info = new DeviceInfo(ctx);

                requestUrl = baseUrl + "&cb=" + System.currentTimeMillis()
                        + "&latlong=" + URLEncoder.encode(info.getLatLong())
                        + "&di=" + URLEncoder.encode(info.getDeviceId(), "utf-8")
                        + "&dif=" + deviceIdFormet + "&appBundle="
                        + URLEncoder.encode(appBundle, "utf-8")
                        + "&channelType=app&appName=" + appName;

                break;

            case "3":
                requestUrl = baseUrl;

                break;

            case "1":
                requestUrl = "http://pv.tubemogul.com/--nt/vast2/hV7wUal5uPNfPxYsnSpr/?duration=30&rtb_type=instream_mobile_vast_inter&"
                        + "app_name="
                        + appName
                        + "&app_id="
                        + URLEncoder.encode(appBundle, "utf-8")
                        + "&user_id="
                        + URLEncoder.encode(advertisingId, "utf-8")
                        + "&timestamp="
                        + URLEncoder.encode("" + System.currentTimeMillis(),
                        "utf-8");
                break;

            default:

                requestUrl = "http://pubads.g.doubleclick.net/gampad/ads?env=vp&gdfp_req=1&impl=s&output=vast&iu=/6062/video-demo&sz=400x300&unviewed_position_start=1&url=http://www.simplevideoad.com&ciu_szs=728x90,300x250&correlator=7108";
                break;

        }
        return requestUrl;
    }*/

    public static String convertDateFormat(long millis) {
        try {
            Calendar calender = Calendar.getInstance();
            calender.setTimeInMillis(millis);
            Date date = calender.getTime();

            SimpleDateFormat formatter = new SimpleDateFormat(
                    "yyyy-MM-dd HH:mm:ss.SSS");

            return formatter.format(date);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }


    public Point calculateScreenDimension(Context ctx) {

        Display display = ((Activity) ctx).getWindowManager().getDefaultDisplay();
        if (Integer.valueOf(android.os.Build.VERSION.SDK_INT) > 13) {
            Point size = new Point();
            display.getSize(size);
            return size;
        }
        return null;
    }

    public String getAdNetwork(ArrayList<String> list) {
        Random random = new Random();
        int index = random.nextInt(list.size());

        return list.get(index);
    }

    public static boolean isAppIsInBackground(Context context) {
        boolean isInBackground = true;
        try {
            ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
                List<ActivityManager.RunningAppProcessInfo> runningProcesses = am.getRunningAppProcesses();
                for (ActivityManager.RunningAppProcessInfo processInfo : runningProcesses) {
                    if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                        for (String activeProcess : processInfo.pkgList) {
                            if (activeProcess.equals(context.getPackageName())) {
                                isInBackground = false;
                            }
                        }
                    }
                }
            } else {
                List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
                ComponentName componentInfo = taskInfo.get(0).topActivity;
                if (componentInfo.getPackageName().equals(context.getPackageName())) {
                    isInBackground = false;
                }
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
        return isInBackground;
    }


    public static void setDefaultCookieManager() {
        CookieHandler currentHandler = CookieHandler.getDefault();
        if (currentHandler != defaultCookieManager) {
            CookieHandler.setDefault(defaultCookieManager);
        }
    }

    public void storeValue(String key, int value, Context context) {
        SharedPreferences.Editor editor = context.getSharedPreferences(MY_PREF, context.MODE_PRIVATE).edit();
        editor.putInt(key, value);
        editor.commit();
    }

    public int getValue(String key, Context context) {
        SharedPreferences prefs = context.getSharedPreferences(MY_PREF, context.MODE_PRIVATE);
        int value = prefs.getInt(key, 0);
        return value;
    }


}
