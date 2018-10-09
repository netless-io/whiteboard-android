package com.herewhite.sdk;

import android.content.Context;
import android.util.AttributeSet;

import wendu.dsbridge.DWebView;

/**
 * white on 2018/8/10.
 */

public class WhiteBroadView extends DWebView {


    private Environment environment = Environment.cloud;

    public WhiteBroadView(Context context) {
        super(context);
        init();
    }

    public WhiteBroadView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        //DEBUG
        DWebView.setWebContentsDebuggingEnabled(true);
        this.loadUrl("http://192.168.199.111:3100");
//        this.loadUrl("file:///android_asset/cloud/index.html");
    }

    public void switchEnv(Environment environment) {
        this.environment = environment;
        if (environment == Environment.dev) {
            this.loadUrl("http://192.168.31.216:3100");
        } else {
            this.loadUrl("file:///android_asset/" + environment.name() + "/index.html");
        }
    }

    public Environment getEnv() {
        return environment;
    }
}
