package com.herewhite.sdk.domain;

/**
 * @deprecated 该接口已废弃。请使用 {@link com.herewhite.sdk.CommonCallback CommonCallback} 中的 {@link com.herewhite.sdk.CommonCallback#urlInterrupter(String)} 方法。
 */
public interface UrlInterrupter {
    /**
     * 拦截图片 URL。
     *
     * @param sourceUrl 原 URL 地址。
     * @return 替换后的图片地址。请确保在返回值中进行传参。
     */
    String urlInterrupter(String sourceUrl);
}
