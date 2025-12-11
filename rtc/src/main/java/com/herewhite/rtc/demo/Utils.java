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
    public static final String BOARD_ROOM_UUID = "dff91470d59d11f0b88591521e4e49c2";
    public static final String BOARD_ROOM_TOKEN = "NETLESSROOM_YWs9VWtNUk92M1JIN2I2Z284dCZleHBpcmVBdD0xNzY1NTA5OTgzODU1Jm5vbmNlPTJiNzE0ZmYwLWQ2NDEtMTFmMC05NmE5LWFiMzg4NjE4OThhZiZyb2xlPTEmc2lnPTNjZmMyMzZjYTQxMmYyMjJlYjc4MzAyMDE2NGQ5NmNlZWI2YTk0NzYxZGFkMWUxNjE2ZDllYmQ4ZmY1NzM1ZTImdXVpZD1kZmY5MTQ3MGQ1OWQxMWYwYjg4NTkxNTIxZTRlNDljMg";
}
