package com.herewhite.sdk.domain;

/**
 * 用户信息类。
 *
 * @deprecated 该类已废弃。请使用 {@link com.herewhite.sdk.RoomParams#setUserPayload(Object)} 自定义用户信息。
 */
public class MemberInformation extends WhiteObject {
    private Long id;
    private String nickName;
    private String avatar;
    private String userId;

    /**
     * `MemberInformation` 构造方法，用于初始化用户信息实例。
     */
    public MemberInformation() {

    }

    /**
     * `MemberInformation` 构造方法，用于初始化用户信息实例。
     *
     * @param userId 用户 ID。
     */
    public MemberInformation(String userId) {
        this.userId = userId;
    }

    /**
     * 获取 long 型的用户 ID。
     * <p>
     * 该方法用于自定义的用户 ID。
     *
     * @return long 型的用户 ID。
     */
    public long getId() {
        return id;
    }

    /**
     * 设置 long 型的用户 ID。
     * <p>
     * 该方法用于设置自定义的用户 ID。
     *
     * @param id long 型的用户 ID。
     */
    public void setId(long id) {
        this.id = id;
    }

    /**
     * 设置用户昵称。
     *
     * @return 用户昵称。
     */
    public String getNickName() {
        return nickName;
    }

    /**
     * 获取 String 型的用户 ID。
     *
     * @return String 型的用户 ID。
     */
    public String getUserId() {
        return userId;
    }

    /**
     * 设置 String 型的用户 ID。
     *
     * @param userId String 型的用户 ID。
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

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
