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
    public long getBroadcasterId() {
        return broadcasterId;
    }
    public MemberInformation getBroadcasterInformation() {
        return broadcasterInformation;
    }
}
