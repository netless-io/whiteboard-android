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

    public static final String BOARD_APP_ID = "122123/123132";
    public static final String BOARD_ROOM_UUID = "";
    public static final String BOARD_ROOM_TOKEN = "";
}
