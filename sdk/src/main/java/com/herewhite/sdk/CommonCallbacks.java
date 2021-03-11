package com.herewhite.sdk;

import com.herewhite.sdk.domain.SDKError;

import org.json.JSONObject;

/**
 * 部分通用回调，不管是回放房间，还是实时房间，都有该部分通知
 * @since 2.9.13
 */
public interface CommonCallbacks {

    /**
     *  当sdk出现未捕获的全局错误时，会在此处抛出
     * @param args
     */
    void throwError(Object args);

    /**
     * 图片替换 API
     * 开启：需要同时在初始化 SDK 时，配置 WhiteSdkConfiguration 实例 enableInterrupterAPI 属性 为 YES；注意：中途无法更改。
     * 启用后，插入图片API/插入scene 时，均会回调该 API。
     * 由于该 API 存在性能问题（调用过于频繁），我们不推荐使用；Android 端，可以使用 WebView 的拦截功能进行拦截
     *
     *
     @param sourceUrl 原始图片地址
     @return 替换后的图片地址
     @since 2.9.14
     */
    String urlInterrupter(String sourceUrl);
    /**
     * 动态 ppt 中的音视频媒体，播放通知
     * @since 2.9.13
     */
    void onPPTMediaPlay();

    /**
     * 动态 ppt 中的音视频媒体，暂停通知
     * @since 2.9.13
     */
    void onPPTMediaPause();

    /**
     * 部分自定义消息接口，用于本地客户端与 bridge 网页的内容的一些交互，这些信息，不是所有端都会收到。
     * 目前存在的事件：
     * 1. iframe 透传信息; 数据格式由 iframe 协商定义，直接透传
     *      限制必须为 字典格式，同时必须存在 name 字段，且值必须为 "iframe"
     * 2. 图片加载失败信息（必须先在初始化先开启），格式：
     *     {name: "imageLoadError",
     *      customMessage: true,
     *      src: 图片地址}
     * 3. ppt 播放，暂停信息（该事件，可以直接监听 pptMediaPlay 和 pptMediaPause，此处不提供具体格式）
     * @param object 内容格式会根据情况发送。发送时，均为 JSON 格式
     * @since 2.11.4
     */
    void onMessage(JSONObject object);

    /**
     * 初始化 SDK 时，会根据传入的 App Identifier 向服务器配置信息（最多尝试三次）
     * 如果失败，SDK 处于不可用状态，调用加入房间/回放房间会处于一直无响应状态，需要开发者重新初始化 SDK。
     * 一般触发情况：
     * 1. 初始化 SDK 时候，网络异常，导致获取配置信息失败；
     * 2. 传入了错误不合法的 App Identifier
     * @since 2.9.14
     */
    void sdkSetupFail(SDKError error);
}
