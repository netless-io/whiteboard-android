package com.herewhite.sdk.window;

import com.herewhite.sdk.ResultCaller;
import com.herewhite.sdk.WhiteSdkConfiguration;

public interface SlideListener {
    /**
     * 拦截 Slide 资源 URL 回调。
     *
     * @since 2.16.52
     *
     * 该回调默认禁用。你可以在初始化白板 SDK 时，通过 {@link WhiteSdkConfiguration#setEnableSlideInterrupterAPI} setEnableSlideInterrupterAPI}(true) 方法开启。
     *
     * 开启后，在 Slide 资源加载时，SDK 会触发该回调，报告资源的原 URL 地址。你需要将替换后的 URL 通过 resultCaller 回调到 SDK。
     *
     * @note 不要在此方法中作过多耗时操作。
     *
     * @param sourceUrl 原 URL 地址。
     * @param resultCaller 回调替换后的 url。
     */
    void slideUrlInterrupter(String sourceUrl, ResultCaller<String> resultCaller);
}
