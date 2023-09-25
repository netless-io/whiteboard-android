package com.herewhite.demo.utils;

import android.webkit.ValueCallback;

import com.herewhite.sdk.WhiteboardView;

public class Scaffold {
    /**
     * save and restore camera state
     *
     * whiteboardView.addOnLayoutChangeListener { _, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
     *     if (left == oldLeft && top == oldTop && right == oldRight && bottom == oldBottom) {
     *         return@addOnLayoutChangeListener
     *     }
     *
     *     if (right - left < LimitedWidth) {
     *         cameraState = room.roomState.cameraState
     *     } else {
     *         cameraState?.let {
     *             val config = CameraConfig().apply {
     *                 scale = it.scale
     *                 centerX = it.centerX
     *                 centerY = it.centerY
     *             }
     *             room.moveCamera(config)
     *         }
     *     }
     * }
     */

    /**
     * hide and show title bar
     *
     * String cssHide = "" +
     *         "var elements = document.querySelectorAll(\".telebox-titlebar, .telebox-titlebar-wrap\");\n" +
     *         "for (var i = 0; i < elements.length; i++) {\n" +
     *         "    elements[i].style.display = \"none\";\n" +
     *         "} \n";
     *
     * String cssShow = "" +
     *         "var elements = document.querySelectorAll(\".telebox-titlebar, .telebox-titlebar-wrap\");\n" +
     *         "for (var i = 0; i < elements.length; i++) {\n" +
     *         "    elements[i].style.display = \"\";\n" +
     *         "} \n";
     *
     * mWhiteboardView.evaluateJavascript(cssShow, null);
     *
     * String hideTitleBar = "" +
     *         "if (!window.__hideTitleBar) {\n" +
     *         "  var style = document.createElement('style')\n" +
     *         "  style.innerText = '.telebox-titlebar, .telebox-titlebar-wrap { display: none; }'\n" +
     *         "  window.__hideTitleBar = style\n" +
     *         "}\n" +
     *         "document.head.appendChild(window.__hideTitleBar)";
     * mWhiteboardView.evaluateJavascript(hideTitleBar, null);
     */

    public void fetchJsVariable(WhiteboardView whiteboardView, String variableName) {
        String jsCode = "JSON.stringify(" + variableName + ");";
//        whiteboardView.evaluateJavascript(jsCode, new ValueCallback<String>() {
//            @Override
//            public void onReceiveValue(String value) {
//                // log the value
//            }
//        });
    }
}
