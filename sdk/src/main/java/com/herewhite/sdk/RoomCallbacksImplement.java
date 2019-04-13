package com.herewhite.sdk;

import android.webkit.JavascriptInterface;

import com.google.gson.Gson;
import com.herewhite.sdk.domain.EventEntry;
import com.herewhite.sdk.domain.FrameError;
import com.herewhite.sdk.domain.RoomPhase;
import com.herewhite.sdk.domain.RoomState;
import com.herewhite.sdk.domain.UpdateCursor;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by buhe on 2018/8/12.
 */

public class RoomCallbacksImplement {
    private final static Gson gson = new Gson();
    private RoomCallbacks listener;
    private Room room;

    public RoomCallbacksImplement() {

    }

    public RoomCallbacks getListener() {
        return listener;
    }

    public void setListener(RoomCallbacks listener) {
        this.listener = listener;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    @JavascriptInterface
    public void fireMagixEvent(Object args) {
        EventEntry eventEntry = gson.fromJson(String.valueOf(args), EventEntry.class);
        if (room != null) {
            room.fireMagixEvent(eventEntry);
        }
    }


    @JavascriptInterface
    public void firePhaseChanged(Object args) {
//         获取事件,反序列化然后发送通知给监听者
        if (listener != null) {
            try {
                listener.onPhaseChanged(RoomPhase.valueOf(String.valueOf(args)));
            } catch (Throwable e) {
                Logger.error("An exception occurred while invoke onPhaseChanged method", e);
            }
        }
    }

    @JavascriptInterface
    public void fireKickedWithReason(Object args) {
        // 获取事件,反序列化然后发送通知给监听者
        if (listener != null) {
            try {
                listener.onKickedWithReason(String.valueOf(args));
            } catch (Throwable e) {
                Logger.error("An exception occurred while invoke onKickedWithReason method", e);
            }

        }
    }

    @JavascriptInterface
    public void fireDisconnectWithError(Object args) {
        // 获取事件,反序列化然后发送通知给监听者
        if (listener != null) {
            try {
                listener.onDisconnectWithError(new Exception(String.valueOf(args)));
            } catch (Throwable e) {
                Logger.error("An exception occurred while invoke onDisconnectWithError method", e);
            }
        }
    }

    @JavascriptInterface
    public void fireRoomStateChanged(Object args) {
        // 获取事件,反序列化然后发送通知给监听者
        RoomState roomState = gson.fromJson(String.valueOf(args), RoomState.class);
        if (listener != null) {
            try {
                listener.onRoomStateChanged(roomState);
            } catch (Throwable e) {
                Logger.error("An exception occurred while invoke onRoomStateChanged method", e);
            }

        }
    }

    @JavascriptInterface
    public void fireBeingAbleToCommitChange(Object args) {
        // 获取事件,反序列化然后发送通知给监听者
        if (listener != null) {
            try {
                listener.onBeingAbleToCommitChange(Boolean.valueOf(String.valueOf(args)));
            } catch (Throwable e) {
                Logger.error("An exception occurred while invoke onBeingAbleToCommitChange method", e);
            }

        }
    }

    @JavascriptInterface
    public void fireCatchErrorWhenAppendFrame(Object args) {
        // 获取事件,反序列化然后发送通知给监听者
        FrameError frameError = gson.fromJson(String.valueOf(args), FrameError.class);
        if (listener != null) {
            try {
                listener.onCatchErrorWhenAppendFrame(frameError.getUserId(), new Exception(frameError.getError()));
            } catch (Throwable e) {
                Logger.error("An exception occurred while invoke onCatchErrorWhenAppendFrame method", e);
            }
        }
    }

    @JavascriptInterface
    public void onCursorViewsUpdate(Object args) {
        if (listener != null) {
            try {
                UpdateCursor updateCursor = gson.fromJson(String.valueOf(args), UpdateCursor.class);
                listener.onCursorViewsUpdate(updateCursor);
            } catch (Throwable e) {
                Logger.error("An exception occurred while invoke onCursorViewsUpdate method", e);
            }
        }
    }
}
