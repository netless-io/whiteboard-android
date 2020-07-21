package com.herewhite.sdk.domain;

/**
 * @deprecated 请使用 {@link com.herewhite.sdk.CommonCallbacks} 实现 {@link com.herewhite.sdk.CommonCallbacks#urlInterrupter(String)}
 */
public interface UrlInterrupter {
    String urlInterrupter(String sourceUrl);
}
