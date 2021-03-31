package com.herewhite.sdk.domain;

/**
 * `EventListener` 接口类。该类包含你想要注册监听的事件回调。
 */
public interface EventListener {
    /**
     * 收到监听的事件回调。
     *
     * @param eventEntry 监听的事件，详见 {@link EventEntry}。
     */
    void onEvent(EventEntry eventEntry);
}
