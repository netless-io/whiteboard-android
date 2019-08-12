package com.herewhite.sdk;

import com.herewhite.sdk.domain.RoomPhase;
import com.herewhite.sdk.domain.RoomState;

import org.json.JSONObject;

/**
 * Created by buhe on 2018/8/12.
 */

public interface RoomCallbacks {

    /** 房间网络连接状态回调事件 */
    void onPhaseChanged(RoomPhase phase);

    void onBeingAbleToCommitChange(boolean isAbleToCommit);

    /** 白板失去连接回调，附带错误信息 */
    void onDisconnectWithError(Exception e);

    /** 用户被远程服务器踢出房间，附带踢出原因 */
    void onKickedWithReason(String reason);

    /**
     房间中RoomState属性，发生变化时，会触发该回调。
     @param modifyState 发生变化的 RoomState 内容
     */
    void onRoomStateChanged(RoomState modifyState);

    /** 用户错误事件捕获，附带用户 id，以及错误原因 */
    void onCatchErrorWhenAppendFrame(long userId, Exception error);

    /**
     * 将部分回调的返回内容，以 Android JSON 格式输出，以支持部分自定义字段的需求
     * @since 2.4.7
     *  */
    interface JSONCallbacks {
        /**
         * 实时房间状态变化回调（JSONObject 形式）。
         * 房间中 RoomState 发生变化时，会触发该回调。与 RoomCallbacks 不同，该回调不会
         * 过滤不属于 RoomState 类中的字段。可以用来获取自定义的 globalState。
         @param jsonObject 发生变化的所有 RoomState 字段。
         @since 2.4.7
         */
        void onRoomStateChanged(JSONObject jsonObject);
    }
}