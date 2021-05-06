package com.herewhite.sdk;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Build;
import android.util.AttributeSet;
import android.webkit.WebChromeClient;

import wendu.dsbridge.DWebView;
import wendu.dsbridge.OnReturnValue;

/**
 * white on 2018/8/10.
 */

public class WhiteboardView extends DWebView implements JsBridgeInterface {

    private boolean autoResize = true;
    private RefreshViewSizeStrategy delayStrategy;

    /**
     * 初始化白板界面。
     *
     * @param context 安卓活动 (Android Activity) 的上下文。
     */
    public WhiteboardView(Context context) {
        super(getFixedContext(context));
        init();
    }

    /**
     * 初始化白板界面。
     *
     * @param context 安卓活动 (Android Activity) 的上下文。
     * @param attrs   自定义控件属性，详见 Android 文档。
     */
    public WhiteboardView(Context context, AttributeSet attrs) {
        super(getFixedContext(context), attrs);
        init();
    }

    @Override
    protected void onSizeChanged(int w, int h, int ow, int oh) {
        super.onSizeChanged(w, h, ow, oh);
        if (autoResize) {
            delayStrategy.refreshViewSize();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        delayStrategy.onDetachedFromWindow();
    }

    /**
     * 设置视图大小切换时自动发送事件至js端
     *
     * @param autoResize
     */
    public void setAutoResize(boolean autoResize) {
        this.autoResize = autoResize;
    }


    /**
     * 兼容 API 文档中隐藏
     */
    public static Context getFixedContext(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return context.createConfigurationContext(new Configuration());
        } else {
            return context;
        }
    }

    private void init() {
        getSettings().setMediaPlaybackRequiresUserGesture(false);
        loadUrl("file:///android_asset/whiteboard/index.html");
        setWebChromeClient(new FixWebChromeClient());
        // 100ms，减少用户体验问题，防止动画过程中频繁调用问题
        delayStrategy = new RefreshViewSizeStrategy(100);
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
}
