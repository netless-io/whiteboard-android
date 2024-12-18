package com.herewhite.sdk;

import wendu.dsbridge.special.OnReturnValue;

public interface JsBridgeInterface {
    void addJavascriptObject(Object object, String namespace);

    <T> void callHandler(String method, Object[] args, OnReturnValue<T> handler);

    <T> void callHandler(String method, OnReturnValue<T> handler);

    void callHandler(String method, Object[] args);

    /**
     * 触发获取焦点
     */
    default void callFocusView() {
    }
}
