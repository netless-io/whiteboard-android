package com.herewhite.sdk;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Build;
import android.util.AttributeSet;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebViewClient;

import androidx.webkit.WebViewAssetLoader;

import com.herewhite.sdk.domain.AssetsHttpsOptions;
import com.herewhite.sdk.internal.WhiteWebViewClient;

import wendu.dsbridge.special.DWebView;
import wendu.dsbridge.special.OnReturnValue;

// white on 2018/8/10.


/**
 * `WhiteboardView` 类，用于配置白板界面。
 */
public class WhiteboardView extends DWebView implements JsBridgeInterface {
    private static String sEntryUrl = "file:///android_asset/whiteboard/index.html";

    private boolean autoResize = true;
    private RefreshViewSizeStrategy delayStrategy;
    private WhiteSdk whiteSdk;
    private WhiteWebViewClient whiteWebViewClient;
    private String entryUrl = sEntryUrl;
    private WhiteboardViewOptions options = new WhiteboardViewOptions();


    /**
     * 初始化白板界面。
     *
     * @param context 安卓活动 (Android Activity) 的上下文。
     */
    public WhiteboardView(Context context) {
        this(context, (WhiteboardViewOptions) null);
    }

    /**
     * 初始化白板界面。
     *
     * @param context 安卓活动 (Android Activity) 的上下文。
     * @param options 白板界面配置选项。
     */
    public WhiteboardView(Context context, WhiteboardViewOptions options) {
        super(getFixedContext(context));
        if (options != null) {
            this.options = options;
        }
        init();
    }

    /**
     * 初始化白板界面。
     *
     * @param context 安卓活动 (Android Activity) 的上下文。
     * @param attrs   自定义白板界面属性，[AttributeSet](https://developer.android.com/reference/android/util/AttributeSet).
     */
    public WhiteboardView(Context context, AttributeSet attrs) {
        super(getFixedContext(context), attrs);
        init();
    }

    /**
     * 兼容 API 文档中隐藏
     */
    public static Context getFixedContext(Context context) {
        if (Build.VERSION.SDK_INT >= 21 && Build.VERSION.SDK_INT < 23) {
            return context.createConfigurationContext(new Configuration());
        }
        return context;
    }
    /// @endcond

    /// @cond test
    @Override
    protected void onSizeChanged(int w, int h, int ow, int oh) {
        super.onSizeChanged(w, h, ow, oh);
        if (isInEditMode()) return;
        if (autoResize) {
            delayStrategy.refreshViewSize();
        }
    }

    /// @cond test
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        delayStrategy.onDetachedFromWindow();
    }
    /// @endcond


    /// @cond test

    /**
     * 设置视图大小切换时自动发送事件至js端
     * 文档中隐藏
     * @param autoResize
     */
    public void setAutoResize(boolean autoResize) {
        this.autoResize = autoResize;
    }
    /// @endcond

    public static void setEntryUrl(String entryUrl) {
        WhiteboardView.sEntryUrl = entryUrl;
    }

    private void init() {
        if (isInEditMode()) return;
        getSettings().setMediaPlaybackRequiresUserGesture(false);
        getSettings().setTextZoom(100);
        setWebChromeClient(new FixWebChromeClient());
        initWebViewClients();
        resolveAssetsOptions();

        loadUrl(entryUrl);

        // 100ms，减少用户体验问题，防止动画过程中频繁调用问题
        delayStrategy = new RefreshViewSizeStrategy(100);
    }

    private void resolveAssetsOptions() {
        if (!options.isEnableAssetsHttps()) {
            entryUrl = sEntryUrl;
            return;
        }

        AssetsHttpsOptions assetsHttpsOptions = options.getAssetsHttpsOptions();
        if (assetsHttpsOptions == null) {
            entryUrl = sEntryUrl;
            return;
        }

        entryUrl = assetsHttpsOptions.buildEntryUrl();
        WebViewAssetLoader assetLoader = new WebViewAssetLoader.Builder()
                .setDomain(assetsHttpsOptions.getDomain())
                .addPathHandler(assetsHttpsOptions.getAssetsPath(), new WebViewAssetLoader.AssetsPathHandler(getContext().getApplicationContext()))
                .build();
        whiteWebViewClient.setAssetLoader(assetLoader);
    }

    private void initWebViewClients() {
        whiteWebViewClient = new WhiteWebViewClient();
        super.setWebViewClient(whiteWebViewClient);
    }

    @Override
    public void setWebViewClient(WebViewClient client) {
        whiteWebViewClient.setDelegate(client);
    }

    @Override
    public <T> void callHandler(String method, Object[] args, OnReturnValue<T> handler) {
        super.callHandler(method, Utils.toBridgeMaps(args), handler);
    }

    @Override
    public void callHandler(String method, Object[] args) {
        this.callHandler(method, args, null);
    }

    @Override
    public <T> void callHandler(String method, OnReturnValue<T> handler) {
        this.callHandler(method, null, handler);
    }

    @Override
    public void callFocusView() {
        requestFocus();
    }

    class FixWebChromeClient extends WebChromeClient {
        @Override
        public Bitmap getDefaultVideoPoster() {
            try {
                int width = 100;
                int height = 50;
                // fix https://bugs.chromium.org/p/chromium/issues/detail?id=521753#c8
                return Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            } catch (Exception e) {
                return super.getDefaultVideoPoster();
            }
        }

        @Override
        public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
            if (whiteSdk != null) {
                whiteSdk.handleConsoleMessage(consoleMessage);
            }
            return super.onConsoleMessage(consoleMessage);
        }
    }

    private class RefreshViewSizeStrategy {
        private final int delay;
        private Runnable refreshViewSize = () -> callHandler("displayer.refreshViewSize", new Object[]{});

        RefreshViewSizeStrategy(int delay) {
            this.delay = delay;
        }

        public void refreshViewSize() {
            removeCallbacks(refreshViewSize);
            postDelayed(refreshViewSize, delay);
        }

        public void onDetachedFromWindow() {
            removeCallbacks(refreshViewSize);
        }
    }

    void attachSdk(WhiteSdk whiteSdk) {
        this.whiteSdk = whiteSdk;
    }
}
