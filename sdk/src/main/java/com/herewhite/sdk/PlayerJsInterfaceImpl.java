package com.herewhite.sdk;

import android.webkit.JavascriptInterface;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.herewhite.sdk.domain.EventEntry;
import com.herewhite.sdk.domain.PlayerPhase;
import com.herewhite.sdk.domain.SDKError;

/**
 * Created by buhe on 2018/8/12.
 */

class PlayerJsInterfaceImpl {
    private final static Gson gson = new Gson();

    private Player player;

    public void setPlayer(Player player) {
        this.player = player;
    }

    @JavascriptInterface
    public void fireMagixEvent(Object args) {
        if (player != null) {
            EventEntry eventEntry = gson.fromJson(String.valueOf(args), EventEntry.class);
            player.fireMagixEvent(eventEntry);
        }
    }

    @JavascriptInterface
    public void fireHighFrequencyEvent(Object args) {
        EventEntry[] events = gson.fromJson(String.valueOf(args), EventEntry[].class);
        if (player != null) {
            player.fireHighFrequencyEvent(events);
        }
    }

    @JavascriptInterface
    public void onPhaseChanged(Object args) {
        PlayerPhase phase = gson.fromJson(String.valueOf(args), PlayerPhase.class);
        if (player != null) {
            player.setPlayerPhase(phase);
        }
    }

    @JavascriptInterface
    public void onLoadFirstFrame(Object args) {
        // 获取事件,反序列化然后发送通知给监听者
        if (player != null) {
            player.onLoadFirstFrame();
        }
    }

    @JavascriptInterface
    public void onSliceChanged(Object args) {
        // 获取事件,反序列化然后发送通知给监听者
        if (player != null) {
            player.onSliceChanged(String.valueOf(args));
        }
    }

    @JavascriptInterface
    public void onPlayerStateChanged(Object args) {
        // 获取事件,反序列化然后发送通知给监听者
        if (player != null) {
            player.syncDisplayerState(String.valueOf(args));
        }
    }

    @JavascriptInterface
    public void onStoppedWithError(Object args) {
        // 获取事件,反序列化然后发送通知给监听者
        if (player != null) {
            player.onStoppedWithError(resolverSDKError(args));
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
    public void onScheduleTimeChanged(Object args) {
        long scheduleTime = 0;
        String valueString = String.valueOf(args);
        // FIXME: 之前用 Long，但是实际情况是会带小数点的情况存在（修改回调速率时）
        if (valueString.contains(".")) {
            scheduleTime = Math.round(Double.parseDouble(String.valueOf(args)));
        } else {
            scheduleTime = Long.parseLong(String.valueOf(args));
        }

        if (player != null) {
            player.setScheduleTime(scheduleTime);
        }
    }

    @JavascriptInterface
    public void onCatchErrorWhenAppendFrame(Object args) {
        // 获取事件,反序列化然后发送通知给监听者
        if (player != null) {
            new Wrapper(() ->
                    player.onCatchErrorWhenAppendFrame(resolverSDKError(args)),
                    "An exception occurred while invoke onCatchErrorWhenAppendFrame method"
            ).run();
        }
    }

    @JavascriptInterface
    public void onCatchErrorWhenRender(final Object args) {
        // 获取事件,反序列化然后发送通知给监听者
        if (player != null) {
            new Wrapper(() ->
                    player.onCatchErrorWhenRender(resolverSDKError(args)),
                    "An exception occurred while invoke onCatchErrorWhenRender method"
            ).run();
        }
    }

    // TODO 是否处理批量的异常模版
    static class Wrapper {
        private final Runnable runnable;
        private final String message;

        // TODO Change Runnable
        public Wrapper(Runnable runnable, String message) {
            this.runnable = runnable;
            this.message = message;
        }

        public void run() {
            try {
                runnable.run();
            } catch (AssertionError a) {
                throw a;
            } catch (Throwable e) {
                Logger.error(message, e);
            }
        }
    }
}
