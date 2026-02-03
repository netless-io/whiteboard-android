package com.herewhite.sdk.internal;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.Nullable;

import com.herewhite.sdk.CommonCallback;
import com.herewhite.sdk.JsBridgeInterface;

import java.lang.ref.WeakReference;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * WebView JavaScript 健康检查工具
 * 用于检测 JavaScript 运行时和 JS-Native 桥接是否正常工作
 */
public final class WebViewJsHealthCheck {
    private static final Handler MAIN_HANDLER = new Handler(Looper.getMainLooper());
    private static final String JS_BRIDGE_TIMEOUT_ERROR = "JavaScript bridge callback not received. " +
            "JS runtime may not be ready or WebView is in a broken state.";
    private static final long TIMEOUT_MS = 1500;

    /**
     * 检测 WebView 的 JS 运行时健康状态
     * <p>
     * 该方法会尝试通过 JS -> Native 桥接通信验证 JavaScript 是否正常工作。
     * 如果在超时时间内没有收到回调，会通过 CommonCallback.throwError 通知应用层。
     * <p>
     * 注意：
     * - 该方法侵入性很小，只会在检测到问题时才通知应用层
     * - 不会干扰正常的业务流程
     * - 所有异常处理都在内部完成
     * - 使用 WeakReference 避免内存泄漏
     *
     * @param bridge         JsBridgeInterface 实例
     * @param commonCallback CommonCallback 实例，用于在检测到问题时通知应用层，可以为 null
     */
    public static void checkOnce(@Nullable JsBridgeInterface bridge, @Nullable CommonCallback commonCallback) {
        if (bridge == null) {
            // WebView / JS bridge not available, skip health check
            return;
        }

        // 使用 WeakReference 避免内存泄漏
        final WeakReference<CommonCallback> callbackRef = new WeakReference<>(commonCallback);
        final AtomicBoolean handled = new AtomicBoolean(false);

        try {
            // 调用 JS 方法并等待回调
            bridge.callHandler("sdk.nativeLog", new Object[]{new String[]{"js-health-probe"}}, retValue -> {
                if (handled.compareAndSet(false, true)) {
                    Logger.info("[JsHealthCheck] JS bridge callback received");
                }
            });

            // 超时检测
            MAIN_HANDLER.postDelayed(() -> {
                if (handled.compareAndSet(false, true)) {
                    Logger.error(JS_BRIDGE_TIMEOUT_ERROR, null);

                    CommonCallback callback = callbackRef.get();
                    if (callback == null) return;

                    try {
                        callback.throwError(JS_BRIDGE_TIMEOUT_ERROR);
                    } catch (Exception e) {
                        Logger.error("Exception in CommonCallback.throwError: " + e.getMessage(), e);
                    }
                }
            }, TIMEOUT_MS);

        } catch (Exception e) {
            Logger.error("Exception during JS health check: " + e.getMessage(), e);
        }
    }

    private WebViewJsHealthCheck() {
    }
}

