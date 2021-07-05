package com.herewhite.demo.test;

import android.os.Build;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.RequiresApi;

public class PptCacheWebViewClient extends WebViewClient {

    private PptResourceManager pptResourceManager;

    public void setPptResourceManager(PptResourceManager resourceManager) {
        this.pptResourceManager = resourceManager;
    }

    @SuppressWarnings(value = "deprecation")
    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
        if (pptResourceManager != null) {
            WebResourceResponse response = pptResourceManager.intercept(url);
            if (response != null) {
                return response;
            }
        }
        return super.shouldInterceptRequest(view, url);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
        if (pptResourceManager != null) {
            WebResourceResponse response = pptResourceManager.intercept(request);
            if (response != null) {
                return response;
            }
        }
        return super.shouldInterceptRequest(view, request);
    }
}