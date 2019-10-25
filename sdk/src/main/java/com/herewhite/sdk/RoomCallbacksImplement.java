package com.herewhite.sdk;

import android.content.Context;
import android.os.Handler;
import android.webkit.JavascriptInterface;

import com.google.gson.Gson;
import com.herewhite.sdk.domain.EventEntry;
import com.herewhite.sdk.domain.FrameError;
import com.herewhite.sdk.domain.RoomPhase;
import com.herewhite.sdk.domain.RoomState;

/**
 * Created by buhe on 2018/8/12.
 */

public class RoomCallbacksImplement implements SyncDisplayerState.Listener<RoomState> {

    private final static Gson gson = new Gson();
    private final Handler handler;
    private RoomCallbacks listener;

    private Room room;

    RoomCallbacksImplement(Context context) {
        this.handler = new Handler(context.getMainLooper());
    }

    public RoomCallbacks getListener() {
        return listener;
    }

    public void setListener(RoomCallbacks listener) {
        this.listener = listener;
    }

    public void setRoom(Room room) {
        this.room = room;
        this.room.getSyncRoomState().setListener(this);
    }

    @Override
    public void onDisplayerStateChanged(final RoomState modifyState) {
        if (listener != null) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (listener != null) {
                        listener.onRoomStateChanged(modifyState);
                    }
                }
            });
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

        if (this.room != null) {
            this.room.setRoomPhase(phase);
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
    public void fireKickedWithReason(Object args) {
        // 获取事件,反序列化然后发送通知给监听者
        if (listener != null) {
            try {
                listener.onKickedWithReason(String.valueOf(args));
            } catch (AssertionError a) {
                throw a;
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
            } catch (AssertionError a) {
                throw a;
            } catch (Throwable e) {
                Logger.error("An exception occurred while invoke onDisconnectWithError method", e);
            }
        }
    }

    @JavascriptInterface
    public void fireRoomStateChanged(Object args) {
        this.room.getSyncRoomState().syncDisplayerState(String.valueOf(args));
    }

    @JavascriptInterface
    public void fireBeingAbleToCommitChange(Object args) {
        // 获取事件,反序列化然后发送通知给监听者
        if (listener != null) {
            try {
                listener.onBeingAbleToCommitChange(Boolean.valueOf(String.valueOf(args)));
            } catch (AssertionError a) {
                throw a;
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
            } catch (AssertionError a) {
                throw a;
            } catch (Throwable e) {
                Logger.error("An exception occurred while invoke onCatchErrorWhenAppendFrame method", e);
            }
        }
    }
}
