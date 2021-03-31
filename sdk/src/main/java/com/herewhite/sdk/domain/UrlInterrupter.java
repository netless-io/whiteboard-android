package com.herewhite.sdk.domain;

/**
 * @deprecated 该接口已废弃。请使用 {@link com.herewhite.sdk.CommonCallback CommonCallback} 中的 {@link com.herewhite.sdk.CommonCallback#urlInterrupter(String)} 方法。
 */
public interface UrlInterrupter {
    String urlInterrupter(String sourceUrl);
}
