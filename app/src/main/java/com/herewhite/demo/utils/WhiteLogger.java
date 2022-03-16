package com.herewhite.demo.utils;

import android.util.Log;

public class WhiteLogger {
    private static final String TAG = WhiteLogger.class.getSimpleName();

    public static void i(String message) {
        Log.i(TAG, message);
    }

    public static void e(String msg) {
        Log.e(TAG, msg);
    }

    public static void e(String tag, String msg, Throwable tr) {
        Log.e(TAG, msg, tr);
    }
}
