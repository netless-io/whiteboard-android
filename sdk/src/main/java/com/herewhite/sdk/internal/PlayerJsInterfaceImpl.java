package com.herewhite.sdk.internal;

import android.webkit.JavascriptInterface;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.herewhite.sdk.domain.EventEntry;
import com.herewhite.sdk.domain.PlayerPhase;
import com.herewhite.sdk.domain.SDKError;

/**
 * Created by buhe on 2018/8/12.
 */

public class PlayerJsInterfaceImpl {
    private final static Gson gson = new Gson();

    private PlayerDelegate player;

    public void setPlayer(PlayerDelegate player) {
        this.player = player;
    }

    @JavascriptInterface
    public void fireMagixEvent(Object args) {
        if (player != null) {
            new JsCallWrapper(() ->
                    player.fireMagixEvent(gson.fromJson(String.valueOf(args), EventEntry.class)),
                    "An exception occurred while sending the event"
            ).run();
        }
    }

    @JavascriptInterface
    public void fireHighFrequencyEvent(Object args) {
        if (player != null) {
            new JsCallWrapper(() -> {
                EventEntry[] events = gson.fromJson(String.valueOf(args), EventEntry[].class);
                player.fireHighFrequencyEvent(events);
            }, "An exception occurred while sending the event").run();
        }
    }

    @JavascriptInterface
    public void onPhaseChanged(Object args) {
        if (player != null) {
            new JsCallWrapper(() -> {
                PlayerPhase phase = gson.fromJson(String.valueOf(args), PlayerPhase.class);
                player.setPlayerPhase(phase);
            }, "An exception occurred while invoke onPhaseChanged method").run();
        }
    }

    @JavascriptInterface
    public void onLoadFirstFrame(Object args) {
        if (player != null) {
            player.onLoadFirstFrame();
        }
    }

    @JavascriptInterface
    public void onSliceChanged(Object args) {
        if (player != null) {
            new JsCallWrapper(() ->
                    player.onSliceChanged(String.valueOf(args)),
                    "An exception occurred while invoke onSliceChanged method"
            ).run();
        }
    }

    @JavascriptInterface
    public void onPlayerStateChanged(Object args) {
        if (player != null) {
            player.syncDisplayerState(String.valueOf(args));
        }
    }

    @JavascriptInterface
    public void onStoppedWithError(Object args) {
        if (player != null) {
            new JsCallWrapper(() ->
                    player.onStoppedWithError(resolverSDKError(args)),
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
    public void onScheduleTimeChanged(Object args) {
        if (player != null) {
            new JsCallWrapper(() -> {
                long scheduleTime = 0;
                String valueString = String.valueOf(args);
                // FIXME: 之前用 Long，但是实际情况是会带小数点的情况存在（修改回调速率时）
                if (valueString.contains(".")) {
                    scheduleTime = Math.round(Double.parseDouble(String.valueOf(args)));
                } else {
                    scheduleTime = Long.parseLong(String.valueOf(args));
                }
                player.setScheduleTime(scheduleTime);
            }, "An exception occurred while invoke onScheduleTimeChanged method"
            ).run();
        }
    }

    @JavascriptInterface
    public void onCatchErrorWhenAppendFrame(Object args) {
        if (player != null) {
            new JsCallWrapper(() ->
                    player.onCatchErrorWhenAppendFrame(resolverSDKError(args)),
                    "An exception occurred while invoke onCatchErrorWhenAppendFrame method"
            ).run();
        }
    }

    @JavascriptInterface
    public void onCatchErrorWhenRender(final Object args) {
        if (player != null) {
            new JsCallWrapper(() ->
                    player.onCatchErrorWhenRender(resolverSDKError(args)),
                    "An exception occurred while invoke onCatchErrorWhenRender method"
            ).run();
        }
    }
}
