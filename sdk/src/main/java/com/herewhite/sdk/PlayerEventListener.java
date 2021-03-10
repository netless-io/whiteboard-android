package com.herewhite.sdk;

import com.herewhite.sdk.domain.PlayerPhase;
import com.herewhite.sdk.domain.PlayerState;
import com.herewhite.sdk.domain.SDKError;

/**
 * 回放房间事件回调接口
 */
public interface PlayerEventListener {
    /**
     * 播放状态切换回调
     */
    void onPhaseChanged(PlayerPhase phase);

    /**
     * 首帧加载回调
     */
    void onLoadFirstFrame();

    /**
     * 分片切换回调，需要了解分片机制。目前无实际用途
     */
    void onSliceChanged(String slice);

    /**
     * 播放中，状态出现变化的回调，只会包含实际发生改变的属性
     */
    void onPlayerStateChanged(PlayerState modifyState);

    /**
     * 出错暂停
     */
    void onStoppedWithError(SDKError error);

    /**
     * 进度时间变化
     */
    void onScheduleTimeChanged(long time);

    /**
     * 添加帧出错
     */
    void onCatchErrorWhenAppendFrame(SDKError error);
    /**
     * 渲染时，出错
     */
    void onCatchErrorWhenRender(SDKError error);
}
