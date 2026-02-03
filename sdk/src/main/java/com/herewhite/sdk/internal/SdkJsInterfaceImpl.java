package com.herewhite.sdk.internal;

import android.webkit.JavascriptInterface;

import androidx.annotation.Nullable;

import com.herewhite.sdk.CommonCallback;
import com.herewhite.sdk.domain.SDKError;
import com.herewhite.sdk.domain.SlideErrorType;
import com.herewhite.sdk.domain.UrlInterrupter;
import com.herewhite.sdk.window.SlideListener;

import org.json.JSONObject;

import wendu.dsbridge.special.CompletionHandler;

public class SdkJsInterfaceImpl {

    @Nullable
    private CommonCallback commonCallback;
    @Nullable
    private UrlInterrupter urlInterrupter;
    @Nullable
    private SlideListener slideListener;

    @Nullable
    private PostMessageCallback postMessageCallback;

    public SdkJsInterfaceImpl(CommonCallback commonCallback) {
        this.commonCallback = commonCallback;
    }

    public void setCommonCallbacks(@Nullable CommonCallback commonCallbacks) {
        this.commonCallback = commonCallbacks;
    }

    @Nullable
    public CommonCallback getCommonCallback() {
        return commonCallback;
    }

    public void setPostMessageCallback(PostMessageCallback postMessageCallback) {
        this.postMessageCallback = postMessageCallback;
    }

    @Nullable
    public UrlInterrupter getUrlInterrupter() {
        return urlInterrupter;
    }

    public void setUrlInterrupter(@Nullable UrlInterrupter urlInterrupter) {
        this.urlInterrupter = urlInterrupter;
    }

    public void setSlideListener(SlideListener slideListener) {
        this.slideListener = slideListener;
    }

    @JavascriptInterface
    public String urlInterrupter(Object args) {
        if (commonCallback != null) {
            return commonCallback.urlInterrupter(String.valueOf(args));
        } else if (urlInterrupter == null) {
            return String.valueOf(args);
        }
        return urlInterrupter.urlInterrupter(String.valueOf(args));
    }

    @JavascriptInterface
    public void slideUrlInterrupter(Object args, CompletionHandler<String> handler) {
        String url = String.valueOf(args);
        if (slideListener != null) {
            slideListener.slideUrlInterrupter(url, handler::complete);
        } else {
            handler.complete(url);
        }
    }

    @JavascriptInterface
    public void slideOpenUrl(Object args) {
        String url = String.valueOf(args);
        if (slideListener != null) {
            slideListener.slideOpenUrl(url);
        }
    }

    @JavascriptInterface
    public void throwError(Object args) {
        Logger.info("WhiteSDK throwError: " + args);
        if (commonCallback != null) {
            commonCallback.throwError(args);
        }
    }

    @JavascriptInterface
    public void logger(Object args) {
        Logger.info("WhiteSDK logger: " + args);
        JSONObject jsonObject = convertToJsonOrNull(args);
        if (commonCallback != null && jsonObject != null) {
            commonCallback.onLogger(jsonObject);
        }
    }

    @JavascriptInterface
    public void postMessage(Object args) {
        Logger.info("WhiteSDK postMessage: " + args);
        JSONObject jsonObject = convertToJsonOrNull(args);
        if (commonCallback != null && jsonObject != null) {
            commonCallback.onMessage(jsonObject);
        }

        if (postMessageCallback != null && jsonObject != null) {
            postMessageCallback.onMessage(jsonObject);
        }

        if (slideListener != null && jsonObject != null) {
            handleSlideEvent(jsonObject);
        }
    }

    private void handleSlideEvent(JSONObject jsonObject) {
        String type = jsonObject.optString("type");
        if ("@slide/_error_".equals(type)) {
            String errorType = jsonObject.optString("errorType");
            String errorMsg = jsonObject.optString("errorMsg");
            String slideId = jsonObject.optString("slideId");
            int slideIndex = jsonObject.optInt("slideIndex");
            if (slideListener != null) {
                slideListener.onSlideError(convertToSlideErrorType(errorType), errorMsg, slideId, slideIndex);
            }
        }
    }

    private SlideErrorType convertToSlideErrorType(String errorType) {
        if ("RESOURCE_ERROR".equals(errorType)) {
            return SlideErrorType.RESOURCE_ERROR;
        } else if ("RUNTIME_ERROR".equals(errorType)) {
            return SlideErrorType.RUNTIME_ERROR;
        } else if ("RUNTIME_WARN".equals(errorType)) {
            return SlideErrorType.RUNTIME_WARN;
        } else if ("CANVAS_CRASH".equals(errorType)) {
            return SlideErrorType.CANVAS_CRASH;
        }
        return SlideErrorType.UNKNOWN;
    }

    @JavascriptInterface
    public void onPPTMediaPlay(Object args) {
        if (commonCallback != null) {
            commonCallback.onPPTMediaPlay();
        }
    }

    @JavascriptInterface
    public void onPPTMediaPause(Object args) {
        if (commonCallback != null) {
            commonCallback.onPPTMediaPause();
        }
    }

    @JavascriptInterface
    public void setupFail(Object args) {
        JSONObject jsonObject = convertToJsonOrNull(args);
        if (commonCallback != null && jsonObject != null) {
            SDKError sdkError = SDKError.parseError(jsonObject);
            commonCallback.sdkSetupFail(sdkError);
        }
    }

    private JSONObject convertToJsonOrNull(Object args) {
        JSONObject result = null;
        try {
            result = new JSONObject(String.valueOf(args));
        } catch (Exception e) {
            Logger.error("convertToJson exception", e);
        }
        return result;
    }
}