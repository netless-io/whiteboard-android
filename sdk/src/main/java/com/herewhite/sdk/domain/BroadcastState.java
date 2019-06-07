package com.herewhite.sdk.domain;

/**
 * Created by buhe on 2018/8/13.
 */

public class BroadcastState extends WhiteObject {

    private ViewMode mode;
    private Long broadcasterId;
    private MemberInformation broadcasterInformation;

    public ViewMode getMode() {
        return mode;
    }

    public void setMode(ViewMode mode) {
        this.mode = mode;
    }

    public long getBroadcasterId() {
        return broadcasterId;
    }

    public void setBroadcasterId(long broadcasterId) {
        this.broadcasterId = broadcasterId;
    }

    public MemberInformation getBroadcasterInformation() {
        return broadcasterInformation;
    }

    public void setBroadcasterInformation(MemberInformation broadcasterInformation) {
        this.broadcasterInformation = broadcasterInformation;
    }
}
