package com.herewhite.sdk.domain;

/**
 * 视角状态，包含视角为主播模式的用户信息。
 */
public class BroadcastState extends WhiteObject {

    private ViewMode mode;
    private Long broadcasterId;
    private RoomMember broadcasterInformation;

    /**
     * 获取用户的视角模式。
     *
     * @return 用户的视角模式。
     */
    public ViewMode getMode() {
        return mode;
    }

    /**
     * 获取主播模式用户在房间中的用户 ID。
     * <p>
     * 2.4.6 版本前，当房间中没有主播时，错误地返回了 0。
     * 2.4.8 版本修复了该问题。修复后，当房间内没有主播时，返回值为空。
     *
     * @return 主播模式用户的用户 ID。
     */
    public Long getBroadcasterId() {
        return broadcasterId;
    }

    /**
     * 获取主播模式用户的用户信息。
     *
     * @return 用户信息。详见 {@link RoomMember RoomMember}。
     */
    public RoomMember getBroadcasterInformation() {
        return broadcasterInformation;
    }
}
