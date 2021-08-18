package com.herewhite.sdk;

import com.herewhite.sdk.domain.RoomPhase;
import com.herewhite.sdk.domain.RoomState;


// Created by buhe on 2018/8/12.

/**
 * 房间事件回调接口。
 */
public interface RoomListener {

    /**
     * 房间连接状态变化回调。
     *
     * @param phase 房间的连接状态，详见 {@link com.herewhite.sdk.domain.RoomPhase RoomPhase}。
     */
    void onPhaseChanged(RoomPhase phase);

    /**
     * 白板 SDK 与白板服务器连接中断回调。
     *
     * @param e 错误信息。
     */
    void onDisconnectWithError(Exception e);

    /**
     * 用户被服务器移出房间回调。
     *
     * @param reason 用户被移除房间的原因。
     */
    void onKickedWithReason(String reason);

    /**
     * 房间状态属性发生变化回调。
     * <p>
     * 该回调仅返回发生变化的房间状态属性，未发生变化的房间状态字段，均未空。
     *
     * @param modifyState 发生变化的房间状态属性，详见 {@link com.herewhite.sdk.domain.RoomState RoomState}。
     */
    void onRoomStateChanged(RoomState modifyState);

    /**
     * 可撤销次数发生变化回调。
     * <p>
     * 当本地用户调用 {@link Room#undo undo} 撤销上一步操作时，会触发该回调，报告剩余的可撤销次数。
     *
     * @param canUndoSteps 剩余的可撤销次数。
     */
    void onCanUndoStepsUpdate(long canUndoSteps);

    /**
     * 可重做次数发生变化回调。
     * <p>
     * 当本地用户调用 {@link Room#redo redo} 重做上一步操作时，会触发该回调，报告剩余的可重做次数。
     *
     * @param canRedoSteps 剩余的可重做次数。
     */
    void onCanRedoStepsUpdate(long canRedoSteps);

    /**
     * 同步用户操作发生错误回调。
     *
     * @param userId 用户 ID，表明在同步哪个用户的操作时发生了错误。
     * @param error  错误原因。
     */
    void onCatchErrorWhenAppendFrame(long userId, Exception error);

    default void onAttributesUpdate(String valueOf) {}
}