package com.herewhite.sdk;

import android.webkit.JavascriptInterface;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.herewhite.sdk.domain.FrameError;
import com.herewhite.sdk.domain.PlayerPhase;
import com.herewhite.sdk.domain.PlayerState;
import com.herewhite.sdk.domain.SDKError;
import com.herewhite.sdk.domain.UpdateCursor;

/**
 * Created by buhe on 2018/8/12.
 */

public class PlayerCallbacksImplement {
    private final static Gson gson = new Gson();
    private PlayerEventListener listener;

    public PlayerEventListener getListener() {
        return listener;
    }

    public void setListener(PlayerEventListener listener) {
        this.listener = listener;
    }

    @JavascriptInterface
    public void onPhaseChanged(Object args) {
//         获取事件,反序列化然后发送通知给监听者
        if (listener != null) {
            try {
                listener.onPhaseChanged(PlayerPhase.valueOf(String.valueOf(args)));
            } catch (Throwable e) {
                Logger.error("An exception occurred while invoke onPhaseChanged method", e);
            }
        }
    }

    @JavascriptInterface
    public void onLoadFirstFrame(Object args) {
        // 获取事件,反序列化然后发送通知给监听者
        if (listener != null) {
            try {
                listener.onLoadFirstFrame();
            } catch (Throwable e) {
                Logger.error("An exception occurred while invoke onLoadFirstFrame method", e);
            }

        }
    }

    @JavascriptInterface
    public void onSliceChanged(Object args) {
        // 获取事件,反序列化然后发送通知给监听者
        if (listener != null) {
            try {
                listener.onSliceChanged(String.valueOf(args));
            } catch (Throwable e) {
                Logger.error("An exception occurred while invoke onSliceChanged method", e);
            }
        }
    }

    @JavascriptInterface
    public void onPlayerStateChanged(Object args) {
        // 获取事件,反序列化然后发送通知给监听者
        if (listener != null) {
            try {
                PlayerState playerState = gson.fromJson(String.valueOf(args), PlayerState.class);
                listener.onPlayerStateChanged(playerState);
            } catch (Throwable e) {
                Logger.error("An exception occurred while invoke onPlayerStateChanged method", e);
            }

        }
    }

    @JavascriptInterface
    public void onStoppedWithError(Object args) {
        // 获取事件,反序列化然后发送通知给监听者
        if (listener != null) {
            try {
                listener.onStoppedWithError(resolverSDKError(args));
            } catch (Throwable e) {
                Logger.error("An exception occurred while invoke onStoppedWithError method", e);
            }

        }
    }

    private SDKError resolverSDKError(Object args) {
        JsonObject jsonObject = gson.fromJson(String.valueOf(args), JsonObject.class);
        String message = jsonObject.get("message").getAsString();
        String jsStack = jsonObject.get("jsStack").getAsString();
        SDKError sdkError = new SDKError(message, jsStack);
        return sdkError;
    }

    @JavascriptInterface
    public void onScheduleTimeChanged(Object args) {
        // 获取事件,反序列化然后发送通知给监听者
        if (listener != null) {
            try {
                listener.onScheduleTimeChanged(Long.parseLong(String.valueOf(args)));
            } catch (Throwable e) {
                Logger.error("An exception occurred while invoke onScheduleTimeChanged method", e);
            }
        }
    }

    @JavascriptInterface
    public void onCatchErrorWhenAppendFrame(Object args) {
        // 获取事件,反序列化然后发送通知给监听者
        if (listener != null) {
            try {
                listener.onCatchErrorWhenAppendFrame(resolverSDKError(args));
            } catch (Throwable e) {
                Logger.error("An exception occurred while invoke onCatchErrorWhenAppendFrame method", e);
            }
        }
    }

    @JavascriptInterface
    public void onCatchErrorWhenRender(Object args) {
        // 获取事件,反序列化然后发送通知给监听者
        if (listener != null) {
            try {
                FrameError frameError = gson.fromJson(String.valueOf(args), FrameError.class);
                listener.onCatchErrorWhenRender(resolverSDKError(args));
            } catch (Throwable e) {
                Logger.error("An exception occurred while invoke onCatchErrorWhenRender method", e);
            }
        }
    }

    @JavascriptInterface
    public void onCursorViewsUpdate(Object args) {
        // 获取事件,反序列化然后发送通知给监听者
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
