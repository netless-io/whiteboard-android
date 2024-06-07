package com.herewhite.sdk.domain;

import java.util.Objects;

/**
 * `ConnectionPrepareParam`, 定义白板房间连接准备参数。
 */
public class ConnectionPrepareParam {
    // 白板项目的唯一标识，可在控制台获取。
    private final String appId;

    // 用户的应用所在的区域。
    private final Region region;

    // 过期时间。在此时间内不再进行检查。默认为12小时。
    private final Long expire;

    /**
     * 创建一个 {@link ConnectionPrepareParam} 实例。
     * @param appId 白板项目的唯一标识，可在控制台获取。
     * @param region 用户的应用所在的区域 {@link Region}.
     *
     * 过期时间默认为 12 小时。
     */
    public ConnectionPrepareParam(String appId, Region region) {
        this(appId, region, 12 * 60 * 60 * 1000L);
    }

    /**
     * 创建一个 {@link ConnectionPrepareParam} 实例。
     * @param appId 白板项目的唯一标识，可在控制台获取。
     * @param region 用户的应用所在的区域 {@link Region}.
     * @param expire 过期时间, 在时间内不做再次检查。
     */
    public ConnectionPrepareParam(String appId, Region region, Long expire) {
        this.appId = Objects.requireNonNull(appId, "App ID cannot be null");
        this.region = Objects.requireNonNull(region, "Region cannot be null");
        if (expire <= 0) {
            throw new IllegalArgumentException("Expire time must be positive");
        }
        this.expire = expire;
    }

    /**
     * 获取白板项目的唯一标识。
     * @return
     */
    public String getAppId() {
        return appId;
    }

    /**
     * 获取用户的应用所在的区域。
     * @return
     */
    public Region getRegion() {
        return region;
    }

    /**
     * 获取过期时间。
     * @return
     */
    public Long getExpire() {
        return expire;
    }
}
