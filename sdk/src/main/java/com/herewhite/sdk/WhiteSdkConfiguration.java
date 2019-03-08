package com.herewhite.sdk;

import com.herewhite.sdk.domain.DeviceType;

/**
 * Created by buhe on 2018/8/10.
 */

public class WhiteSdkConfiguration {
    private DeviceType deviceType;
    private double zoomMaxScale;
    private double zoomMinScale;
    private boolean debug;
    private boolean hasUrlInterrupterAPI = false;

    public WhiteSdkConfiguration(DeviceType deviceType, double zoomMaxScale, double zoomMinScale) {
        this(deviceType, zoomMaxScale, zoomMinScale, false);
    }

    public WhiteSdkConfiguration(DeviceType deviceType, double zoomMaxScale, double zoomMinScale, boolean debug) {
        this.deviceType = deviceType;
        this.zoomMaxScale = zoomMaxScale;
        this.zoomMinScale = zoomMinScale;
        this.debug = debug;
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

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public boolean isHasUrlInterrupterAPI() {
        return hasUrlInterrupterAPI;
    }

    public void setHasUrlInterrupterAPI(boolean hasUrlInterrupterAPI) {
        this.hasUrlInterrupterAPI = hasUrlInterrupterAPI;
    }
}
