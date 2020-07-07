package com.herewhite.sdk;

/**
 * 部分通用回调，不管是回放房间，还是实时房间，都有该部分通知
 */
public interface CommonCallbacks {

    /**
     *  当sdk出现未捕获的全局错误时，会在此处抛出
     * @param args
     */
    void throwError(Object args);

    /**
     * 动态 ppt 中的音视频媒体，播放通知
     * @param args
     */
    void onPPTMediaPlay(Object args);

    /**
     * 动态 ppt 中的音视频媒体，暂停通知
     * @param args
     */
    void onPPTMediaPause(Object args);
}
