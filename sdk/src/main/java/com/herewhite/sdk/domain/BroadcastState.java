package com.herewhite.sdk.domain;

/**
 * 视角状态，包含主播用户信息
 */
public class BroadcastState extends WhiteObject {

    private ViewMode mode;
    private Long broadcasterId;
    private RoomMember broadcasterInformation;

    public ViewMode getMode() {
        return mode;
    }

    /**
     * 获取主播在房间中的 memberId
     * <p>
     * 2.4.6 前，当房间中没有主播时，错误的返回了 0。
     * 2.4.8 修复了该问题。
     *
     * @return 主播 memberId
     */
    public Long getBroadcasterId() {
        return broadcasterId;
    }

    /**
     * 主播信息字段
     *
     * @return 主播信息字段
     */
    public RoomMember getBroadcasterInformation() {
        return broadcasterInformation;
    }
}
