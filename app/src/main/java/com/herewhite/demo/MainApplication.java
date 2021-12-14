package com.herewhite.demo;

import android.app.Application;
import android.content.Context;

import com.herewhite.sdk.WhiteboardView;

public class MainApplication extends Application {
    // Just for test
    public static Context sContext;

    @Override
    public void onCreate() {
        super.onCreate();

        this.sContext = getApplicationContext();
        WhiteboardView.setWebContentsDebuggingEnabled(true);
    }
}
