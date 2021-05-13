package com.herewhite.sdk.domain;

/**
 * Created by buhe on 2018/8/12.
 */

/**
 * 房间状态类。
 */
public class RoomState extends WhiteDisplayerState {

    private MemberState memberState;
    private BroadcastState broadcastState;
    private Double zoomScale;

    /**
     * 获取互动白板实时房间内当前的白板工具状态。
     *
     * @return 白板工具状态，详见 {@link MemberState MemberState}。
     */
    public MemberState getMemberState() {
        return memberState;
    }

    /**
     * 获取互动白板实时房间内当前的视角状态。
     *
     * @return 视角状态，详见 {@link BroadcastState BroadcastState}。
     */
    public BroadcastState getBroadcastState() {
        return broadcastState;
    }

    /**
     * 获取互动白板实时房间内当前的视角缩放比例。
     *
     * @deprecated 该方法已废弃。
     *
     * @return 视角缩放比例。
     */
    public Double getZoomScale() {
        return zoomScale;
    }
}
