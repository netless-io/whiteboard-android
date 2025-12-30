package com.herewhite.sdk.internal;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Message;
import android.view.KeyEvent;
import android.webkit.ClientCertRequest;
import android.webkit.HttpAuthHandler;
import android.webkit.RenderProcessGoneDetail;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.RequiresApi;
import androidx.webkit.WebViewAssetLoader;

import android.net.http.SslError;

public final class WhiteWebViewClient extends WebViewClient {
    private volatile WebViewClient delegate;
    private volatile WebViewAssetLoader assetLoader;

    public WhiteWebViewClient() {
    }

    public WhiteWebViewClient(WebViewAssetLoader assetLoader) {
        this.assetLoader = assetLoader;
    }

    public void setDelegate(WebViewClient delegate) {
        this.delegate = delegate;
    }

    public void setAssetLoader(WebViewAssetLoader assetLoader) {
        this.assetLoader = assetLoader;
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        WebViewClient client = delegate;
        if (client != null) {
            return client.shouldOverrideUrlLoading(view, url);
        }
        return super.shouldOverrideUrlLoading(view, url);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
        WebViewClient client = delegate;
        if (client != null) {
            return client.shouldOverrideUrlLoading(view, request);
        }
        return super.shouldOverrideUrlLoading(view, request);
    }

    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
        WebViewAssetLoader loader = assetLoader;
        if (loader != null) {
            WebResourceResponse response = loader.shouldInterceptRequest(request.getUrl());
            if (response != null) {
                return response;
            }
        }
        WebViewClient client = delegate;
        if (client != null) {
            return client.shouldInterceptRequest(view, request);
        }
        return super.shouldInterceptRequest(view, request);
    }

    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
        WebViewAssetLoader loader = assetLoader;
        if (loader != null) {
            WebResourceResponse response = loader.shouldInterceptRequest(Uri.parse(url));
            if (response != null) {
                return response;
            }
        }
        WebViewClient client = delegate;
        if (client != null) {
            return client.shouldInterceptRequest(view, url);
        }
        return super.shouldInterceptRequest(view, url);
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        WebViewClient client = delegate;
        if (client != null) {
            client.onPageStarted(view, url, favicon);
            return;
        }
        super.onPageStarted(view, url, favicon);
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        WebViewClient client = delegate;
        if (client != null) {
            client.onPageFinished(view, url);
            return;
        }
        super.onPageFinished(view, url);
    }

    @Override
    public void onLoadResource(WebView view, String url) {
        WebViewClient client = delegate;
        if (client != null) {
            client.onLoadResource(view, url);
            return;
        }
        super.onLoadResource(view, url);
    }

    @Override
    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
        WebViewClient client = delegate;
        if (client != null) {
            client.onReceivedError(view, errorCode, description, failingUrl);
            return;
        }
        super.onReceivedError(view, errorCode, description, failingUrl);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
        WebViewClient client = delegate;
        if (client != null) {
            client.onReceivedError(view, request, error);
            return;
        }
        super.onReceivedError(view, request, error);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
        WebViewClient client = delegate;
        if (client != null) {
            client.onReceivedHttpError(view, request, errorResponse);
            return;
        }
        super.onReceivedHttpError(view, request, errorResponse);
    }

    @Override
    public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
        WebViewClient client = delegate;
        if (client != null) {
            client.onReceivedSslError(view, handler, error);
            return;
        }
        super.onReceivedSslError(view, handler, error);
    }

    @Override
    public void onFormResubmission(WebView view, Message dontResend, Message resend) {
        WebViewClient client = delegate;
        if (client != null) {
            client.onFormResubmission(view, dontResend, resend);
            return;
        }
        super.onFormResubmission(view, dontResend, resend);
    }

    @Override
    public void doUpdateVisitedHistory(WebView view, String url, boolean isReload) {
        WebViewClient client = delegate;
        if (client != null) {
            client.doUpdateVisitedHistory(view, url, isReload);
            return;
        }
        super.doUpdateVisitedHistory(view, url, isReload);
    }

    @Override
    public void onReceivedClientCertRequest(WebView view, ClientCertRequest request) {
        WebViewClient client = delegate;
        if (client != null) {
            client.onReceivedClientCertRequest(view, request);
            return;
        }
        super.onReceivedClientCertRequest(view, request);
    }

    @Override
    public void onReceivedHttpAuthRequest(WebView view, HttpAuthHandler handler, String host, String realm) {
        WebViewClient client = delegate;
        if (client != null) {
            client.onReceivedHttpAuthRequest(view, handler, host, realm);
            return;
        }
        super.onReceivedHttpAuthRequest(view, handler, host, realm);
    }

    @Override
    public void onReceivedLoginRequest(WebView view, String realm, String account, String args) {
        WebViewClient client = delegate;
        if (client != null) {
            client.onReceivedLoginRequest(view, realm, account, args);
            return;
        }
        super.onReceivedLoginRequest(view, realm, account, args);
    }

    @Override
    public void onScaleChanged(WebView view, float oldScale, float newScale) {
        WebViewClient client = delegate;
        if (client != null) {
            client.onScaleChanged(view, oldScale, newScale);
            return;
        }
        super.onScaleChanged(view, oldScale, newScale);
    }

    @Override
    public void onUnhandledKeyEvent(WebView view, KeyEvent event) {
        WebViewClient client = delegate;
        if (client != null) {
            client.onUnhandledKeyEvent(view, event);
            return;
        }
        super.onUnhandledKeyEvent(view, event);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public boolean onRenderProcessGone(WebView view, RenderProcessGoneDetail detail) {
        WebViewClient client = delegate;
        if (client != null) {
            return client.onRenderProcessGone(view, detail);
        }
        return super.onRenderProcessGone(view, detail);
    }
}
