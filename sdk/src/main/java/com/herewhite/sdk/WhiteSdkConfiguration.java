package com.herewhite.sdk;

import com.herewhite.sdk.domain.DeviceType;

/**
 * Created by buhe on 2018/8/10.
 */

public class WhiteSdkConfiguration {
    private DeviceType deviceType;
    private double zoomMaxScale;
    private double zoomMinScale;
    private boolean enableDebug;
    private boolean hasUrlInterrupterAPI = false;

    public WhiteSdkConfiguration(DeviceType deviceType, double zoomMaxScale, double zoomMinScale) {
        this(deviceType, zoomMaxScale, zoomMinScale, false);
    }

    public WhiteSdkConfiguration(DeviceType deviceType, double zoomMaxScale, double zoomMinScale, boolean enableDebug) {
        this.deviceType = deviceType;
        this.zoomMaxScale = zoomMaxScale;
        this.zoomMinScale = zoomMinScale;
        this.enableDebug = enableDebug;
    }

    public DeviceType getDeviceType() {
        return deviceType;
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

    public boolean isEnableDebug() {
        return enableDebug;
    }

    public void setEnableDebug(boolean enableDebug) {
        this.enableDebug = enableDebug;
    }

    public boolean isHasUrlInterrupterAPI() {
        return hasUrlInterrupterAPI;
    }

    public void setHasUrlInterrupterAPI(boolean hasUrlInterrupterAPI) {
        this.hasUrlInterrupterAPI = hasUrlInterrupterAPI;
    }
}
