package com.herewhite.demo;

import android.app.Application;

import com.herewhite.demo.common.DemoAPI;
import com.herewhite.sdk.WhiteboardView;

public class MainApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        DemoAPI.get().init(getApplicationContext());
        WhiteboardView.setWebContentsDebuggingEnabled(true);
    }
}
