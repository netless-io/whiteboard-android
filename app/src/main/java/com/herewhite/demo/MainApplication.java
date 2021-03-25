package com.herewhite.demo;

import android.app.Application;
import android.content.Context;

public class MainApplication extends Application {
    // Just for test
    static Context sContext;

    @Override
    public void onCreate() {
        super.onCreate();

        this.sContext = getApplicationContext();
    }
}
