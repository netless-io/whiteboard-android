package com.herewhite.sdk;

import android.content.Context;
import android.util.AttributeSet;

import com.google.gson.Gson;

import wendu.dsbridge.DWebView;
import wendu.dsbridge.OnReturnValue;

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
        this.getSettings().setMediaPlaybackRequiresUserGesture(false);
//        this.loadUrl("http://192.168.199.111:3100");
        this.loadUrl("file:///android_asset/cloud/index.html");
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


    private final static Gson gson = new Gson();


    public <T> void callHandler(String method, Object[] args, OnReturnValue<T> handler) {
        super.callHandler(method, toMaps(args), handler);
    }

    public void callHandler(String method, Object[] args) {
        this.callHandler(method, args, null);
    }

    public <T> void callHandler(String method, OnReturnValue<T> handler) {
        this.callHandler(method, null, handler);
    }

    private Object[] toMaps(Object[] objects) {
        if (objects != null) {
            Object[] maps = new Object[objects.length];
            for (int i = 0; i < objects.length; i++) {
                maps[i] = toJSON(objects[i]);
            }
            return maps;
        } else {
            return new Object[0];
        }
    }

    private Object toJSON(Object object) {
        if (object instanceof Double
                || object instanceof Integer
                || object instanceof Long
                || object instanceof Float
                || object instanceof Short
                || object instanceof Boolean
                || object instanceof String) {
            return object;
        } else {
            return gson.toJson(object);
        }
    }
}
