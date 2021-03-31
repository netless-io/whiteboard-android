package com.herewhite.sdk.domain;

import com.google.gson.annotations.SerializedName;

public enum PlayerPhase {
    /**
     * 正在等待白板回放的第一帧。这是白板回放的初始阶段。
     */
    waitingFirstFrame,
    /**
     * 白板回放正在播放。
     */
    playing,
    /**
     * 白板回放已暂停。
     */
    pause,
    @SerializedName("stop")
    /**
     * 白板回放已停止。
     */
            stopped,
    /**
     * 白板回放已结束。
     */
    ended,
    /**
     * 白板回放正在缓存中。
     */
    buffering,
}
