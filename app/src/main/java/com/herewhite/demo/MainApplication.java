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
        // WhiteSdk.prepareWhiteConnection(this, new ConnectionPrepareParam("123/12312", Region.cn));
        // WhiteboardView.enableAssetsHttps();
    }
}
