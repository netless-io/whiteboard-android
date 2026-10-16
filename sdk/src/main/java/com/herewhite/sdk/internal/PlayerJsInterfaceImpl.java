package com.herewhite.sdk.internal;
import com.herewhite.sdk.LifecycleHost;

import android.webkit.JavascriptInterface;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.herewhite.sdk.domain.EventEntry;
import com.herewhite.sdk.domain.PlayerPhase;
import com.herewhite.sdk.domain.SDKError;


// Created by buhe on 2018/8/12.

public class PlayerJsInterfaceImpl {
    private LifecycleHost lifecycle;
    public PlayerJsInterfaceImpl() {}
    public PlayerJsInterfaceImpl(LifecycleHost lifecycle) { this.lifecycle = lifecycle; }
    private final static Gson gson = new Gson();

    private volatile PlayerDelegate player;

    public void setPlayer(PlayerDelegate player) {
        this.player = player;
    }

    @JavascriptInterface
    public void fireMagixEvent(Object input) {
        final PlayerDelegate destination = player;
        Object scopedArgs = input;
        if (lifecycle != null) { scopedArgs = lifecycle.unwrap(input, true); if (scopedArgs == LifecycleHost.REJECTED) { return; } }
        final Object args = scopedArgs;
        if (destination != null) {
            new JsCallWrapper(() ->
                    destination.fireMagixEvent(gson.fromJson(String.valueOf(args), EventEntry.class)),
                    "An exception occurred while sending the event"
            ).run();
        }
    }

    @JavascriptInterface
    public void fireHighFrequencyEvent(Object input) {
        final PlayerDelegate destination = player;
        Object scopedArgs = input;
        if (lifecycle != null) { scopedArgs = lifecycle.unwrap(input, true); if (scopedArgs == LifecycleHost.REJECTED) { return; } }
        final Object args = scopedArgs;
        if (destination != null) {
            new JsCallWrapper(() -> {
                EventEntry[] events = gson.fromJson(String.valueOf(args), EventEntry[].class);
                destination.fireHighFrequencyEvent(events);
            }, "An exception occurred while sending the event").run();
        }
    }

    @JavascriptInterface
    public void onPhaseChanged(Object input) {
        final PlayerDelegate destination = player;
        Object scopedArgs = input;
        if (lifecycle != null) { scopedArgs = lifecycle.unwrap(input, true); if (scopedArgs == LifecycleHost.REJECTED) { return; } }
        final Object args = scopedArgs;
        if (destination != null) {
            new JsCallWrapper(() -> {
                PlayerPhase phase = gson.fromJson(String.valueOf(args), PlayerPhase.class);
                destination.setPlayerPhase(phase);
            }, "An exception occurred while invoke onPhaseChanged method").run();
        }
    }

    @JavascriptInterface
    public void onLoadFirstFrame(Object input) {
        final PlayerDelegate destination = player;
        Object scopedArgs = input;
        if (lifecycle != null) { scopedArgs = lifecycle.unwrap(input, true); if (scopedArgs == LifecycleHost.REJECTED) { return; } }
        final Object args = scopedArgs;
        if (destination != null) {
            destination.onLoadFirstFrame();
        }
    }

    @JavascriptInterface
    public void onSliceChanged(Object input) {
        final PlayerDelegate destination = player;
        Object scopedArgs = input;
        if (lifecycle != null) { scopedArgs = lifecycle.unwrap(input, true); if (scopedArgs == LifecycleHost.REJECTED) { return; } }
        final Object args = scopedArgs;
        if (destination != null) {
            new JsCallWrapper(() ->
                    destination.onSliceChanged(String.valueOf(args)),
                    "An exception occurred while invoke onSliceChanged method"
            ).run();
        }
    }

    @JavascriptInterface
    public void onPlayerStateChanged(Object input) {
        final PlayerDelegate destination = player;
        Object scopedArgs = input;
        if (lifecycle != null) { scopedArgs = lifecycle.unwrap(input, true); if (scopedArgs == LifecycleHost.REJECTED) { return; } }
        final Object args = scopedArgs;
        if (destination != null) {
            destination.syncDisplayerState(String.valueOf(args));
        }
    }

    @JavascriptInterface
    public void onStoppedWithError(Object input) {
        final PlayerDelegate destination = player;
        Object scopedArgs = input;
        if (lifecycle != null) { scopedArgs = lifecycle.unwrap(input, true); if (scopedArgs == LifecycleHost.REJECTED) { return; } }
        final Object args = scopedArgs;
        if (destination != null) {
            new JsCallWrapper(() ->
                    destination.onStoppedWithError(resolverSDKError(args)),
                    "An exception occurred while invoke onStoppedWithError method"
            ).run();
        }
    }

    private SDKError resolverSDKError(Object args) {
        JsonObject jsonObject = gson.fromJson(String.valueOf(args), JsonObject.class);
        String message = "";
        if (jsonObject.get("message") != null) {
            message = jsonObject.get("message").getAsString();
        }
        String jsStack = "";
        if (jsonObject.get("jsStack") != null) {
            jsStack = jsonObject.get("jsStack").getAsString();
        }
        SDKError sdkError = new SDKError(message, jsStack);
        return sdkError;
    }

    @JavascriptInterface
    public void onScheduleTimeChanged(Object input) {
        final PlayerDelegate destination = player;
        Object scopedArgs = input;
        if (lifecycle != null) { scopedArgs = lifecycle.unwrap(input, true); if (scopedArgs == LifecycleHost.REJECTED) { return; } }
        final Object args = scopedArgs;
        if (destination != null) {
            new JsCallWrapper(() -> {
                long scheduleTime = 0;
                String valueString = String.valueOf(args);
                // FIXME: 之前用 Long，但是实际情况是会带小数点的情况存在（修改回调速率时）
                if (valueString.contains(".")) {
                    scheduleTime = Math.round(Double.parseDouble(String.valueOf(args)));
                } else {
                    scheduleTime = Long.parseLong(String.valueOf(args));
                }
                destination.setScheduleTime(scheduleTime);
            }, "An exception occurred while invoke onScheduleTimeChanged method"
            ).run();
        }
    }

    @JavascriptInterface
    public void onCatchErrorWhenAppendFrame(Object input) {
        final PlayerDelegate destination = player;
        Object scopedArgs = input;
        if (lifecycle != null) { scopedArgs = lifecycle.unwrap(input, true); if (scopedArgs == LifecycleHost.REJECTED) { return; } }
        final Object args = scopedArgs;
        if (destination != null) {
            new JsCallWrapper(() ->
                    destination.onCatchErrorWhenAppendFrame(resolverSDKError(args)),
                    "An exception occurred while invoke onCatchErrorWhenAppendFrame method"
            ).run();
        }
    }

    @JavascriptInterface
    public void onCatchErrorWhenRender(Object input) {
        final PlayerDelegate destination = player;
        Object scopedArgs = input;
        if (lifecycle != null) { scopedArgs = lifecycle.unwrap(input, true); if (scopedArgs == LifecycleHost.REJECTED) { return; } }
        final Object args = scopedArgs;
        if (destination != null) {
            new JsCallWrapper(() ->
                    destination.onCatchErrorWhenRender(resolverSDKError(args)),
                    "An exception occurred while invoke onCatchErrorWhenRender method"
            ).run();
        }
    }
}
