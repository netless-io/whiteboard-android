package com.herewhite.sdk;

import android.webkit.JavascriptInterface;

import com.google.gson.Gson;
import com.herewhite.sdk.domain.EventEntry;
import com.herewhite.sdk.domain.FrameError;
import com.herewhite.sdk.domain.RoomPhase;

import androidx.annotation.Nullable;

/**
 * Created by buhe on 2018/8/12.
 */
public class RoomJsInterfaceImpl {
    private final static Gson gson = new Gson();
    @Nullable
    private Room room;

    RoomJsInterfaceImpl() {
    }

    // TODO 命令是否切合，是否使用attach之类的
    public void setRoom(Room room) {
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
        EventEntry eventEntry = gson.fromJson(String.valueOf(args), EventEntry.class);
        if (room != null) {
            room.fireMagixEvent(eventEntry);
        }
    }

    @JavascriptInterface
    public void fireHighFrequencyEvent(Object args) {
        EventEntry[] events = gson.fromJson(String.valueOf(args), EventEntry[].class);
        if (room != null) {
            room.fireHighFrequencyEvent(events);
        }
    }

    @JavascriptInterface
    public void firePhaseChanged(Object args) {
        RoomPhase phase = RoomPhase.valueOf(String.valueOf(args));
        if (room != null) {
            room.setRoomPhase(phase);
        }
    }

    @JavascriptInterface
    public void fireKickedWithReason(Object args) {
        if (room != null) {
            room.fireKickedWithReason(String.valueOf(args));
        }
    }

    @JavascriptInterface
    public void fireDisconnectWithError(Object args) {
        if (room != null) {
            room.fireDisconnectWithError(new Exception(String.valueOf(args)));
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
        // 获取事件,反序列化然后发送通知给监听者
        FrameError frameError = gson.fromJson(String.valueOf(args), FrameError.class);
        if (room != null) {
            room.fireCatchErrorWhenAppendFrame(frameError.getUserId(), new Exception(frameError.getError()));
        }
    }
}
