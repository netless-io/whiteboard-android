package com.herewhite.sdk;

import com.herewhite.sdk.domain.CameraBound;
import com.herewhite.sdk.domain.MemberInformation;
import com.herewhite.sdk.domain.Region;
import com.herewhite.sdk.domain.WhiteObject;

import java.util.concurrent.TimeUnit;

/**
 * Created by buhe on 2018/8/11.
 */

/**
 * 加入房间时的参数配置类，类似 {@link WhiteSdkConfiguration}
 */
public class RoomParams extends WhiteObject {

    private String uuid;
    private String roomToken;

    /**
     * 与 {@link WhiteSdkConfiguration#setRegion(Region)} 一致，
     * 如果 {@link WhiteSdkConfiguration#setRegion(Region)} 已经配置，此处可以不填。
     * 配置后，会在加入房间时，覆盖 {@link WhiteSdkConfiguration} 中的 region
     *
     * @param region
     */
    public void setRegion(Region region) {
        this.region = region;
    }

    public Region getRegion() {
        return region;
    }

    private Region region;
    private CameraBound cameraBound;

    /**
     * 重连时，最大重连尝试时间，单位：毫秒，默认 45 秒。
     */
    private long timeout = 45000;

    public boolean isWritable() {
        return isWritable;
    }

    /**
     * 互动模式API，设置为订阅（false）的房间，无法操作影响房间的 API。
     * 设置为 false，该用户，将不在成员列表中，其他用户无法得知该用户的存在。
     * 默认 true
     *
     * @param writable
     */
    public void setWritable(boolean writable) {
        isWritable = writable;
    }

    private boolean isWritable = true;

    public boolean getDisableEraseImage() {
        return disableEraseImage;
    }

    /**
     * 设置橡皮擦教具是否能够擦除图片，true 不能擦除图片；false 为可以擦除图片。默认 false；
     *
     * @param disableEraseImage
     */
    public void setDisableEraseImage(boolean disableEraseImage) {
        this.disableEraseImage = disableEraseImage;
    }

    /**
     * 设置加入房间时的超时时间
     *
     * @param timeout  时长长度
     * @param timeUnit 时长单位
     */
    public void setTimeout(long timeout, TimeUnit timeUnit) {
        this.timeout = TimeUnit.MILLISECONDS.convert(timeout, timeUnit);
    }

    private boolean disableEraseImage = false;

    public boolean isDisableDeviceInputs() {
        return disableDeviceInputs;
    }

    /**
     * 禁止教具响应用户输入，会覆盖 {@link WhiteSdkConfiguration#setDisableDeviceInputs(boolean)} 的配置
     *
     * @param disableDeviceInputs 是否禁止响应用户输入。默认 false，即响应用户输入。
     * @since 2.5.0
     */
    public void setDisableDeviceInputs(boolean disableDeviceInputs) {
        this.disableDeviceInputs = disableDeviceInputs;
    }

    public boolean isDisableOperations() {
        return disableOperations;
    }

    /**
     * 禁止响应用户任何操作。教具无法输入内容，同时用户无法对房间进行视角缩放，移动等操作。
     * 计费时，还是全价。
     *
     * @param disableOperations 禁止响应用户操作。默认 false，即响应用户任何操作。
     * @since 2.5.0
     * @deprecated 请使用 {@link #setDisableDeviceInputs(boolean)} {@link #setDisableCameraTransform(boolean)}
     */
    public void setDisableOperations(boolean disableOperations) {
        this.disableCameraTransform = disableOperations;
        this.disableDeviceInputs = disableOperations;
        this.disableOperations = disableOperations;
    }

    public boolean isDisableBezier() {
        return disableBezier;
    }

    /**
     * 关闭贝塞尔曲线优化，主要作用在线条的展示效果上。
     *
     * @param disableBezier 关闭贝塞尔曲线优化。默认 false，即开启贝塞尔曲线优化。
     * @since 2.5.0
     */
    public void setDisableBezier(boolean disableBezier) {
        this.disableBezier = disableBezier;
    }

    private boolean disableDeviceInputs = false;
    private boolean disableOperations = false;

    public boolean isDisableCameraTransform() {
        return disableCameraTransform;
    }

    /**
     * 禁止本地用户视野移动，默认 false，允许用户移动；true 则禁止用户移动视野
     */
    public void setDisableCameraTransform(boolean disableCameraTransform) {
        this.disableCameraTransform = disableCameraTransform;
    }

    private boolean disableCameraTransform = false;
    private boolean disableBezier = false;

    public CameraBound getCameraBound() {
        return cameraBound;
    }

    /**
     * 锁定白板的视野范围范围
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
     * <p>
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

    /**
     * 初始化 RoomParam 配置类
     *
     * @param uuid      房间 uuid
     * @param roomToken 房间 roomToken
     */
    public RoomParams(String uuid, String roomToken) {
        this(uuid, roomToken, (Object) null);
    }

    /**
     * 配置实时房间参数
     *
     * @param uuid       实时房间 uuid
     * @param roomToken  实时房间 token
     * @param memberInfo 用户信息，仅限对应字段
     * @see MemberInformation 已弃用，现已支持更高自由度的用户信息定义
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
     * @param uuid        实时房间 uuid
     * @param roomToken   实时房间 token
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
     * @deprecated 2.0.0 请使用 {@link #getUserPayload()}
     */
    @Deprecated
    public MemberInformation getMemberInfo() {
        if (userPayload instanceof MemberInformation) {
            return (MemberInformation) userPayload;
        }
        return null;
    }

    /**
     * 设置用户信息
     *
     * @param memberInfo {@link MemberInformation} 已弃用
     * @deprecated 2.0.0 请使用 {@link #setUserPayload(Object)} 设置自定义用户信息。
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
