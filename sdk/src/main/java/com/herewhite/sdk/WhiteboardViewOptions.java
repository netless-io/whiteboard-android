package com.herewhite.sdk;

import com.herewhite.sdk.domain.AssetsHttpsOptions;

/**
 * `WhiteboardViewOptions` 类，用于配置 WhiteboardView 的初始化选项。
 */
public class WhiteboardViewOptions {
    private boolean enableAssetsHttps = false;
    private AssetsHttpsOptions assetsHttpsOptions;

    public WhiteboardViewOptions() {
    }

    /**
     * 获取是否启用 HTTPS 资源加载。
     *
     * @return 是否启用 HTTPS 资源加载。
     */
    public boolean isEnableAssetsHttps() {
        return enableAssetsHttps;
    }

    /**
     * 设置是否启用 HTTPS 资源加载。
     * <p>
     * WhiteboardView 默认使用 file 方式加载内置白板资源，这是最稳定的运行模式。
     * 对于需要突破 file 协议限制的特殊场景，可通过本方法显式启用
     * 基于 WebViewAssetLoader 的 HTTPS 资源加载方案。
     *
     * @param enableAssetsHttps 是否启用 HTTPS 资源加载。
     * @return 当前 WhiteboardViewOptions 实例，支持链式调用。
     */
    public WhiteboardViewOptions setEnableAssetsHttps(boolean enableAssetsHttps) {
        this.enableAssetsHttps = enableAssetsHttps;
        if (enableAssetsHttps && this.assetsHttpsOptions == null) {
            this.assetsHttpsOptions = new AssetsHttpsOptions();
        }
        return this;
    }

    /**
     * 获取 HTTPS 资源加载选项。
     *
     * @return HTTPS 资源加载选项，如果未启用则返回 null。
     */
    public AssetsHttpsOptions getAssetsHttpsOptions() {
        return assetsHttpsOptions;
    }

    /**
     * 设置 HTTPS 资源加载选项。
     * <p>
     * 设置此选项会自动启用 HTTPS 资源加载。
     *
     * @param assetsHttpsOptions HTTPS 资源加载选项。
     * @return 当前 WhiteboardViewOptions 实例，支持链式调用。
     */
    public WhiteboardViewOptions setAssetsHttpsOptions(AssetsHttpsOptions assetsHttpsOptions) {
        this.assetsHttpsOptions = assetsHttpsOptions;
        if (assetsHttpsOptions != null) {
            this.enableAssetsHttps = true;
        }
        return this;
    }
}

