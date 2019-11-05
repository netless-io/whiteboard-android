package com.herewhite.sdk;

import com.herewhite.sdk.domain.CameraBound;
import com.herewhite.sdk.domain.MemberInformation;
import com.herewhite.sdk.domain.WhiteObject;

/**
 * Created by buhe on 2018/8/11.
 */

public class RoomParams extends WhiteObject {

    private String uuid;
    private String roomToken;
    private CameraBound cameraBound;

    public boolean getDisableEraseImage() {
        return disableEraseImage;
    }

    public void setDisableEraseImage(boolean disableEraseImage) {
        this.disableEraseImage = disableEraseImage;
    }

    private boolean disableEraseImage = false;

    /**
     * Is disable device inputs boolean.
     *
     * @return the boolean
     * @since 2.5.0
     */
    public boolean isDisableDeviceInputs() {
        return disableDeviceInputs;
    }

    /**
     * 禁止教具响应用户输入
     *
     * @param disableDeviceInputs 是否禁止响应用户输入。默认 false，即响应用户输入。
     * @since 2.5.0
     */
    public void setDisableDeviceInputs(boolean disableDeviceInputs) {
        this.disableDeviceInputs = disableDeviceInputs;
    }

    /**
     * Is disable operations boolean.
     *
     * @return the boolean
     * @since 2.5.0
     */
    public boolean isDisableOperations() {
        return disableOperations;
    }

    /**
     * 只读，禁止响应用户任何操作。
     *
     * @param disableOperations 禁止响应用户操作。默认 false，即响应用户任何操作。
     * @since 2.5.0
     */
    public void setDisableOperations(boolean disableOperations) {
        this.disableOperations = disableOperations;
    }

    /**
     * Is disable bezier boolean.
     *
     * @return the boolean
     * @since 2.5.0
     */
    public boolean isDisableBezier() {
        return disableBezier;
    }

    /**
     * 关闭贝塞尔曲线优化
     *
     * @param disableBezier 关闭贝塞尔曲线优化。默认 false，即开启贝塞尔曲线优化。
     * @since 2.5.0
     */
    public void setDisableBezier(boolean disableBezier) {
        this.disableBezier = disableBezier;
    }

    private boolean disableDeviceInputs = false;
    private boolean disableOperations = false;
    private boolean disableBezier = false;

    /**
     * Gets camera bound.
     *
     * @return the camera bound
     * @since 2.5.0
     */
    public CameraBound getCameraBound() {
        return cameraBound;
    }

    /**
     * 锁定画布范围
     *
     * @param cameraBound 画布范围 {@link CameraBound}
     * @since 2.5.0
     */
    public void setCameraBound(CameraBound cameraBound) {
        this.cameraBound = cameraBound;
    }

    public Object getUserPayload() {
        return userPayload;
    }

    /**
     * 配置需要透传的用户信息，推荐使用 {@link WhiteObject} 子类，以保证字段结构正确
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
        this.userPayload = memberInfo;
    }

    /**
     * 配置实时房间参数
     *
     * @param uuid       实时房间 uuid
     * @param roomToken  实时房间 token
     * @param userPayload 自定义用户字段，参考 {@link #setUserPayload(Object)} key-value 结构，请使用自定义后的 {@link WhiteObject} 子类
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
    public MemberInformation getMemberInfo() {
        if (userPayload instanceof MemberInformation) {
            return (MemberInformation)userPayload;
        }
        return null;
    }

    /**
     * 设置用户信息
     *
     * @param memberInfo {@link MemberInformation} 已弃用
     * @deprecated 请使用 {@link #setUserPayload(Object)} 设置自定义用户信息。
     */
    @Deprecated
    public void setMemberInfo(MemberInformation memberInfo) {
        this.userPayload = memberInfo;
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

}
