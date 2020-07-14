package com.herewhite.demo;

import android.os.Build;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class LocalFileWebViewClient extends WebViewClient {
    final private String DynamicPpTDomain = "https://convertcdn.netless.link";
    private static final String TAG = "LocalFile";

    public String getPptDirectory() {
        return pptDirectory;
    }

    public void setPptDirectory(String pptDirectory) {
        this.pptDirectory = pptDirectory;
    }

    private String pptDirectory = "";

    @SuppressWarnings(value = "deprecation")
    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
        WebResourceResponse response = localResponse(url, new HashMap<>());
        if (response != null) {
            return response;
        }
        return super.shouldInterceptRequest(view, url);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
        String url = request.getUrl().toString();
        WebResourceResponse response = localResponse(url, request.getRequestHeaders());
        if (response != null) {
            return response;
        }
        return super.shouldInterceptRequest(view, request);
    }

    @Nullable
    WebResourceResponse localResponse(String url, Map<String, String> map) {
        if (url.startsWith(DynamicPpTDomain)) {
            Log.d(TAG, "url: " + url);
            // 最好替换规则更严谨一些，只替换出现在最开始的 https://
            String path = url.replace("https://", "/");
            File file = new File(pptDirectory + path);
            Boolean media = url.endsWith("mp4") || url.endsWith("mp3");

            if (map.get("Range") != null) {
                Log.i("shouldInterceptRequest", "header: " + map.get("Range"));
            }

            try {
                if (file.exists()) {
                    FileInputStream fis = new FileInputStream(file);
                    WebResourceResponse response = null;
                    String mimeTypeFromExtension = MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(url));
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                        HashMap<String, String>headers = new HashMap<>();
                        headers.put("Access-Control-Allow-Origin", "*");
                        headers.put("Access-Control-Allow-Methods", "POST, GET, OPTIONS");
                        headers.put("Access-Control-Allow-Headers", "X-PINGOTHER, Content-Type");

                        if (media) {
                            response = new WebResourceResponse(mimeTypeFromExtension, "UTF-8", fis);
                        } else {
                            response = new WebResourceResponse(mimeTypeFromExtension, "UTF-8", 200, "ok", headers, fis);
                        }
                    } else {
                        response = new WebResourceResponse(mimeTypeFromExtension, "UTF-8", fis);
                    }
                    Log.d(TAG, "shouldInterceptRequest: hit " + url);
                    return response;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

}