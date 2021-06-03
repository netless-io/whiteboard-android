package com.herewhite.sdk.internal;

import android.webkit.JavascriptInterface;

import com.google.gson.Gson;
import com.herewhite.sdk.domain.EventEntry;
import com.herewhite.sdk.domain.FrameError;
import com.herewhite.sdk.domain.RoomPhase;

import androidx.annotation.Nullable;


// Created by buhe on 2018/8/12.

public class RoomJsInterfaceImpl {
    private final static Gson gson = new Gson();
    @Nullable
    private RoomDelegate room;

    public RoomJsInterfaceImpl() {
    }

    public void setRoom(RoomDelegate room) {
        this.room = room;
    }

    @JavascriptInterface
    public void fireCanUndoStepsUpdate(Object args) {
        if (room != null) {
            room.fireCanUndoStepsUpdate(Long.valueOf(String.valueOf(args)));
        }
    }

    @JavascriptInterface
    public void fireCanRedoStepsUpdate(Object args) {
        if (room != null) {
            room.onCanRedoStepsUpdate(Long.valueOf(String.valueOf(args)));
        }
    }

    @JavascriptInterface
    public void fireMagixEvent(Object args) {
        if (room != null) {
            new JsCallWrapper(() -> {
                EventEntry eventEntry = gson.fromJson(String.valueOf(args), EventEntry.class);
                room.fireMagixEvent(eventEntry);
            }, "An exception occurred while sending the event"
            ).run();
        }
    }

    @JavascriptInterface
    public void fireHighFrequencyEvent(Object args) {
        if (room != null) {
            new JsCallWrapper(() -> {
                EventEntry[] events = gson.fromJson(String.valueOf(args), EventEntry[].class);
                room.fireHighFrequencyEvent(events);
            }, "An exception occurred while sending the event"
            ).run();
        }
    }

    @JavascriptInterface
    public void firePhaseChanged(Object args) {
        if (room != null) {
            new JsCallWrapper(() ->
                    room.firePhaseChanged(RoomPhase.valueOf(String.valueOf(args))),
                    "An exception occurred while invoke onPhaseChanged method"
            ).run();
        }
    }

    @JavascriptInterface
    public void fireKickedWithReason(Object args) {
        if (room != null) {
            new JsCallWrapper(() ->
                    room.fireKickedWithReason(String.valueOf(args)),
                    "An exception occurred while invoke onKickedWithReason method"
            ).run();
        }
    }

    @JavascriptInterface
    public void fireDisconnectWithError(Object args) {
        if (room != null) {
            new JsCallWrapper(() ->
                    room.fireDisconnectWithError(new Exception(String.valueOf(args))),
                    "An exception occurred while invoke onDisconnectWithError method"
            ).run();
        }
    }

    @JavascriptInterface
    public void fireRoomStateChanged(Object args) {
        if (room != null) {
            room.fireRoomStateChanged(String.valueOf(args));
        }
    }

    @JavascriptInterface
    public void fireCatchErrorWhenAppendFrame(Object args) {
        if (room != null) {
            new JsCallWrapper(() -> {
                FrameError frameError = gson.fromJson(String.valueOf(args), FrameError.class);
                room.fireCatchErrorWhenAppendFrame(frameError.getUserId(), new Exception(frameError.getError()));
            }, "An exception occurred while invoke onCatchErrorWhenAppendFrame method"
            ).run();
        }
    }
}
