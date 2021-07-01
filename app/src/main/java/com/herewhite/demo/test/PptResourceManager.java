package com.herewhite.demo.test;

import android.os.Build;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;

import androidx.annotation.RequiresApi;

import com.herewhite.sdk.domain.RoomState;
import com.netless.pptdownload.DownloadHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class PptResourceManager {
    private static final String TAG = "PptResourceManager";

    private static PptResourceManager sInstance;

    private DownloadHelper downloadHelper;

    private String pptCacheDir;
    private String pptDomain;

    private PptResourceManager() {
        downloadHelper = DownloadHelper.getInstance();
    }

    public static synchronized PptResourceManager getInstance() {
        if (sInstance == null) {
            sInstance = new PptResourceManager();
        }
        return sInstance;
    }

    /**
     * 设置pptDomain
     * @param pptDomain
     */
    public void setPptDomain(String pptDomain) {
        this.pptDomain = pptDomain;
        downloadHelper.setDomain(pptDomain);
    }

    /**
     * 设置PPT缓存目录
     * @param pptCacheDir
     */
    public void setPptCacheDir(String pptCacheDir) {
        this.pptCacheDir = pptCacheDir;
        downloadHelper.setPPTCacheDir(pptCacheDir);
    }

    public void updateRoomState(RoomState state) {
        DownloadHelper.updateRoomState(state);
    }

    public WebResourceResponse intercept(String url) {
        return intercept(url, Collections.EMPTY_MAP);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public WebResourceResponse intercept(WebResourceRequest request) {
        return intercept(request.getUrl().toString(), request.getRequestHeaders());
    }

    private WebResourceResponse intercept(String url, Map<String, String> map) {
        if (!checkIntercept(url)) {
            return null;
        }
        Log.d(TAG, "url: " + url);
        File file = getFileByUrl(url);
        if (file == null || !file.isFile()) {
            return null;
        }

        WebResourceResponse response = null;
        try {
            String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(url));
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                if (isMedia(url) && map.containsKey("Range")) {
                    Map<String, String> headers = new HashMap<>();
                    FileInputStream fis = new FileInputStream(file);
                    int totalRange = fis.available();
                    String rangeString = map.get("Range");
                    String[] parts = rangeString.split("=");
                    String[] streamParts = parts[1].split("-");
                    String fromRange = streamParts[0];
                    int range = totalRange - 1;
                    if (streamParts.length > 1 && streamParts[1] != "") {
                        range = Integer.parseInt(streamParts[1]);
                    }
                    headers.put("Accept-Ranges", "bytes");
                    headers.put("Content-Range", "bytes " + fromRange + "-" + range + "/" + totalRange);

                    int statusCode = fromRange.equals("0") ? 200 : 206;
                    Log.i(TAG, "code: " + statusCode + headers);
                    response = new WebResourceResponse(mimeType, "UTF-8", statusCode, "ok", headers, fis);
                } else {
                    HashMap<String, String> headers = new HashMap<>();
                    headers.put("Access-Control-Allow-Origin", "*");
                    headers.put("Access-Control-Allow-Methods", "POST, GET, OPTIONS");
                    headers.put("Access-Control-Allow-Headers", "X-PINGOTHER, Content-Type");

                    response = new WebResourceResponse(mimeType, "UTF-8", 200, "ok", headers, new FileInputStream(file));
                }
            } else {
                response = new WebResourceResponse(mimeType, "UTF-8", new FileInputStream(file));
            }
            Log.d(TAG, "InterceptRequest: hit " + url);
        } catch (IOException e) {
        }
        return response;
    }

    private boolean isMedia(String url) {
        return url.endsWith("mp4") || url.endsWith("mp3");
    }

    private boolean checkIntercept(String url) {
        if (pptDomain != null) {
            return url.startsWith(pptDomain);
        }
        return url.contains("/dynamicConvert/");
    }

    private File getFileByUrl(String url) {
        int index = url.indexOf("/dynamicConvert/");
        if (index == -1) {
            return null;
        }
        int pathStart = index + "/dynamicConvert/".length();
        return new File(pptCacheDir, url.substring(pathStart));
    }
}
