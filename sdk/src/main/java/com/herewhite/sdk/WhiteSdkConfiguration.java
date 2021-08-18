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
 * `WhiteSdk` 实例的配置。
 *
 * @note 成功初始化 `WhiteSdk` 后，无法再调用 `WhiteSdkConfiguration` 类中的任何方法修改 `WhiteSdk` 的配置。
 */
public class WhiteSdkConfiguration extends WhiteObject {

    /**
     * 白板上绘画的渲染模式。
     *
     * @since 2.8.0
     */
    public enum RenderEngineType {
        /**
         * SVG 渲染模式。
         * 2.8.0 及之前版本的互动白板 SDK 默认使用的渲染模式，该模式兼容性较好，但性能较差。
         */
        @SerializedName("svg")
        svg,
        /**
         * Canvas 渲染模式。
         * <p>
         * 2.8.0 版本起新增 `canvas` 渲染模式，该模式性能较好，但兼容性较差。
         * 2.9.0 及之后版本的互动白板 SDK 默认使用 `canvas` 渲染模式。
         *
         * @note 由于部分 Android 6.1 至 Android 8.1 设备无法支持 `canvas` 渲染模式，SDK 会自动将默认的渲染模式切换为 `svg` 渲染模式。
         */
        @SerializedName("canvas")
        canvas,
    }

    /**
     * `PptParams` 类，用于设置动态 PPT 参数。
     */
    public static class PptParams extends WhiteObject {
        /// @cond test
        /** 文档中隐藏 */
        public String getScheme() {
            return scheme;
        }
        /// @endcond

        /// @cond test
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
        /// @endcond

        private String scheme;

        /**
         * 获取是否开启动态 PPT 服务端排版功能。
         *
         * @return 动态 PPT 服务端排版功能的开启状态：
         * - `true`：开启。
         * - `false`：关闭。
         */
        public boolean isUseServerWrap() {
            return useServerWrap;
        }

        /**
         * 开启/关闭动态 PPT 服务端排版功能。
         *
         * @since 2.11.16
         *
         * 自 2021 年 2 月 10 日起，将 PPTX 文件转换为 HTML 网页时，Agora Interactive 白板服务端支持对 PPTX 文件进行排版，以确保 PPTX 文件的文本在各个平台上的呈现保持一致。
         *
         * @note Note: 自 2.12.27 版本起，`useServerWrap` 的默认值由 `false` 改为 `true`。
         *
         * @param useServerWrap 是否开启服务端排版功能：
         * - `true`：（默认）开启。
         * - `false`：关闭。
         */
        public void setUseServerWrap(boolean useServerWrap) {
            this.useServerWrap = useServerWrap;
        }

        private boolean useServerWrap = true;

        /// @cond test
        /** 文档中隐藏 */
        public PptParams(String scheme) {
            this.scheme = scheme;
        }
        /// @endcond

        /// @cond test
        public PptParams() {

        }
        /// @endcond
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
    private boolean enableSyncedStore = true;

    private LoggerOptions loggerOptions;

    private String appIdentifier;
    private HashMap<String, String> __nativeTags = new HashMap<>();
    /**
     * pptParams 动态 ppt 专用参数
     */
    private PptParams pptParams = new PptParams();
    private HashMap<String, String> fonts;

    /**
     * 获取是否启用 iframe 插件。
     *
     * @return 是否启用 iframe 插件：
     * - `true`：开启。
     * - `false`：未启用。
     */
    public boolean isEnableIFramePlugin() {
        return enableIFramePlugin;
    }

    /**
     * 启用/禁用 iframe 插件。
     *
     * iframe 插件的功能，详见 https://github.com/netless-io/netless-iframe-bridge。
     *
     * @param enableIFramePlugin 是否启用 iframe 插件：
     *                           - `true`：开启。
     *                           - `false`：（默认）不启用。
     */
    public void setEnableIFramePlugin(boolean enableIFramePlugin) {
        this.enableIFramePlugin = enableIFramePlugin;
    }

    /**
     * 获取设置的数据中心。
     *
     * @return 数据中心，详见 {@link com.herewhite.sdk.domain.Region Region}。
     */
    public Region getRegion() {
        return region;
    }

    /**
     * 设置数据中心。
     *
     * @note 该方法设置的数据中心必须与要加入的互动白板实时房间所在数据中心一致；否则，SDK 将无法连接到房间 。
     *
     * @param region 数据中心，详见 {@link com.herewhite.sdk.domain.Region Region}。
     */
    public void setRegion(Region region) {
        this.region = region;
    }

    boolean isEnableRtcIntercept() {
        return enableRtcIntercept;
    }

    /// @cond test
    /**
     * 设置是否启用 RTC SDK 的混音方法播放动态 PPT 中的音频。
     *
     * @since 2.9.17
     * <p>
     * 当同时使用 Agora RTC SDK 和互动白板 SDK, 且互动白板中展示的动态 PPT 中包含音频文件时，你可以调用 RTC SDK 的混音方法播放动态 PPT 中的音频，以保证音频正常播放。
     *
     * @note 初始化 `WhiteSdk` 时，如果你实现并传入 {@link AudioMixerBridge AudioMixerBridge} 类，SDK 会自动设置 `setEnableRtcIntercept(true)`。你无需主动调用该方法。
     *
     * @param enableRtcIntercept 是否启用 RTC SDK 的混音方法播放动态 PPT 中的音频：
     *                           - `true`：启用。
     *                           - `false`：（默认）不启用。
     *
     * 可以在文档中隐藏
     */
    void setEnableRtcIntercept(boolean enableRtcIntercept) {
        this.enableRtcIntercept = enableRtcIntercept;
    }
    /// @endcond

    /// @cond test
    /**
     * 文档中隐藏
     * @return
     */
    public boolean isDisableDeviceInputs() {
        return disableDeviceInputs;
    }
    /// @endcond

    /// @cond test
    /**
     * FIXME: 该 API 会 {@link RoomParams#setDisableDeviceInputs(boolean)} 覆盖，需要删除。
     * 禁止白板工具输入，使用该功能后，终端客户无法使用白板工具书写内容
     *
     * @param disableDeviceInputs
     * @since 2.9.0
     * 文档中隐藏
     */
    public void setDisableDeviceInputs(boolean disableDeviceInputs) {
        this.disableDeviceInputs = disableDeviceInputs;
    }
    /// @endcond

    /**
     * 设置绘图的渲染模式。
     *
     * @since 2.8.0
     * <p>
     * 为优化白板上绘图的渲染，自 2.8.0 版本起，白板 SDK 新增 `canvas` 渲染模式，并从 2.9.0 版本起，将 `canvas` 渲染模式作为默认的渲染模式。
     *
     * @note 由于部分 Android 6.1 至 Android 8.1 设备无法支持 `canvas` 渲染模式，SDK 会自动将默认的渲染模式切换为 `svg` 渲染模式。
     *
     * @param renderEngine 绘图的渲染模式，详见 {@link RenderEngineType}。
     */
    public void setRenderEngine(RenderEngineType renderEngine) {
        this.renderEngine = renderEngine;
    }

    /**
     * 获取绘图的渲染模式。
     *
     * @return 绘图的渲染模式，详见 {@link RenderEngineType}。
     */
    public RenderEngineType getRenderEngine() {
        return renderEngine;
    }

    /**
     * 获取设置的动态 PPT 参数。
     *
     * @return 设置的动态 PPT 参数，详见 {@link PptParams PptParams}。
     */
    public PptParams getPptParams() {
        return pptParams;
    }

    /**
     * 设置动态 PPT 参数。
     *
     * @param pptParams 动态 PPT 参数，详见 {@link PptParams PptParams}。
     */
    public void setPptParams(PptParams pptParams) {
        this.pptParams = pptParams;
    }

    /**
     * 获取自定义字体。
     *
     * @return 自定义字体名称和地址。
     */
    public HashMap<String, String> getFonts() {
        return fonts;
    }

    /**
     * 设置自定义字体。
     *
     * @since 2.2.0
     * <p>
     * 为正常显示动态 PPT 中的非常规字体，在初始化 `WhiteSdk` 实例时，你可以调用该方法传入该字体文件的 URL 地址。
     *
     * @note 调用该方法前，你需要将字体文件上传至你的 app 服务器或第三方云存储，并生成一个 URL 地址。
     *
     * @param fonts 自定义字体，为 `key-value` 键值对，`key` 为字体名称，`value` 为字体的 URL 地址，例如 `"Calibri", "https://your-cdn.com/Calibri.ttf"`。
     */
    public void setFonts(HashMap<String, String> fonts) {
        this.fonts = fonts;
    }

    /**
     * 获取是否开启一次性加载动态 PPT 中的所有图片资源。
     *
     * @return 一次性加载动态 PPT 中的所有图片资源开启状态：
     * - `true`：开启。
     * - `false`: 未开启。
     */
    public boolean isPreloadDynamicPPT() {
        return preloadDynamicPPT;
    }

    /**
     * 设置是否在加载动态 PPT 首页时，一次性加载动态 PPT 中的所有图片资源。
     *
     * @note
     * Agora 不推荐设置 `setPreloadDynamicPPT(true)`，这样会使 PPT 显示缓慢。
     *
     * @param preloadDynamicPPT 是否在加载动态 PPT 首页时，一次性加载动态 PPT 中的所有图片资源：
     *                          - `true`：一次性加载所有动态 PPT。
     *                          - `false`: （默认）不一次性加载所有动态 PPT。
     */
    public void setPreloadDynamicPPT(boolean preloadDynamicPPT) {
        this.preloadDynamicPPT = preloadDynamicPPT;
    }

    private void setupNativeTags() {
        __nativeTags.put("nativeVersion", WhiteSdk.Version());
        __nativeTags.put("platform", "android API " + Build.VERSION.SDK_INT);
    }

    /**
     * 初始化互动白板 SDK 配置。
     *
     *
     *
     * @param appIdentifier 白板项目的唯一标识。详见[获取白板项目的 App Identifier](https://docs.agora.io/cn/whiteboard/enable_whiteboard?platform=Android#获取-app-identifier)。
     * @param log           是否开启调试日志回调：
     *                      - `true`：开启。
     *                      - `false`：（默认）关闭。
     * 调试日志仅包含调用初始化互动白板 SDK、加入房间和开始回放等方法的回调。
     */
    public WhiteSdkConfiguration(String appIdentifier, boolean log) {
        this(appIdentifier);
        this.log = log;
    }

    /**
     * 初始化互动白板 SDK 配置。
     *
     * @param appIdentifier 白板项目的唯一标识。详见[获取白板项目的 App Identifier](https://docs.agora.io/cn/whiteboard/enable_whiteboard?platform=Android#获取-app-identifier)。
     */
    public WhiteSdkConfiguration(String appIdentifier) {
        this.appIdentifier = appIdentifier;
        if (VERSION.SDK_INT >= Build.VERSION_CODES.N && VERSION.SDK_INT < Build.VERSION_CODES.P) {
            renderEngine = RenderEngineType.svg;
        }
        setupNativeTags();
    }


    /**
     * 获取设置的日志选项。
     *
     * @return 设置的日志选项，详见 {@link com.herewhite.sdk.domain.LoggerOptions LoggerOptions}。
     */
    public LoggerOptions getLoggerOptions() {
        return loggerOptions;
    }

    /**
     * 设置日志选项。
     *
     * @since 2.4.2
     *
     * @param loggerOptions 日志选择，详见 {@link com.herewhite.sdk.domain.LoggerOptions LoggerOptions}。
     */
    public void setLoggerOptions(LoggerOptions loggerOptions) {
        this.loggerOptions = loggerOptions;
    }

    /// @cond test
    /**
     * 文档中隐藏
     *
     * @return
     */
    public boolean isRouteBackup() {
        return routeBackup;
    }
    /// @endcond

    /// @cond test
    /**
     * 是否启用双路由功能，同时向两个网址请求数据，选择最快的应答。会造成一定的额外开销，默认关闭
     * 文档中隐藏
     *
     * @param routeBackup
     */
    public void setRouteBackup(boolean routeBackup) {
        this.routeBackup = routeBackup;
    }
    /// @endcond

    /// @cond test
    /**
     * 文档中隐藏
     * @return
     */
    public DeviceType getDeviceType() {
        return deviceType;
    }
    /// @endcond

    /**
     * 设置是否显示用户头像。
     * <p>
     * 要显示用户头像，请确保你在 `userPayload` 对象中传入了头像的键值对，
     * 并在调用 {@link com.herewhite.sdk.RoomParams#setUserPayload(Object userPayload) setUserPayload}。
     *
     * @param userCursor 是否显示用户头像：
     *                   - `true`：显示。
     *                   - `false`：（默认）不显示。
     */
    public void setUserCursor(boolean userCursor) {
        this.userCursor = userCursor;
    }

    /**
     * 获取是否显示用户头像。
     *
     * @return 是否显示用户头像：
     * - `true`：显示。
     * - `false`：不显示。
     */
    public boolean isUserCursor() {
        return userCursor;
    }

    /**
     * 获取是否开启仅接收远端用户状态改变的回调。
     *
     * @return 是否开启仅接收远端用户状态改变回调：
     * - `true`：开启。开启该功能后，本地用户仅会接收到远端用户状态改变的回调，自身状态的改变不会触发回调。
     * - `false`：关闭。关闭该功能后，本地用户会接收到远端用户状态改变的回调，也会接收到自身状态改变的回调。
     */
    public boolean isOnlyCallbackRemoteStateModify() {
        return onlyCallbackRemoteStateModify;
    }

    /**
     * 开启/关闭仅接收远端用户状态改变的回调。
     * <p>
     * 开启该功能后，本地用户仅会接收到远端用户状态改变的回调，自身状态的改变不会触发回调；关闭该功能后，本地用户会接收到远端用户状态改变的回调，也会接收到自身状态改变的回调。
     *
     * @param onlyCallbackRemoteStateModify 是否开启仅接收远端用户状态改变回调：
     * - `true`：开启。
     * - `false`：（默认）关闭。
     */
    public void setOnlyCallbackRemoteStateModify(boolean onlyCallbackRemoteStateModify) {
        this.onlyCallbackRemoteStateModify = onlyCallbackRemoteStateModify;
    }

    /// @cond test
    /**
     * 文档中隐藏
     */
    public void setDeviceType(DeviceType deviceType) {
        this.deviceType = deviceType;
    }
    /// @endcond

    /**
     * 获取是否开启调试日志打印。
     *
     * @return 是否开启调试日志打印：
     * - `true`：开启。
     * - `false`：关闭。
     */
    public boolean isLog() {
        return log;
    }

    /**
     * 开启/关闭调试日志打印。
     *
     * 调试日志仅包含调用初始化互动白板 SDK、加入房间和开始回放等方法的回调。
     *
     * @param log 是否开启调试日志打印：
     *            - `true`：开启。
     *            - `false`：（默认）关闭。
     */
    public void setLog(boolean log) {
        this.log = log;
    }

    /**
     * 获取是否开启图片拦截和替换功能。
     *
     * @return 是否开启图片拦截和替换功能：
     * - `true`：开启。
     * - `false`：关闭。
     */
    public boolean isEnableInterrupterAPI() {
        return enableInterrupterAPI;
    }

    /**
     * 开启/关闭图片拦截替换功能。
     * <p>
     * 该方法可以开启或关闭图片拦截功能。
     * 如果开启，在图片实际插入白板前，SDK 会拦截图片并触发 {@link CommonCallback#urlInterrupter(String) urlInterrupter} 回调，你可以在该回调中替换图片的地址。
     *
     * @note Agora 建议不要开启图片拦截功能，否则会频繁触发回调。
     *
     * @param enableInterrupterAPI 是否开启图片拦截和替换功能：
     *                             - `true`：开启。
     *                             - `false`：（默认）关闭。
     */
    public void setEnableInterrupterAPI(boolean enableInterrupterAPI) {
        this.enableInterrupterAPI = enableInterrupterAPI;
    }

    /**
     * 获取是否开启对图片加载失败事件的监听。
     *
     * @return 是否监听图片加载失败事件：
     * - `true`：开启监听。
     * - `false`：关闭监听。
     */
    public boolean isEnableImgErrorCallback() {
        return enableImgErrorCallback;
    }

    /**
     * 开启/关闭对图片加载失败事件的监听。
     *
     * @param enableImgErrorCallback 是否开启对图片加载失败事件的监听：
     * - `true`：开启。开启后，所有 `img` 标签加载事件都会在 {@link CommonCallback#onMessage(JSONObject) onMessage} 中回调。
     * - `false`：（默认）关闭。
     */
    public void setEnableImgErrorCallback(boolean enableImgErrorCallback) {
        this.enableImgErrorCallback = enableImgErrorCallback;
    }

    private boolean enableImgErrorCallback;
}
