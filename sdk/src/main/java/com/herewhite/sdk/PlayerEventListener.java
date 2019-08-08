package com.herewhite.sdk;

import com.herewhite.sdk.domain.PlayerPhase;
import com.herewhite.sdk.domain.PlayerState;
import com.herewhite.sdk.domain.SDKError;

import org.json.JSONObject;

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
     * 播放中，状态出现变化的回调
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

    /** 将部分回调的返回内容，以 Android JSON 格式输出，以支持部分自定义字段的需求 */
    interface JSONListener {
        /**
         * 回放房间状态变化回调（JSONObject 形式）。
         * 房间中 PlayerState 发生变化时，会触发该回调。与 PlayerEventListener 不同，该回调不会
         * 过滤不属于 PlayerState 类中的字段。可以用来获取自定义的 globalState。
         @param jsonObject 发生变化的所有 PlayerState 字段。
         */
        void onPlayerStateChanged(JSONObject jsonObject);
    }
}
