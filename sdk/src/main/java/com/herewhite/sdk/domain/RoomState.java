package com.herewhite.sdk.domain;

/**
 * Created by buhe on 2018/8/12.
 */

public class RoomState extends WhiteDisplayerState {

    private MemberState memberState;
    private BroadcastState broadcastState;
    private Double zoomScale;

    public MemberState getMemberState() {
        return memberState;
    }

    public BroadcastState getBroadcastState() {
        return broadcastState;
    }

    public Double getZoomScale() {
        return zoomScale;
    }
}
