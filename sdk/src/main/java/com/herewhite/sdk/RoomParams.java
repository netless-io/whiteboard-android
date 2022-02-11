package com.herewhite.sdk;

import com.herewhite.sdk.domain.CameraBound;
import com.herewhite.sdk.domain.MemberInformation;
import com.herewhite.sdk.domain.Region;
import com.herewhite.sdk.domain.WhiteObject;
import com.herewhite.sdk.domain.WindowParams;

import java.util.concurrent.TimeUnit;


// Created by buhe on 2018/8/11.

/**
 * `RoomParams` 类，用于配置实时房间的参数。
 *
 * @note `RoomParams` 类中所有的方法都必须在 `joinRoom` 前调用；成功加入房间后，调用该类中的任何方法都不会生效。
 */
public class RoomParams extends WhiteObject {

    private String uuid;
    private String roomToken;
    private String uid;

    /**
     * 设置数据中心。
     *
     * @note
     * - 该方法设置的数据中心必须与要加入的互动白板实时房间所在数据中心一致，否则无法加入房间。
     * - 该方法与 `WhiteSdkConfiguration` 类中的 {@link WhiteSdkConfiguration#setRegion(Region) setRegion} 方法作用相同，两个方法只需要调用其中的一个。如果同时调用，该方法会覆盖 `WhiteSdkConfiguration` 类中的 {@link WhiteSdkConfiguration#setRegion(Region) setRegion}。
     *
     * @param region 数据中心，详见 {@link com.herewhite.sdk.domain.Region Region}。
     */
    public void setRegion(Region region) {
        this.region = region;
    }

    /**
     * 获取设置的数据中心。
     *
     * @return 设置的数据中心，详见 {@link com.herewhite.sdk.domain.Region Region}。
     */
    public Region getRegion() {
        return region;
    }

    private Region region;
    private CameraBound cameraBound;

    /**
     * 重连时，最大重连尝试时间，单位：毫秒，默认 45 秒。
     */
    private long timeout = 45000;

    /**
     * 获取用户是否以互动模式加入白板房间。
     *
     * @return 用户是否以互动模式加入白板房间：
     * - `true`：以互动模式加入白板房间，即具有读写权限。
     * - `false`：以订阅模式加入白板房间，即具有只读权限。
     */
    public boolean isWritable() {
        return isWritable;
    }

    /**
     * 设置用户是否以互动模式加入白板房间。
     * <p>
     * 用户可以以以下模式加入互动白板实时房间：
     * - 互动模式：对白板具有读写权限，会出现在房间的成员列表中，对其他用户可见。
     * - 订阅模式：对白板具有只读权限，不会出现在房间的成员列表中，对其他用户不可见。
     *
     * @param writable 用户是否以互动模式加入白板房间：
     *                 - `true`：（默认）以互动模式加入白板房间。
     *                 - `false`：以订阅模式加入白板房间。
     */
    public void setWritable(boolean writable) {
        isWritable = writable;
    }

    private boolean isWritable = true;

    /**
     * 获取是否关闭橡皮擦擦除图片功能。
     *
     * @return 是否关闭橡皮擦擦除图片功能：
     * - `true`：橡皮擦不可以擦除图片。
     * - `false`：橡皮擦可以擦除图片。
     */
    public boolean getDisableEraseImage() {
        return disableEraseImage;
    }

    /**
     * 设置是否关闭橡皮擦擦除图片功能。
     * <p>
     * 默认情况下，橡皮擦可以擦除白板上的所有内容，包括图片。你可以调用 `setDisableEraseImage(true)` 设置橡皮擦不能擦除图片。
     *
     * @param disableEraseImage 是否关闭橡皮擦擦除图片功能：
     *                          - `true`：橡皮擦不可以擦除图片。
     *                          - `false`：（默认）橡皮擦可以擦除图片。
     */
    public void setDisableEraseImage(boolean disableEraseImage) {
        this.disableEraseImage = disableEraseImage;
    }

    /**
     * 设置加入房间的超时时间。
     *
     * @param timeout  超时时长，默认值为 45000 毫秒。
     * @param timeUnit 时长单位，默认值为毫秒 （`MILLISECONDS`），取值详见 [TimeUnit](https://www.android-doc.com/reference/java/util/concurrent/TimeUnit.html)。
     */
    public void setTimeout(long timeout, TimeUnit timeUnit) {
        this.timeout = TimeUnit.MILLISECONDS.convert(timeout, timeUnit);
    }

    private boolean disableEraseImage = false;

    /**
     * 获取是否禁止白板工具响应用户输入。
     *
     * @return 是否禁止白板工具响应用户输入：
     * - `true`：禁止白板工具响应用户输入。
     * - `false`：允许白板工具响应用户输入。
     */
    public boolean isDisableDeviceInputs() {
        return disableDeviceInputs;
    }

    /**
     * 开启/禁止白板工具响应用户输入。
     *
     * @since 2.5.0
     *
     * @param disableDeviceInputs 是否禁止白板工具响应用户输入：
     *   - `true`：禁止白板工具响应用户输入。
     *   - `false`：（默认）允许白板工具响应用户输入。
     */
    public void setDisableDeviceInputs(boolean disableDeviceInputs) {
        this.disableDeviceInputs = disableDeviceInputs;
    }

    /**
     * 获取是否禁止白板响应用户的操作。
     *
     * @return 是否禁止白板响应用户的操作。
     * - `true`：禁止白板响应用户的操作。
     * - `false`：允许白板响应用户的操作。
     */
    public boolean isDisableOperations() {
        return disableOperations;
    }

    /**
     * 允许/禁止白板响应用户任何操作。
     *
     * @since 2.5.0
     *
     * @deprecated 该方法已废弃。请使用 {@link #setDisableDeviceInputs(boolean) setDisableDeviceInputs} 和 {@link #setDisableCameraTransform(boolean) setDisableCameraTransform}。
     * <p>
     * 禁止白板响应用户任何操作后，用户无法使用白板工具输入内容，也无法对白板进行视角缩放和视角移动。
     *
     * @param disableOperations 是否禁止白板响应用户的操作：
     *  - `true`：禁止白板响应用户的操作。
     *  - `false`：（默认）允许白板响应用户的操作。
     */
    public void setDisableOperations(boolean disableOperations) {
        this.disableCameraTransform = disableOperations;
        this.disableDeviceInputs = disableOperations;
        this.disableOperations = disableOperations;
    }

    /**
     * 获取是否关闭贝塞尔曲线优化。
     *
     * @return 是否关闭贝塞尔曲线优化：
     * - `true`: 关闭贝塞尔曲线优化。
     * - `false`: 开启贝塞尔曲线优化。
     */
    public boolean isDisableBezier() {
        return disableBezier;
    }

    /**
     * 设置是否关闭贝塞尔曲线优化。
     *
     * @since 2.5.0
     *
     * @param disableBezier 是否关闭贝塞尔曲线优化：
     * - `true`: 关闭贝塞尔曲线优化。
     * - `false`: （默认）开启贝塞尔曲线优化。
     *
     */
    public void setDisableBezier(boolean disableBezier) {
        this.disableBezier = disableBezier;
    }

    private boolean disableDeviceInputs = false;
    private boolean disableOperations = false;

    /**
     * 获取是否禁止本地用户操作白板视角。
     *
     * @return 是否禁止本地用户操作白板视角：
     * - `true`：禁止本地用户操作白板视角。
     * - `false`：允许本地用户操作白板视角。
     */
    public boolean isDisableCameraTransform() {
        return disableCameraTransform;
    }

    /**
     * 禁止/允许本地用户操作白板的视角，包括缩放和移动视角。
     *
     * @param disableCameraTransform 是否禁止本地用户操作白板视角：
     *                               - `true`：禁止本地用户操作白板视角。
     *                               - `false`：（默认）允许本地用户操作白板视角。
     */
    public void setDisableCameraTransform(boolean disableCameraTransform) {
        this.disableCameraTransform = disableCameraTransform;
    }

    private boolean disableCameraTransform = false;
    private boolean disableBezier = false;

    /**
     * 获取是否关闭笔锋效果。
     *
     * @return 是否关闭笔锋效果：
     * - true: 关闭笔锋效果。
     * - false: 开启笔锋效果。
     */
    public boolean isDisableNewPencil() {
        return disableNewPencil;
    }

    /**
     * 关闭/开启笔锋效果。
     * @since 2.12.2
     *
     * @note
     * - 在 2.12.2 版本中，`setDisableNewPencil` 的默认值为 `false`，自 2.12.3 版本起，`setDisableNewPencil` 的默认值改为 `true`。
     * - 为正常显示笔迹，在开启笔峰效果前，请确保该房间内的所有用户使用如下 SDK：
     *      - Android SDK 2.12.3 版或之后
     *      - iOS SDK 2.12.3 版或之后
     *      - Web SDK 2.12.5 版或之后
     *
     * @param disableNewPencil 是否关闭笔锋效果：
     * - true: （默认）关闭笔锋效果。
     * - false: 开启笔锋效果。
     */
    public void setDisableNewPencil(boolean disableNewPencil) {
        this.disableNewPencil = disableNewPencil;
    }

    private boolean disableNewPencil = true;

    /**
     * 获取视角边界。
     *
     * @return 视角边界。
     */
    public CameraBound getCameraBound() {
        return cameraBound;
    }

    /**
     * 设置本地用户的视角边界。
     *
     * @since 2.5.0
     *
     * @param cameraBound 视角边界，详见 {@link com.herewhite.sdk.domain.CameraBound CameraBound}。
     */
    public void setCameraBound(CameraBound cameraBound) {
        this.cameraBound = cameraBound;
    }

    /**
     * 获取自定义用户信息。
     *
     * @return 自定义用户信息。
     */
    public Object getUserPayload() {
        return userPayload;
    }

    /**
     * 自定义用户信息。
     *
     * @since 2.0.0
     *
     * 你可以在 `userPayload` 中传入自定义的用户信息，例如用户ID，昵称和头像，然后调用此方法将信息发送给应用程序。
     *
     * @note
     * 为确保传入的 `userPayload` 格式正确，必须使用 {@link com.herewhite.sdk.domain.WhiteObject WhiteObject} 子类。
     *
     * @param userPayload 自定义的用户信息，必须为 key-value 结构，例如，`"avatar", "https://example.com/user.png")`。
     */
    public void setUserPayload(Object userPayload) {
        this.userPayload = userPayload;
    }

    private Object userPayload;

    /**
     * 初始化房间配置参数。
     *
     * @param uuid      房间 UUID， 即房间唯一标识符。传入的房间 UUID 必须和生成 Room Token 时填入的房间 UUID 一致。
     * @param roomToken 用于鉴权的 Room Token。
     * @param uid 用户标识，可以为任意 string，字符串长度不能超过 1024，2.15.0 后必须填写。
     */
    public RoomParams(String uuid, String roomToken, String uid) {
        this(uuid, roomToken, uid, (Object) null);
    }

    /**
     * 初始化房间配置参数并传入用户信息。
     *
     * @deprecated 该方法已经废弃。请使用 {@link RoomParams(String, String, Object) RoomParams}[2/2]。
     *
     * @param uuid       房间 UUID， 即房间唯一标识符。传入的房间 UUID 必须和生成 Room Token 时填入的房间 UUID 一致。
     * @param roomToken  用于鉴权的 Room Token。
     * @param uid 用户标识，可以为任意 string，字符串长度不能超过 1024，2.15.0 后必须填写。
     * @param memberInfo 自定义用户信息，详见 {@link com.herewhite.sdk.domain.MemberInformation MemberInformation}。
     *
     */
    @Deprecated
    public RoomParams(String uuid, String roomToken, String uid, MemberInformation memberInfo) {
        this(uuid, roomToken, uid, (Object) memberInfo);
    }

    /**
     * 初始化房间配置参数并传入自定义的用户信息。
     *
     * @since 2.0.0
     *
     * @param uuid        房间 UUID， 即房间唯一标识符。传入的房间 UUID 必须和生成 Room Token 时填入的房间 UUID 一致。
     * @param roomToken   用于鉴权的 Room Token。
     * @param uid 用户标识，可以为任意 string，字符串长度不能超过 1024, 2.15.0 后必须填写。
     * @param userPayload 自定义用户信息，必须为 {@link com.herewhite.sdk.domain.WhiteObject WhiteObject} 子类。
     */
    public RoomParams(String uuid, String roomToken, String uid, Object userPayload) {
        this.uuid = uuid;
        this.roomToken = roomToken;
        this.uid = uid;
        this.userPayload = userPayload;
    }

    /**
     * 获取自定义的用户信息。
     *
     * @deprecated 该方法已废弃。请使用 {@link #getUserPayload() getUserPayload}。
     *
     * @return 自定义用户信息，详见 {@link com.herewhite.sdk.domain.MemberInformation MemberInformation}。
     */
    @Deprecated
    public MemberInformation getMemberInfo() {
        if (userPayload instanceof MemberInformation) {
            return (MemberInformation) userPayload;
        }
        return null;
    }

    /**
     * 自定义用户信息。
     *
     * @deprecated 该方法已废弃。请使用 {@link #getUserPayload() getUserPayload}。
     *
     * @param memberInfo 用户信息，详见 {@link com.herewhite.sdk.domain.MemberInformation MemberInformation}。
     */
    @Deprecated
    public void setMemberInfo(MemberInformation memberInfo) {
        this.userPayload = memberInfo;
    }

    /**
     * 获取房间 UUID。
     *
     * @return 房间 UUID，即房间的唯一标识符。
     */
    public String getUuid() {
        return uuid;
    }

    /**
     * 设置房间 UUID。
     *
     * @param uuid 房间 UUID，即房间的唯一标识符。
     */
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    /**
     * 获取 Room Token。
     *
     * @return Room Token。
     */
    public String getRoomToken() {
        return roomToken;
    }

    /**
     * 设置 Room Token。
     *
     * @param roomToken 用于鉴权的 Room Token。生成该 Room Token 的房间 UUID 必须和上面传入的房间 UUID 一致。
     */
    public void setRoomToken(String roomToken) {
        this.roomToken = roomToken;
    }

    /**
     * 多窗口属性
     */
    private WindowParams windowParams;

    public WindowParams getWindowParams() {
        return windowParams;
    }

    public void setWindowParams(WindowParams windowParams) {
        this.windowParams = windowParams;
    }

    /**
     * 是否关闭 ``insertText`` 与 ``updateText`` 操作权限
     */
    private boolean disableTextOperations = false;

    public boolean isDisableTextOperations() {
        return disableTextOperations;
    }

    public void setDisableTextOperations(boolean disableTextOperations) {
        this.disableTextOperations = disableTextOperations;
    }
}
