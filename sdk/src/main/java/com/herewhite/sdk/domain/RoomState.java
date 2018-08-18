package com.herewhite.sdk.domain;

/**
 * Created by buhe on 2018/8/12.
 */

public class RoomState {

    private GlobalState globalState;
    private MemberState memberState;
    private BroadcastState broadcastState;
    private LinearTransformationDescription transform;
    private RoomMember[] roomMembers;
    private String[] pptImages;

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

    public LinearTransformationDescription getTransform() {
        return transform;
    }

    public void setTransform(LinearTransformationDescription transform) {
        this.transform = transform;
    }

    public RoomMember[] getRoomMembers() {
        return roomMembers;
    }

    public void setRoomMembers(RoomMember[] roomMembers) {
        this.roomMembers = roomMembers;
    }

    public String[] getPptImages() {
        return pptImages;
    }

    public void setPptImages(String[] pptImages) {
        this.pptImages = pptImages;
    }
}