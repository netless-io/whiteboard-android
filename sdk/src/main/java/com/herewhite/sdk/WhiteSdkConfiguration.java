package com.herewhite.sdk;

import com.herewhite.sdk.domain.DeviceType;
import com.herewhite.sdk.domain.LoggerOptions;
import com.herewhite.sdk.domain.WhiteObject;

import java.util.HashMap;

/**
 * Created by buhe on 2018/8/10.
 */

public class WhiteSdkConfiguration extends WhiteObject {
    private DeviceType deviceType;
    //TODO: 兼容字段，大版本移除。
    private double zoomMaxScale;
    //TODO: 兼容字段，大版本移除。
    private double zoomMinScale;
    private boolean debug;
    private boolean enableInterrupterAPI = false;
    //TODO: 兼容字段，大版本移除。
    private boolean hasUrlInterrupterAPI = false;
    private boolean userCursor = false;
    private boolean onlyCallbackRemoteStateModify = false;
    private HashMap<String, String> font;

    public boolean isPreloadDynamicPPT() {
        return preloadDynamicPPT;
    }

    /**
     * 动态 PPT 预加载选项
     *
     * 在使用动态 PPT 的同时，加载动态 PPT 中所需要的图片资源
     *
     * @param preloadDynamicPPT 默认关闭，不进行预加载
     */
    public void setPreloadDynamicPPT(boolean preloadDynamicPPT) {
        this.preloadDynamicPPT = preloadDynamicPPT;
    }

    private boolean preloadDynamicPPT = false;

    public WhiteSdkConfiguration() {
        this.deviceType = DeviceType.touch;
    }

    public WhiteSdkConfiguration(DeviceType deviceType, double zoomMaxScale, double zoomMinScale) {
        this(deviceType, zoomMaxScale, zoomMinScale, false);
    }

    public WhiteSdkConfiguration(DeviceType deviceType, double zoomMaxScale, double zoomMinScale, boolean debug) {
        this.deviceType = deviceType;
        this.zoomMaxScale = zoomMaxScale;
        this.zoomMinScale = zoomMinScale;
        this.debug = debug;
    }

    public LoggerOptions getLoggerOptions() {
        return loggerOptions;
    }

    /**
     * 日志上报系统设置项
     *
     * @param loggerOptions {@link LoggerOptions}
     * @since 2.4.2
     */
    public void setLoggerOptions(LoggerOptions loggerOptions) {
        this.loggerOptions = loggerOptions;
    }

    private LoggerOptions loggerOptions;

    public HashMap<String, String> getFont() {
        return font;
    }

    /**
     * 文档转网页（动态 PPT）时，自定义字体地址。key-value 结构
     *
     * @param font
     * @since 2.2.0
     */
    public void setFont(HashMap<String, String> font) {
        this.font = font;
    }

    public DeviceType getDeviceType() {
        return deviceType;
    }

    /**
     * 显示用户头像
     * 需要保证对应用户在加入房间时，传入了 userPayload，并且 userPayload key-value 结构中，存在 avatar 字段
     *
     * @param userCursor 开关，默认关闭,即不显示用户头像
     */
    public void setUserCursor(boolean userCursor) { this.userCursor = userCursor; }

    public boolean isUserCursor() { return userCursor; }

    public boolean isOnlyCallbackRemoteStateModify() {
        return onlyCallbackRemoteStateModify;
    }

    public void setOnlyCallbackRemoteStateModify(boolean onlyCallbackRemoteStateModify) {
        this.onlyCallbackRemoteStateModify = onlyCallbackRemoteStateModify;
    }

    public void setDeviceType(DeviceType deviceType) {
        this.deviceType = deviceType;
    }

    public double getZoomMaxScale() {
        return zoomMaxScale;
    }

    public void setZoomMaxScale(double zoomMaxScale) {
        this.zoomMaxScale = zoomMaxScale;
    }

    public double getZoomMinScale() {
        return zoomMinScale;
    }

    public void setZoomMinScale(double zoomMinScale) {
        this.zoomMinScale = zoomMinScale;
    }

    public boolean isDebug() {
        return debug;
    }

    /**
     * 打印 debug 日志
     *
     * @param debug 默认关闭
     */
    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public boolean isHasUrlInterrupterAPI() {
        return hasUrlInterrupterAPI;
    }

    /**
     * 设置图片替换 API
     *
     * @param hasUrlInterrupterAPI 图片替换开关，默认关闭
     */
    public void setHasUrlInterrupterAPI(boolean hasUrlInterrupterAPI) {
        this.enableInterrupterAPI = hasUrlInterrupterAPI;
        this.hasUrlInterrupterAPI = hasUrlInterrupterAPI;
    }
}
