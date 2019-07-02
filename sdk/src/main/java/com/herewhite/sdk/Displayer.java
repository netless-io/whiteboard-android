package com.herewhite.sdk;

import android.content.Context;

import com.google.gson.Gson;

public class Displayer {

    protected final WhiteBroadView bridge;
    protected String uuid;
    protected final Context context;
    protected WhiteSdk sdk;
    protected final static Gson gson = new Gson();

    public Displayer(String uuid, WhiteBroadView bridge, Context context, WhiteSdk sdk) {
        this.uuid = uuid;
        this.bridge = bridge;
        this.context = context;
        this.sdk = sdk;
    }

    public void moveCamera(Object camera) {
        this.bridge.callHandler("displayer.moveCamera", new Object[]{camera});
    }

    public void moveCameraToContainer(Object rectange) {
        this.bridge.callHandler("displayer.moveCameraToContain", new Object[]{rectange});
    }
}
