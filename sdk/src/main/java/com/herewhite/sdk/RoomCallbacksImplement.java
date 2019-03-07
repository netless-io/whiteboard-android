package com.herewhite.sdk;

import android.webkit.JavascriptInterface;

import com.google.gson.Gson;
import com.herewhite.sdk.domain.FrameError;
import com.herewhite.sdk.domain.RoomPhase;
import com.herewhite.sdk.domain.RoomState;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by buhe on 2018/8/12.
 */

public class RoomCallbacksImplement {
    private final static Gson gson = new Gson();
    private final List<RoomCallbacks> listeners = new ArrayList<>();

    public void addRoomCallbacks(RoomCallbacks callback) {
        listeners.add(callback);
    }

    @JavascriptInterface
    public void firePhaseChanged(Object args) throws JSONException {
//         获取事件,反序列化然后发送通知给监听者
        for (RoomCallbacks roomCallbacks : listeners) {
            try {
                roomCallbacks.onPhaseChanged(RoomPhase.valueOf(String.valueOf(args)));
            } catch (Throwable e) {
                Logger.error("An exception occurred while invoke onPhaseChanged method", e);
            }
        }
    }

    @JavascriptInterface
    public void fireKickedWithReason(Object args) throws JSONException {
        // 获取事件,反序列化然后发送通知给监听者
        for (RoomCallbacks roomCallbacks : listeners) {
            try {
                roomCallbacks.onKickedWithReason(String.valueOf(args));
            } catch (Throwable e) {
                Logger.error("An exception occurred while invoke onKickedWithReason method", e);
            }

        }
    }

    @JavascriptInterface
    public void fireDisconnectWithError(Object args) throws JSONException {
        // 获取事件,反序列化然后发送通知给监听者
        for (RoomCallbacks roomCallbacks : listeners) {
            try {
                roomCallbacks.onDisconnectWithError(new Exception(String.valueOf(args)));
            } catch (Throwable e) {
                Logger.error("An exception occurred while invoke onDisconnectWithError method", e);
            }
        }
    }

    @JavascriptInterface
    public void fireRoomStateChanged(Object args) throws JSONException {
        // 获取事件,反序列化然后发送通知给监听者
        RoomState roomState = gson.fromJson(String.valueOf(args), RoomState.class);
        for (RoomCallbacks roomCallbacks : listeners) {
            try {
                roomCallbacks.onRoomStateChanged(roomState);
            } catch (Throwable e) {
                Logger.error("An exception occurred while invoke onRoomStateChanged method", e);
            }

        }
    }

    @JavascriptInterface
    public void fireBeingAbleToCommitChange(Object args) throws JSONException {
        // 获取事件,反序列化然后发送通知给监听者
        for (RoomCallbacks roomCallbacks : listeners) {
            try {
                roomCallbacks.onBeingAbleToCommitChange(Boolean.valueOf(String.valueOf(args)));
            } catch (Throwable e) {
                Logger.error("An exception occurred while invoke onBeingAbleToCommitChange method", e);
            }

        }
    }

    @JavascriptInterface
    public void fireCatchErrorWhenAppendFrame(Object args) throws JSONException {
        // 获取事件,反序列化然后发送通知给监听者
        FrameError frameError = gson.fromJson(String.valueOf(args), FrameError.class);
        for (RoomCallbacks roomCallbacks : listeners) {
            try {
                roomCallbacks.onCatchErrorWhenAppendFrame(frameError.getUserId(), new Exception(frameError.getError()));
            } catch (Throwable e) {
                Logger.error("An exception occurred while invoke onCatchErrorWhenAppendFrame method", e);
            }
        }
    }
}
