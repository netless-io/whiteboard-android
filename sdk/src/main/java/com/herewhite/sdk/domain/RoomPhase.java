package com.herewhite.sdk.domain;

/**
 * Created by buhe on 2018/8/12.
 */

/**
 * 房间连接状态。
 */
public enum RoomPhase {
    /**
     * 连接中。
     */
    connecting,
    /**
     * 已连接，
     */
    connected,
    /**
     * 正在重连。
     */
    reconnecting,
    /**
     * 正在断开连接。
     */
    disconnecting,
    /**
     * 已经断开连接。
     */
    disconnected,
}
