package com.herewhite.sdk.domain;

/**
 * Created by buhe on 2018/8/12.
 */

public class RoomState {

    private GlobalState globalState;
    private MemberState memberState;
    private BroadcastState broadcastState;
    private SceneState sceneState;
    private RoomMember[] roomMembers;
    private Double zoomScale;

    public GlobalState getGlobalState() {
        return globalState;
    }

    public void setGlobalState(GlobalState globalState) {
        this.globalState = globalState;
    }

    public MemberState getMemberState() {
        return memberState;
    }

    public void setMemberState(MemberState memberState) {
        this.memberState = memberState;
    }

    public BroadcastState getBroadcastState() {
        return broadcastState;
    }

    public void setBroadcastState(BroadcastState broadcastState) {
        this.broadcastState = broadcastState;
    }

    public RoomMember[] getRoomMembers() {
        return roomMembers;
    }

    public void setRoomMembers(RoomMember[] roomMembers) {
        this.roomMembers = roomMembers;
    }

    public Double getZoomScale() {
        return zoomScale;
    }

    public void setZoomScale(Double zoomScale) {
        this.zoomScale = zoomScale;
    }

    public SceneState getSceneState() {
        return sceneState;
    }

    public void setSceneState(SceneState sceneState) {
        this.sceneState = sceneState;
    }
}
