package com.herewhite.sdk;

/**
 * Created by buhe on 2018/8/10.
 */

public class WhiteSdkConfiguration {
    private DeviceType deviceType;
    private double zoomMaxScale;
    private double zoomMinScale;

    public WhiteSdkConfiguration(DeviceType deviceType, double zoomMaxScale, double zoomMinScale) {
        this.deviceType = deviceType;
        this.zoomMaxScale = zoomMaxScale;
        this.zoomMinScale = zoomMinScale;
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
}
