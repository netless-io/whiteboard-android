package com.herewhite.sdk;

import android.webkit.JavascriptInterface;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.herewhite.sdk.domain.EventEntry;
import com.herewhite.sdk.domain.PlayerPhase;
import com.herewhite.sdk.domain.PlayerState;
import com.herewhite.sdk.domain.SDKError;

/**
 * Created by buhe on 2018/8/12.
 */

class PlayerJsInterfaceImpl implements SyncDisplayerState.Listener<PlayerState> {
    private final static Gson gson = new Gson();

    private PlayerEventListener listener;
    private Player player;

    public PlayerEventListener getListener() {
        return listener;
    }

    public void setPlayer(Player player) {
        this.player = player;
        this.player.getSyncPlayerState().setListener(this);
    }

    public Player getPlayer() {
        return player;
    }

    public void setListener(PlayerEventListener listener) {
        this.listener = listener;
    }

    @Override
    public void onDisplayerStateChanged(PlayerState modifyState) {
        if (listener != null) {
            try {
                listener.onPlayerStateChanged(modifyState);
            } catch (AssertionError a) {
                throw a;
            } catch (Throwable e) {
                Logger.error("An exception occurred while invoke onPlayerStateChanged method", e);
            }
        }
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

        if (this.player != null) {
            this.player.setPlayerPhase(phase);
        }
        if (listener != null) {
            try {
                listener.onPhaseChanged(phase);
            } catch (AssertionError a) {
                throw a;
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
            } catch (AssertionError a) {
                throw a;
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
            } catch (AssertionError a) {
                throw a;
            } catch (Throwable e) {
                Logger.error("An exception occurred while invoke onSliceChanged method", e);
            }
        }
    }

    @JavascriptInterface
    public void onPlayerStateChanged(Object args) {
        // 获取事件,反序列化然后发送通知给监听者
        this.player.getSyncPlayerState().syncDisplayerState(String.valueOf(args));
    }

    @JavascriptInterface
    public void onStoppedWithError(Object args) {
        // 获取事件,反序列化然后发送通知给监听者
        if (listener != null) {
            try {
                listener.onStoppedWithError(resolverSDKError(args));
            } catch (AssertionError a) {
                throw a;
            } catch (Throwable e) {
                Logger.error("An exception occurred while invoke onStoppedWithError method", e);
            }

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
        //FIXME: 之前用 Long，但是实际情况是会带小数点的情况存在（修改回调速率时）
        if (valueString.contains(".")) {
            scheduleTime = Math.round(Double.parseDouble(String.valueOf(args)));
        } else {
            scheduleTime = Long.parseLong(String.valueOf(args));
        }

        if (this.player != null) {
            this.player.setScheduleTime(scheduleTime);
        }
        // 获取事件,反序列化然后发送通知给监听者
        if (listener != null) {
            try {
                listener.onScheduleTimeChanged(scheduleTime);
            } catch (AssertionError a) {
                throw a;
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
            } catch (AssertionError a) {
                throw a;
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
                listener.onCatchErrorWhenRender(resolverSDKError(args));
            } catch (AssertionError a) {
                throw a;
            } catch (Throwable e) {
                Logger.error("An exception occurred while invoke onCatchErrorWhenRender method", e);
            }
        }
    }
}
