package com.herewhite.sdk.domain;

/**
 * 视角状态，包含主播用户信息
 */
public class BroadcastState extends WhiteObject {

    private ViewMode mode;
    private Long broadcasterId;
    private MemberInformation broadcasterInformation;

    /**
     * 用户透传数据,具体类型有 Gson 决定，key-value 字段会产生 Map
     *
     * @return 用户信息
     * @since 2.4.7
     */
    public Object getPayload() {
        return payload;
    }

    private Object payload;
    public ViewMode getMode() {
        return mode;
    }

    /**
     * 获取主播在房间中的 memberId
     *
     * 2.4.6 前，当房间中没有主播时，错误的返回了 0。
     * 2.4.7 修复了该问题。
     *
     * @return 主播 memberId
     */
    public Long getBroadcasterId() {
        return broadcasterId;
    }

    /**
     * 主播信息字段
     *
     * @deprecated 请使用 {@link #getPayload()} 获取完全自由的用户信息
     * @return 主播信息字段
     */
    public MemberInformation getBroadcasterInformation() {
        return broadcasterInformation;
    }
}
