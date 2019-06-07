package com.herewhite.sdk.domain;

/**
 * Created by buhe on 2018/8/12.
 */

public class RoomState extends WhiteObject {

    private GlobalState globalState;
    private MemberState memberState;
    private BroadcastState broadcastState;
    private SceneState sceneState;
    private RoomMember[] roomMembers;
    private Double zoomScale;

    public GlobalState getGlobalState() {
        return globalState;
    }
    public MemberState getMemberState() {
        return memberState;
    }
    public BroadcastState getBroadcastState() {
        return broadcastState;
    }
    public RoomMember[] getRoomMembers() {
        return roomMembers;
    }
    public Double getZoomScale() {
        return zoomScale;
    }
    public SceneState getSceneState() {
        return sceneState;
    }
}
