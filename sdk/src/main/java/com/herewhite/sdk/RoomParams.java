package com.herewhite.sdk;

import com.herewhite.sdk.domain.MemberInformation;
import com.herewhite.sdk.domain.WhiteObject;

/**
 * Created by buhe on 2018/8/11.
 */

public class RoomParams extends WhiteObject {

    private long previousUserId;
    private String uuid;
    private String roomToken;
    private String sessionToken;
    private String userToken;
    private MemberInformation memberInfo;

    public RoomParams(String uuid, String roomToken) {
        this(uuid, roomToken, null);
    }

    public RoomParams(String uuid, String roomToken, MemberInformation memberInfo) {
        this.uuid = uuid;
        this.roomToken = roomToken;
        this.memberInfo = memberInfo;
    }

    public MemberInformation getMemberInfo() { return memberInfo; }
    public void setMemberInfo(MemberInformation memberInfo) { this.memberInfo = memberInfo; }

    public long getPreviousUserId() {
        return previousUserId;
    }

    public void setPreviousUserId(long previousUserId) {
        this.previousUserId = previousUserId;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getRoomToken() {
        return roomToken;
    }

    public void setRoomToken(String roomToken) {
        this.roomToken = roomToken;
    }

    public String getSessionToken() {
        return sessionToken;
    }

    public void setSessionToken(String sessionToken) {
        this.sessionToken = sessionToken;
    }

    public String getUserToken() {
        return userToken;
    }

    public void setUserToken(String userToken) {
        this.userToken = userToken;
    }
}
