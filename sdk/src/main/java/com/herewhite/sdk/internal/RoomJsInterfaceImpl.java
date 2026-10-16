package com.herewhite.sdk.internal;
import com.herewhite.sdk.LifecycleHost;

import android.webkit.JavascriptInterface;

import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.herewhite.sdk.domain.EventEntry;
import com.herewhite.sdk.domain.FrameError;
import com.herewhite.sdk.domain.RoomPhase;


// Created by buhe on 2018/8/12.

public class RoomJsInterfaceImpl {
    private LifecycleHost lifecycle;
    private final static Gson gson = new Gson();
    @Nullable
    private volatile RoomDelegate room;

    public RoomJsInterfaceImpl() {}
    public RoomJsInterfaceImpl(LifecycleHost lifecycle) { this.lifecycle = lifecycle; }

    public void setRoom(RoomDelegate room) {
        this.room = room;
    }

    @JavascriptInterface
    public void fireCanUndoStepsUpdate(Object input) {
        final RoomDelegate destination = room;
        Object scopedArgs = input;
        if (lifecycle != null) { scopedArgs = lifecycle.unwrap(input, true); if (scopedArgs == LifecycleHost.REJECTED) { return; } }
        final Object args = scopedArgs;
        if (destination != null) {
            destination.fireCanUndoStepsUpdate(Long.valueOf(String.valueOf(args)));
        }
    }

    @JavascriptInterface
    public void fireCanRedoStepsUpdate(Object input) {
        final RoomDelegate destination = room;
        Object scopedArgs = input;
        if (lifecycle != null) { scopedArgs = lifecycle.unwrap(input, true); if (scopedArgs == LifecycleHost.REJECTED) { return; } }
        final Object args = scopedArgs;
        if (destination != null) {
            destination.onCanRedoStepsUpdate(Long.valueOf(String.valueOf(args)));
        }
    }

    @JavascriptInterface
    public void fireMagixEvent(Object input) {
        final RoomDelegate destination = room;
        Object scopedArgs = input;
        if (lifecycle != null) { scopedArgs = lifecycle.unwrap(input, true); if (scopedArgs == LifecycleHost.REJECTED) { return; } }
        final Object args = scopedArgs;
        if (destination != null) {
            new JsCallWrapper(() -> {
                EventEntry eventEntry = gson.fromJson(String.valueOf(args), EventEntry.class);
                destination.fireMagixEvent(eventEntry);
            }, "An exception occurred while sending the event"
            ).run();
        }
    }

    @JavascriptInterface
    public void fireHighFrequencyEvent(Object input) {
        final RoomDelegate destination = room;
        Object scopedArgs = input;
        if (lifecycle != null) { scopedArgs = lifecycle.unwrap(input, true); if (scopedArgs == LifecycleHost.REJECTED) { return; } }
        final Object args = scopedArgs;
        if (destination != null) {
            new JsCallWrapper(() -> {
                EventEntry[] events = gson.fromJson(String.valueOf(args), EventEntry[].class);
                destination.fireHighFrequencyEvent(events);
            }, "An exception occurred while sending the event"
            ).run();
        }
    }

    @JavascriptInterface
    public void firePhaseChanged(Object input) {
        final RoomDelegate destination = room;
        Object scopedArgs = input;
        if (lifecycle != null) { scopedArgs = lifecycle.unwrap(input, true); if (scopedArgs == LifecycleHost.REJECTED) { return; } }
        final Object args = scopedArgs;
        if (destination != null) {
            new JsCallWrapper(() ->
                    destination.firePhaseChanged(RoomPhase.valueOf(String.valueOf(args))),
                    "An exception occurred while invoke onPhaseChanged method"
            ).run();
        }
    }

    @JavascriptInterface
    public void fireKickedWithReason(Object input) {
        final RoomDelegate destination = room;
        Object scopedArgs = input;
        if (lifecycle != null) { scopedArgs = lifecycle.unwrap(input, true); if (scopedArgs == LifecycleHost.REJECTED) { return; } }
        final Object args = scopedArgs;
        if (destination != null) {
            new JsCallWrapper(() ->
                    destination.fireKickedWithReason(String.valueOf(args)),
                    "An exception occurred while invoke onKickedWithReason method"
            ).run();
        }
    }

    @JavascriptInterface
    public void fireDisconnectWithError(Object input) {
        final RoomDelegate destination = room;
        Object scopedArgs = input;
        if (lifecycle != null) { scopedArgs = lifecycle.unwrap(input, true); if (scopedArgs == LifecycleHost.REJECTED) { return; } }
        final Object args = scopedArgs;
        if (destination != null) {
            new JsCallWrapper(() ->
                    destination.fireDisconnectWithError(new Exception(String.valueOf(args))),
                    "An exception occurred while invoke onDisconnectWithError method"
            ).run();
        }
    }

    @JavascriptInterface
    public void fireRoomStateChanged(Object input) {
        final RoomDelegate destination = room;
        Object scopedArgs = input;
        if (lifecycle != null) { scopedArgs = lifecycle.unwrap(input, true); if (scopedArgs == LifecycleHost.REJECTED) { return; } }
        final Object args = scopedArgs;
        if (destination != null) {
            destination.fireRoomStateChanged(String.valueOf(args));
        }
    }

    @JavascriptInterface
    public void fireCatchErrorWhenAppendFrame(Object input) {
        final RoomDelegate destination = room;
        Object scopedArgs = input;
        if (lifecycle != null) { scopedArgs = lifecycle.unwrap(input, true); if (scopedArgs == LifecycleHost.REJECTED) { return; } }
        final Object args = scopedArgs;
        if (destination != null) {
            new JsCallWrapper(() -> {
                FrameError frameError = gson.fromJson(String.valueOf(args), FrameError.class);
                destination.fireCatchErrorWhenAppendFrame(frameError.getUserId(), new Exception(frameError.getError()));
            }, "An exception occurred while invoke onCatchErrorWhenAppendFrame method"
            ).run();
        }
    }
}
