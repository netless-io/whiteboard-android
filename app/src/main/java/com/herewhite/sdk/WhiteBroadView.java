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
        this.loadUrl("http:/30.55.209.196:3100");
    }
}
