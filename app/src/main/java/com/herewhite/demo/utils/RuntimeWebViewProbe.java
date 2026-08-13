package com.herewhite.demo.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.pm.PackageInfo;
import android.os.Build;
import android.webkit.WebView;

public final class RuntimeWebViewProbe {
    private RuntimeWebViewProbe() {
    }

    public static void show(Activity activity, WebView webView) {
        String packageName = "unavailable";
        String packageVersion = "unavailable";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            PackageInfo packageInfo = WebView.getCurrentWebViewPackage();
            if (packageInfo != null) {
                packageName = packageInfo.packageName;
                packageVersion = packageInfo.versionName;
            }
        }

        String provider = "webviewPackage: " + packageName
                + "\nwebviewVersion: " + packageVersion
                + "\nplatform: android API " + Build.VERSION.SDK_INT;
        webView.evaluateJavascript("navigator.userAgent", userAgent ->
                activity.runOnUiThread(() -> new AlertDialog.Builder(activity)
                        .setTitle("WebView Runtime Probe")
                        .setMessage(provider + "\n\nnavigator.userAgent:\n" + userAgent)
                        .setPositiveButton("OK", null)
                        .show()));
    }
}
