package com.herewhite.sdk;

import com.herewhite.sdk.domain.RoomPhase;
import com.herewhite.sdk.domain.RoomState;

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
     @param modifyState 只包含发生变化的 RoomState 内容，未发生改变的内容，均为空
     */
    void onRoomStateChanged(RoomState modifyState);

    /** 用户错误事件捕获，附带用户 id，以及错误原因 */
    void onCatchErrorWhenAppendFrame(long userId, Exception error);
}