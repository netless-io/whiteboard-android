package com.herewhite.sdk;

import android.webkit.JavascriptInterface;

import com.herewhite.sdk.domain.SDKError;
import com.herewhite.sdk.domain.UrlInterrupter;
import com.herewhite.sdk.internal.Logger;

import org.json.JSONObject;

import java.util.Map;

import androidx.annotation.Nullable;

class SdkJsInterfaceImpl {
    @Nullable
    private CommonCallback commonCallback;
    @Nullable
    private UrlInterrupter urlInterrupter;

    public SdkJsInterfaceImpl(CommonCallback commonCallback) {
        this.commonCallback = commonCallback;
    }

    public void setCommonCallbacks(@Nullable CommonCallbacks commonCallbacks) {
        this.commonCallback = commonCallbacks;
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
        if (commonCallback != null) {
            return commonCallback.urlInterrupter(String.valueOf(args));
        } else if (urlInterrupter == null) {
            return String.valueOf(args);
        }
        return urlInterrupter.urlInterrupter(String.valueOf(args));
    }

    @JavascriptInterface
    public void throwError(Object args) {
        Logger.info("WhiteSDK JS error: " + Utils.fromJson(String.valueOf(args), Map.class));
        if (commonCallback != null) {
            commonCallback.throwError(args);
        }
    }

    @JavascriptInterface
    public void logger(Object args) {
        Logger.info("WhiteSDK logger: " + Utils.fromJson(String.valueOf(args), Map.class));
    }

    @JavascriptInterface
    public void postMessage(Object args) {
        if (commonCallback != null) {
            try {
                JSONObject object = new JSONObject((String) args);
                commonCallback.onMessage(object);
            } catch (Throwable throwable) {
            }
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
    public void setupFail(Object object) {
        if (commonCallback != null && object instanceof JSONObject) {
            SDKError sdkError = SDKError.parseError((JSONObject) object);
            commonCallback.sdkSetupFail(sdkError);
        }
    }
}