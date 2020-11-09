package com.herewhite.sdk;

import android.os.Build;

import com.google.gson.annotations.SerializedName;
import com.herewhite.sdk.domain.DeviceType;
import com.herewhite.sdk.domain.LoggerOptions;
import com.herewhite.sdk.domain.WhiteObject;
import com.herewhite.sdk.domain.Region;
import android.os.Build.VERSION;
import java.util.HashMap;

/**
 * Created by buhe on 2018/8/10.
 */

public class WhiteSdkConfiguration extends WhiteObject {

    public enum RenderEngineType {
        @SerializedName("svg")
        svg,
        @SerializedName("canvas")
        canvas,
    }

    public static class PptParams extends WhiteObject {
        public String getScheme() {
            return scheme;
        }

        public void setScheme(String scheme) {
            this.scheme = scheme;
        }

        private String scheme;
        public PptParams(String scheme) {
            this.scheme = scheme;
        }
    }

    // native 永远只接收 touch 事件
    private DeviceType deviceType = DeviceType.touch;
    // 在 webView 中，打印日志，并回调给 native 端
    private boolean log = false;
    private RenderEngineType renderEngine = RenderEngineType.canvas;
    private boolean enableInterrupterAPI = false;
    private boolean preloadDynamicPPT = false;
    private boolean routeBackup = false;
    private boolean userCursor = false;
    private boolean onlyCallbackRemoteStateModify = false;
    private boolean disableDeviceInputs = false;

    public boolean isEnableIFramePlugin() {
        return enableIFramePlugin;
    }

    public void setEnableIFramePlugin(boolean enableIFramePlugin) {
        this.enableIFramePlugin = enableIFramePlugin;
    }

    private boolean enableIFramePlugin = false;

    public Region getRegion() {
        return region;
    }

    public void setRegion(Region region) {
        this.region = region;
    }

    private Region region;

    boolean isEnableRtcIntercept() {
        return enableRtcIntercept;
    }

    /**
     * 是否使用 rtc 接管动态 PPT 音视频播放（声音），默认 false。
     * SDK 会根据 WhiteSdk 初始化时，是否传入 {@link AudioMixerBridge} 实现类，来自动配置该属性，无需开发者主动设置。
     * @param enableRtcIntercept
     */
    void setEnableRtcIntercept(boolean enableRtcIntercept) {
        this.enableRtcIntercept = enableRtcIntercept;
    }

    private boolean enableRtcIntercept = false;

    public boolean isDisableDeviceInputs() {
        return disableDeviceInputs;
    }

    /**
     * 禁止教具输入
     * @param disableDeviceInputs
     * @since 2.9.0
     */
    public void setDisableDeviceInputs(boolean disableDeviceInputs) {
        this.disableDeviceInputs = disableDeviceInputs;
    }

    private LoggerOptions loggerOptions;

    private String appIdentifier;
    private HashMap<String, String> __nativeTags = new HashMap<>();
    /** pptParams 动态 ppt 专用参数 */
    private PptParams pptParams;
    private HashMap<String, String> fonts;

    /**
     * 设置画笔的渲染引擎模式
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

    public void setPptParams(PptParams pptParams) {
        this.pptParams = pptParams;
    }

    public HashMap<String, String> getFonts() {
        return fonts;
    }

    /**
     * 文档转网页（动态 PPT）时，自定义字体，为 key-value 结构
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
     * 动态 PPT 预加载选项
     *
     * 在使用动态 PPT 的同时，加载动态 PPT 中所需要的图片资源，会导致在第一次加载页面时，出现大量加载。
     *
     * @param preloadDynamicPPT 默认关闭，不进行预加载
     */
    public void setPreloadDynamicPPT(boolean preloadDynamicPPT) {
        this.preloadDynamicPPT = preloadDynamicPPT;
    }

    private void setupNativeTags() {
        __nativeTags.put("nativeVersion", WhiteSdk.Version());
        __nativeTags.put("platform", "android API " + Build.VERSION.SDK_INT);
    }

    public WhiteSdkConfiguration(String appIdentifier, boolean log) {
        this(appIdentifier);
        this.log = log;
    }

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
     * 是否启用双路由功能，同时像两个网址请求数据，选择最快的应答。会造成一定的额外开销。默认关闭
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
    public void setUserCursor(boolean userCursor) { this.userCursor = userCursor; }

    public boolean isUserCursor() { return userCursor; }

    public boolean isOnlyCallbackRemoteStateModify() {
        return onlyCallbackRemoteStateModify;
    }

    public void setOnlyCallbackRemoteStateModify(boolean onlyCallbackRemoteStateModify) {
        this.onlyCallbackRemoteStateModify = onlyCallbackRemoteStateModify;
    }

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
     * 设置图片替换 API
     *
     * @param enableInterrupterAPI 图片替换开关，默认关闭
     */
    public void setEnableInterrupterAPI(boolean enableInterrupterAPI) {
        this.enableInterrupterAPI = enableInterrupterAPI;
    }
}
