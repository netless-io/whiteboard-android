package com.herewhite.sdk.domain;

import java.util.Objects;

public class ConnectionPrepareParam {
    /**
     * 白板项目的唯一标识，可在控制台获取。
     * 需与 {@link com.herewhite.sdk.WhiteSdkConfiguration#WhiteSdkConfiguration(String)} 设置保持一致
     */
    private final String appId;

    /**
     * 用户的应用所在的区域 {@link Region}.
     */
    private final Region region;

    /**
     * 过期时间, 在时间内不做再次检查。默认为 12 小时
     */
    private final Long expire;

    public ConnectionPrepareParam(String appId, Region region) {
        this(appId, region, 12 * 60 * 60 * 1000L);
    }

    public ConnectionPrepareParam(String appId, Region region, Long expire) {
        this.appId = Objects.requireNonNull(appId, "App ID cannot be null");
        this.region = Objects.requireNonNull(region, "Region cannot be null");
        if (expire <= 0) {
            throw new IllegalArgumentException("Expire time must be positive");
        }
        this.expire = expire;
    }

    public String getAppId() {
        return appId;
    }

    public Region getRegion() {
        return region;
    }

    public Long getExpire() {
        return expire;
    }
}
