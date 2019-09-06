package com.herewhite.sdk.domain;

/**
 * {@link com.herewhite.sdk.RoomParams#setUserPayload(Object)} Payload 已支持自定义格式内容
 * 上传
 */
public class MemberInformation extends WhiteObject {
    private Long id;
    private String nickName;
    private String avatar;
    private String userId;

    public MemberInformation() {

    }

    public MemberInformation(String userId) {
        this.userId = userId;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getNickName() {
        return nickName;
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
}
