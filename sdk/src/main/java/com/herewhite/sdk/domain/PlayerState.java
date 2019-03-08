package com.herewhite.sdk.domain;

public class PlayerState {
    private GlobalState globalState;
    /**
     * 房间用户状态
     */
    private RoomMember[] roomMembers;
    /**
     * 用户观察状态
     */
    private PlayerObserverMode observerState;

    public GlobalState getGlobalState() {
        return globalState;
    }

    public void setGlobalState(GlobalState globalState) {
        this.globalState = globalState;
    }

    public RoomMember[] getRoomMembers() {
        return roomMembers;
    }

    public void setRoomMembers(RoomMember[] roomMembers) {
        this.roomMembers = roomMembers;
    }

    public PlayerObserverMode getObserverState() {
        return observerState;
    }

    public void setObserverState(PlayerObserverMode observerState) {
        this.observerState = observerState;
    }
}
