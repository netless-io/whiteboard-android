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
     * 获取房间的教具状态。
     *
     * @return 房间的教具状态，详见 {@link MemberState MemberState}。
     */
    public MemberState getMemberState() {
        return memberState;
    }

    /**
     * 获取房间的视角状态。
     *
     * @return
     */
    public BroadcastState getBroadcastState() {
        return broadcastState;
    }

    /**
     * 获取房间的缩放比例。
     *
     * @return 房间的缩放比例。
     */
    public Double getZoomScale() {
        return zoomScale;
    }
}
