package com.herewhite.sdk;

import android.os.Build;
import android.os.Build.VERSION;

import com.google.gson.annotations.SerializedName;
import com.herewhite.sdk.domain.DeviceType;
import com.herewhite.sdk.domain.LoggerOptions;
import com.herewhite.sdk.domain.Region;
import com.herewhite.sdk.domain.WhiteObject;

import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by buhe on 2018/8/10.
 * 初始化 whiteSDK 用的配置参数，初始化成功，该类中的参数修改，不起作用
 */
public class WhiteSdkConfiguration extends WhiteObject {

    /**
     * sdk 笔画等教具的渲染的模式
     *
     * @since 2.8.0
     */
    public enum RenderEngineType {
        /**
         * 旧版本渲染，兼容性较好，性能较差
         * Android 6.1 ~ Android 8.1 有部分机器无法渲染 canvas 模式，只能用这个，默认 sdk 中会使用 svg 模式
         */
        @SerializedName("svg")
        svg,
        /**
         * 性能更好，兼容性略有问题，2.8.0；当前的默认渲染模式
         */
        @SerializedName("canvas")
        canvas,
    }

    /***
     * PptParams 单独传给动态 ppt 转换用的参数
     */
    public static class PptParams extends WhiteObject {

        public String getScheme() {
            return scheme;
        }

        /**
         * 更改动态 ppt 请求时的请求协议，可以将 https://www.exmaple.com/1.pptx 更改成 scheme://www.example.com/1.pptx
         * Android 端该方法无需使用
         * 文档中隐藏
         *
         * @param scheme
         */
        public void setScheme(String scheme) {
            this.scheme = scheme;
        }

        private String scheme;

        public boolean isUseServerWrap() {
            return useServerWrap;
        }

        /**
         * 2021-02-10 之后转换的动态 ppt 支持服务端排版功能，可以确保不同平台排版一致，目前默认关闭。
         * 如果打开，则不会在前端进行排版
         *
         * @param useServerWrap
         * @since 2.11.16
         */
        public void setUseServerWrap(boolean useServerWrap) {
            this.useServerWrap = useServerWrap;
        }

        private boolean useServerWrap;

        public PptParams(String scheme) {
            this.scheme = scheme;
        }
    }


    private Region region;
    // native 永远只接收 touch 事件
    private DeviceType deviceType = DeviceType.touch;
    // 在 webView 中，打印 native 的调用，并将得到的数据回传给 native 端
    private boolean log = false;
    private RenderEngineType renderEngine = RenderEngineType.canvas;
    private boolean enableInterrupterAPI = false;
    private boolean preloadDynamicPPT = false;
    private boolean routeBackup = false;
    private boolean userCursor = false;
    private boolean onlyCallbackRemoteStateModify = false;
    private boolean disableDeviceInputs = false;
    private boolean enableIFramePlugin = false;
    private boolean enableRtcIntercept = false;

    private LoggerOptions loggerOptions;

    private String appIdentifier;
    private HashMap<String, String> __nativeTags = new HashMap<>();
    /**
     * pptParams 动态 ppt 专用参数
     */
    private PptParams pptParams;
    private HashMap<String, String> fonts;

    public boolean isEnableIFramePlugin() {
        return enableIFramePlugin;
    }

    /**
     * 是否启用 iframe 插件，插件具体功能，见 https://github.com/netless-io/netless-iframe-bridge。
     * 默认不启用
     *
     * @param enableIFramePlugin
     */
    public void setEnableIFramePlugin(boolean enableIFramePlugin) {
        this.enableIFramePlugin = enableIFramePlugin;
    }

    public Region getRegion() {
        return region;
    }

    /**
     * 数据中心地区，需要与房间所在的数据中心一致，否则无法加入房间，会提示找不到房间
     *
     * @param region
     */
    public void setRegion(Region region) {
        this.region = region;
    }

    boolean isEnableRtcIntercept() {
        return enableRtcIntercept;
    }

    /**
     * 是否用 RTC 接管动态 PPT 音视频播放（声音），默认 false。
     * SDK 会根据 WhiteSdk 初始化时，是否传入 {@link AudioMixerBridge} 实现类，来自动配置该属性，无需开发者主动设置。
     * RTC 必须有 AudioMixerBridge 中需要的接口
     *
     * @param enableRtcIntercept
     * @since 2.9.17
     */
    void setEnableRtcIntercept(boolean enableRtcIntercept) {
        this.enableRtcIntercept = enableRtcIntercept;
    }

    public boolean isDisableDeviceInputs() {
        return disableDeviceInputs;
    }

    /**
     * FIXME: 该 API 会 {@link RoomParams#setDisableDeviceInputs(boolean)} 覆盖，需要删除。
     * 禁止教具输入，使用该功能后，终端客户无法使用教具书写内容
     *
     * @param disableDeviceInputs
     * @since 2.9.0
     * 文档中隐藏
     */
    public void setDisableDeviceInputs(boolean disableDeviceInputs) {
        this.disableDeviceInputs = disableDeviceInputs;
    }

    /**
     * 设置画笔的渲染引擎模式
     *
     * @param renderEngine 画笔教具的渲染模式，对于大量书写做了额外优化。2.9.0 默认切换至 canvas，之前版本为 svg。
     * @since 2.8.0
     */
    public void setRenderEngine(RenderEngineType renderEngine) {
        this.renderEngine = renderEngine;
    }

    public RenderEngineType getRenderEngine() {
        return renderEngine;
    }

    public PptParams getPptParams() {
        return pptParams;
    }

    /**
     * pptParams 动态 ppt 专用参数
     *
     * @param pptParams
     */
    public void setPptParams(PptParams pptParams) {
        this.pptParams = pptParams;
    }

    public HashMap<String, String> getFonts() {
        return fonts;
    }

    /**
     * 文档转网页（动态 PPT）时，自定义字体，为 key-value 结构，与 web 端一致
     *
     * @param fonts key 为字体名称，value 为字体网址的字典结构
     * @since 2.2.0
     */
    public void setFonts(HashMap<String, String> fonts) {
        this.fonts = fonts;
    }

    public boolean isPreloadDynamicPPT() {
        return preloadDynamicPPT;
    }

    /**
     * 动态 PPT 首页一次性加载选项
     * 在使用动态 PPT 的同时，加载动态 PPT 中所需要的图片资源，会导致在第一次加载页面时，出现大量加载。
     *
     * @param preloadDynamicPPT 默认关闭，不进行预加载
     *                          不推荐使用
     */
    public void setPreloadDynamicPPT(boolean preloadDynamicPPT) {
        this.preloadDynamicPPT = preloadDynamicPPT;
    }

    private void setupNativeTags() {
        __nativeTags.put("nativeVersion", WhiteSdk.Version());
        __nativeTags.put("platform", "android API " + Build.VERSION.SDK_INT);
    }

    /**
     * config 初始化方法
     *
     * @param appIdentifier 白板项目的 AppId
     * @param log           native 端 debug 用日志，是否开启日志回调。（仅限初始化 SDK，加入房间，开始回放 API）
     */
    public WhiteSdkConfiguration(String appIdentifier, boolean log) {
        this(appIdentifier);
        this.log = log;
    }

    /**
     * config 初始化方法
     *
     * @param appIdentifier
     */
    public WhiteSdkConfiguration(String appIdentifier) {
        this.appIdentifier = appIdentifier;
        if (VERSION.SDK_INT >= Build.VERSION_CODES.N && VERSION.SDK_INT < Build.VERSION_CODES.P) {
            renderEngine = RenderEngineType.svg;
        }
        setupNativeTags();
    }


    public LoggerOptions getLoggerOptions() {
        return loggerOptions;
    }

    /**
     * 日志上报系统设置项，有默认上报行为
     * 与 web 端一致
     *
     * @param loggerOptions {@link LoggerOptions}
     * @since 2.4.2
     */
    public void setLoggerOptions(LoggerOptions loggerOptions) {
        this.loggerOptions = loggerOptions;
    }

    public boolean isRouteBackup() {
        return routeBackup;
    }

    /**
     * 是否启用双路由功能，同时向两个网址请求数据，选择最快的应答。会造成一定的额外开销，默认关闭
     * 文档中隐藏
     *
     * @param routeBackup
     */
    public void setRouteBackup(boolean routeBackup) {
        this.routeBackup = routeBackup;
    }

    public DeviceType getDeviceType() {
        return deviceType;
    }

    /**
     * 显示用户头像
     * 需要保证对应用户在加入房间时，传入了 userPayload，并且 userPayload key-value 结构中，存在 avatar 字段
     *
     * @param userCursor 开关，默认关闭,即不显示用户头像
     */
    public void setUserCursor(boolean userCursor) {
        this.userCursor = userCursor;
    }

    public boolean isUserCursor() {
        return userCursor;
    }

    public boolean isOnlyCallbackRemoteStateModify() {
        return onlyCallbackRemoteStateModify;
    }

    /**
     * 只有非本地用户调用状态修改时，才会产生回调
     *
     * @param onlyCallbackRemoteStateModify
     */
    public void setOnlyCallbackRemoteStateModify(boolean onlyCallbackRemoteStateModify) {
        this.onlyCallbackRemoteStateModify = onlyCallbackRemoteStateModify;
    }

    /**
     * 文档中隐藏
     */
    public void setDeviceType(DeviceType deviceType) {
        this.deviceType = deviceType;
    }

    public boolean isLog() {
        return log;
    }

    /**
     * 打印 debug 日志
     *
     * @param log 默认关闭
     */
    public void setLog(boolean log) {
        this.log = log;
    }

    public boolean isEnableInterrupterAPI() {
        return enableInterrupterAPI;
    }

    /**
     * 设置图片替换 API，图片替换开关，默认关闭；如果开启，则可以在 {@link CommonCallbacks#urlInterrupter(String)} 中回调，并有修改该图片的地址内容
     *
     * @param enableInterrupterAPI
     */
    public void setEnableInterrupterAPI(boolean enableInterrupterAPI) {
        this.enableInterrupterAPI = enableInterrupterAPI;
    }

    public boolean isEnableImgErrorCallback() {
        return enableImgErrorCallback;
    }

    /**
     * 开启对图片加载失败事件的监听，默认关闭。开启后，所有 img 标签加载事件，都会在 {@link CommonCallbacks#onMessage(JSONObject)} 中回调
     *
     * @param enableImgErrorCallback
     */
    public void setEnableImgErrorCallback(boolean enableImgErrorCallback) {
        this.enableImgErrorCallback = enableImgErrorCallback;
    }

    private boolean enableImgErrorCallback;
}
