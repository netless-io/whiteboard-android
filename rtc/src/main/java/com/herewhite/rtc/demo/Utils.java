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
    public static final String BOARD_ROOM_UUID = "cd1221809b8111f1aaeead87383431cf";
    public static final String BOARD_ROOM_TOKEN = "NETLESSROOM_YWs9VWtNUk92M1JIN2I2Z284dCZleHBpcmVBdD0xNzg3Mzc4Mzk0OTY3Jm5vbmNlPTg4MzQzNjcwLTlkMjUtMTFmMS1iYzM4LWQ3Yjg5YzgwZTNlMSZyb2xlPTEmc2lnPWZkYjI5MjRkNmMwYTkxMmMzZWQ5YmU5OGZmMTZlNjA3ZDQyYjgxMzljNzA4NTVjMzVkOGM5NzlmOGJjMmY0NTUmdXVpZD1jZDEyMjE4MDliODExMWYxYWFlZWFkODczODM0MzFjZg";
}
