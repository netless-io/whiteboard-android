package com.herewhite.sdk.window;

import com.herewhite.sdk.ResultCaller;
import com.herewhite.sdk.WhiteSdkConfiguration;
import com.herewhite.sdk.domain.SlideErrorType;

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
    default void slideUrlInterrupter(String sourceUrl, ResultCaller<String> resultCaller) {
        resultCaller.call(sourceUrl);
    }

    /**
     * Slide 加载错误回调。
     *
     * @since 2.16.93
     *
     * @param errorType 错误类型。
     * @param errorMsg  错误信息。
     * @param slideId   Slide ID。
     * @param slideIndex Slide 索引。
     */
    default void onSlideError(SlideErrorType errorType, String errorMsg, String slideId, int slideIndex) {
    }

    default void slideOpenUrl(String url) {

    }

    /**
     * Slide 资源加载重试次数耗尽回调。
     *
     * <p>当 Slide 资源加载失败并且重试次数耗尽时，SDK 会触发该回调。
     *
     * @param url      加载失败的资源 URL。
     * @param message  错误信息。
     */
    default void onSlideResourceMaxRetries(String url, String message) {
    }

    /**
     * Slide 页面状态变化回调。
     *
     * <p>当多窗口 SlideApp 页面索引或页面数量变化时触发。页码从 1 开始。
     *
     * @param appId SlideApp 的窗口 ID。
     * @param page 当前页码，从 1 开始。
     * @param pageCount 页面总数。
     */
    default void onSlidePageStateChanged(String appId, int page, int pageCount) {
    }
}
