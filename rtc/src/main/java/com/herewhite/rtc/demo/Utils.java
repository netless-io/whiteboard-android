package com.herewhite.rtc.demo;

import android.content.Context;
import android.provider.Settings;

public class Utils {
    public static String getUserId(Context context) {
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    public static int getFallbackRtcId(Context context) {
        return getUserId(context).hashCode();
    }
}
