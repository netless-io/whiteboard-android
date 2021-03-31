package com.herewhite.sdk;

/**
 * 通用回调，用于 SDK 向 app 发送回调事件通知
 * 实时房间和回放房间都可以继承该接口下的回调方法。
 *
 * @since 2.9.13
 * @deprecated Use the standard {@link CommonCallback} instead
 */
@Deprecated
public interface CommonCallbacks extends CommonCallback {
}
