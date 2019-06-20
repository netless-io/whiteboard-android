package com.herewhite.sdk;

import com.herewhite.sdk.domain.MemberInformation;
import com.herewhite.sdk.domain.WhiteObject;

import org.json.JSONObject;

/**
 * Created by buhe on 2018/8/11.
 */

public class RoomParams extends WhiteObject {

    private String uuid;
    private String roomToken;
    private String sessionToken;
    private String userToken;
    private MemberInformation memberInfo;

    public Object getUserPayload() {
        return userPayload;
    }

    public void setUserPayload(Object userPayload) {
        this.userPayload = userPayload;
    }

    private Object userPayload;

    public RoomParams(String uuid, String roomToken) {
        this(uuid, roomToken, null);
    }

    /*
    * memberInfo 已弃用，请使用功能更强的 userPayload
    * @deprecated
    * */
    @Deprecated
    public RoomParams(String uuid, String roomToken, MemberInformation memberInfo) {
        this.uuid = uuid;
        this.roomToken = roomToken;
        this.memberInfo = memberInfo;
    }

    /*
    * userPayload 需要为String，Number，Null 等可以
    * */
    public RoomParams(String uuid, String roomToken, Object userPayload) {
        this.uuid = uuid;
        this.roomToken = roomToken;
        this.userPayload = userPayload;
    }

    @Deprecated
    public MemberInformation getMemberInfo() { return memberInfo; }

    @Deprecated
    public void setMemberInfo(MemberInformation memberInfo) { this.memberInfo = memberInfo; }

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
