package com.herewhite.sdk.internal;

import android.webkit.JavascriptInterface;

import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.herewhite.sdk.CommonCallback;
import com.herewhite.sdk.domain.SDKError;
import com.herewhite.sdk.domain.UrlInterrupter;

import org.json.JSONObject;

public class SdkJsInterfaceImpl {
    private final static Gson gson = new Gson();

    @Nullable
    private CommonCallback commonCallback;
    @Nullable
    private UrlInterrupter urlInterrupter;

    public SdkJsInterfaceImpl(CommonCallback commonCallback) {
        this.commonCallback = commonCallback;
    }

    public void setCommonCallbacks(@Nullable CommonCallback commonCallbacks) {
        this.commonCallback = commonCallbacks;
    }

    @Nullable
    public UrlInterrupter getUrlInterrupter() {
        return urlInterrupter;
    }

    public void setUrlInterrupter(@Nullable UrlInterrupter urlInterrupter) {
        this.urlInterrupter = urlInterrupter;
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