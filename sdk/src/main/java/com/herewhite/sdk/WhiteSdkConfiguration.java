package com.herewhite.sdk;

import com.herewhite.sdk.domain.DeviceType;
import com.herewhite.sdk.domain.WhiteObject;

import java.util.HashMap;

/**
 * Created by buhe on 2018/8/10.
 */

public class WhiteSdkConfiguration extends WhiteObject {
    private DeviceType deviceType;
    private double zoomMaxScale;
    private double zoomMinScale;
    private boolean debug;
    private boolean hasUrlInterrupterAPI = false;
    private boolean userCursor = false;
    private boolean customCursor = false;
    private boolean onlyCallbackRemoteStateModify = false;
    private HashMap<String, String> font;

    public HashMap<String, String> getFont() {
        return font;
    }

    public void setFont(HashMap<String, String> font) {
        this.font = font;
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

    public DeviceType getDeviceType() {
        return deviceType;
    }

    public void setUserCursor(boolean userCursor) { this.userCursor = userCursor; }

    public boolean isUserCursor() { return userCursor; }

    public void setCustomCursor(boolean customCursor) { this.customCursor = customCursor; }
    public boolean isCustomCursor() { return customCursor; }

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
