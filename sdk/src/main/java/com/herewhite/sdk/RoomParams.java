package com.herewhite.sdk;

import com.herewhite.sdk.domain.MemberInformation;
import com.herewhite.sdk.domain.WhiteObject;

/**
 * Created by buhe on 2018/8/11.
 */

public class RoomParams extends WhiteObject {

    private String uuid;
    private String roomToken;
    private MemberInformation memberInfo;

    public Object getUserPayload() {
        return userPayload;
    }

    /**
     * 配置需要透传的用户信息，推荐使用 {@link com.google.gson.JsonObject} key-value object
     *
     * 如果需要显示用户头像地址，请在用户信息的 avatar 字段中，添加用户头像图片地址。
     * 从 {@link MemberInformation} 迁移，只需要在 userPayload 中，传入相同字段即可。
     *
     * @param userPayload 用户信息，完全自由定义，会被完整传递
     * @since 2.0.0
     */
    public void setUserPayload(Object userPayload) {
        this.userPayload = userPayload;
    }

    private Object userPayload;

    public RoomParams(String uuid, String roomToken) {
        this(uuid, roomToken, (Object) null);
    }

    /**
     * 配置实时房间参数
     *
     * @see MemberInformation 已弃用，现已支持更高自由度的用户信息定义
     * @param uuid       实时房间 uuid
     * @param roomToken  实时房间 token
     * @param memberInfo 用户信息，仅限对应字段
     * @deprecated 请使用 {@link #RoomParams(String, String, Object)} 传入用户信息。
     */
    @Deprecated
    public RoomParams(String uuid, String roomToken, MemberInformation memberInfo) {
        this.uuid = uuid;
        this.roomToken = roomToken;
        this.memberInfo = memberInfo;
    }

    /**
     * 配置实时房间参数
     *
     * @param uuid       实时房间 uuid
     * @param roomToken  实时房间 token
     * @param userPayload 自定义用户字段，参考 {@link #setUserPayload(Object)} 说明
     * @since 2.0.0
     */
    public RoomParams(String uuid, String roomToken, Object userPayload) {
        this.uuid = uuid;
        this.roomToken = roomToken;
        this.userPayload = userPayload;
    }

    /**
     * 获取设置的用户信息
     *
     * @return 用户信息
     * @deprecated 请使用 {@link #getUserPayload()}
     */
    @Deprecated
    public MemberInformation getMemberInfo() { return memberInfo; }

    /**
     * 设置用户信息
     *
     * @param memberInfo {@link MemberInformation} 已弃用
     * @deprecated 请使用 {@link #setUserPayload(Object)} 设置自定义用户信息。
     */
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

}
