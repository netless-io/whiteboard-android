package com.herewhite.sdk;

import android.content.Context;
import android.util.AttributeSet;

import wendu.dsbridge.DWebView;

/**
 * white on 2018/8/10.
 */

public class WhiteBroadView extends DWebView {


    public WhiteBroadView(Context context) {
        super(context);
        init();
    }

    public WhiteBroadView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init(){
        //DEBUG
        DWebView.setWebContentsDebuggingEnabled(true);
//        this.loadUrl("http://10.63.74.141:3100");
        this.loadUrl("file:///android_asset/index.html");
    }
}
