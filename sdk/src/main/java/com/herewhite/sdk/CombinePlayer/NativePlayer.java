package com.herewhite.sdk.CombinePlayer;

/**
 * NativePlayer 接口
 * @since 2.4.22
 */
public interface NativePlayer {

    /**
     * play 方法，由 CombinePlayer 调用，请勿主动调用
     * */
    void play();

    /**
     * pause 方法，由 CombinePlayer 调用，请勿主动调用
     */
    void pause();

    /**
     * 返回是否处于播放状态（play但是由于缺少数据，而正在缓冲，也算）
     *
     * @return 是否正在处于播放状态
     */
    boolean isPlaying();

    /**
     * 是否能够不经过缓冲，而直接播放
     * @return 是否有足够的缓冲
     */
    boolean hasEnoughBuffer();

    enum NativePlayerPhase {
        /**
         * 初始化状态，没有任何信息
         */
        Idle,
        /**
         * 暂停中
         */
        Pause,
        /**
         * 正在连续播放（如果因为没有数据，进入缓冲状态，就是 buffering
         */
        Playing,
        /**
         * 正在缓冲中
         */
        Buffering,
    }
}
