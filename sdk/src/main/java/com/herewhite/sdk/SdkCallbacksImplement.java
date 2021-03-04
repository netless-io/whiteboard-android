package com.herewhite.sdk;

import android.webkit.JavascriptInterface;

import com.herewhite.sdk.domain.SDKError;
import com.herewhite.sdk.domain.UrlInterrupter;

import org.json.JSONObject;

import java.util.Map;

import androidx.annotation.Nullable;

class SdkCallbacksImplement {
    @Nullable
    private CommonCallbacks commonCallbacks;
    @Nullable
    private UrlInterrupter urlInterrupter;

    public SdkCallbacksImplement() {

    }

    public SdkCallbacksImplement(CommonCallbacks commonCallbacks) {
        this.commonCallbacks = commonCallbacks;
    }

    public void setCommonCallbacks(@Nullable CommonCallbacks commonCallbacks) {
        this.commonCallbacks = commonCallbacks;
    }

    public void setUrlInterrupter(@Nullable UrlInterrupter urlInterrupter) {
        this.urlInterrupter = urlInterrupter;
    }

    @Nullable
    public UrlInterrupter getUrlInterrupter() {
        return urlInterrupter;
    }

    @JavascriptInterface
    public String urlInterrupter(Object args) {
        if (commonCallbacks != null) {
            return commonCallbacks.urlInterrupter(String.valueOf(args));
        } else if (urlInterrupter == null) {
            return String.valueOf(args);
        }
        return urlInterrupter.urlInterrupter(String.valueOf(args));
    }

    @JavascriptInterface
    public void throwError(Object args) {
        Logger.info("WhiteSDK JS error: " + Utils.fromJson(String.valueOf(args), Map.class));
        if (commonCallbacks != null) {
            commonCallbacks.throwError(args);
        }
    }

    @JavascriptInterface
    public void logger(Object args) {
        Logger.info("WhiteSDK logger: " + Utils.fromJson(String.valueOf(args), Map.class));
    }

    @JavascriptInterface
    public void postMessage(Object args) {
        if (commonCallbacks != null) {
            try {
                JSONObject object = new JSONObject((String) args);
                commonCallbacks.onMessage(object);
            } catch (Throwable throwable) {
            }
        }
    }

    @JavascriptInterface
    public void onPPTMediaPlay(Object args) {
        if (commonCallbacks != null) {
            commonCallbacks.onPPTMediaPlay();
        }
    }

    @JavascriptInterface
    public void onPPTMediaPause(Object args) {
        if (commonCallbacks != null) {
            commonCallbacks.onPPTMediaPause();
        }
    }

    @JavascriptInterface
    public void setupFail(Object object) {
        if (commonCallbacks != null && object instanceof JSONObject) {
            SDKError sdkError = SDKError.parseError((JSONObject) object);
            commonCallbacks.sdkSetupFail(sdkError);
        }
    }
}