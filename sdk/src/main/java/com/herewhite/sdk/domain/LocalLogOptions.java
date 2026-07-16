package com.herewhite.sdk.domain;

/**
 * WebView 内本地日志配置。
 */
public class LocalLogOptions extends WhiteObject {
    private Boolean enabled;
    private Boolean enabledUpload;

    public Boolean getEnabled() {
        return enabled;
    }

    /**
     * 设置是否开启 WebView 内本地日志。默认为关闭，需要显式设置为 true。
     */
    public LocalLogOptions setEnabled(Boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public Boolean getEnabledUpload() {
        return enabledUpload;
    }

    /**
     * 设置是否触发本地日志上传。该开关仅在 enabled 为 true 时生效，不影响本地日志写入。
     */
    public LocalLogOptions setEnabledUpload(Boolean enabledUpload) {
        this.enabledUpload = enabledUpload;
        return this;
    }
}
