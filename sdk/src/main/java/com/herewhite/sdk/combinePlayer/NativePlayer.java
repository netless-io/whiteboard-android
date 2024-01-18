package com.herewhite.sdk.combinePlayer;

/**
 * `NativePlayer` 接口。
 *
 * @since 2.4.23
 */
public interface NativePlayer {

    /**
     * 播放视频。
     *
     * @note
     * 该方法需要由 `PlayerSyncManager` 调用，请勿主动调用。
     */
    void play();

    /**
     * 暂停播放视频。
     *
     * @note
     * 该方法需要由 `PlayerSyncManager` 调用，请勿主动调用。
     */
    void pause();

    /**
     * 获取是否能够不经过缓冲，直接播放视频。
     *
     * @return 是否能够不经过缓冲，直接播放视频：
     * - `true`：可以不经过缓冲，直接播放视频。
     * - `false`：需要经过缓冲才能播放视频。
     */
    boolean hasEnoughBuffer();

    /**
     * 获取视频播放阶段。
     *
     * @return 视频播放阶段，详见 {@link NativePlayer.NativePlayerPhase NativePlayerPhase}。
     */
    NativePlayerPhase getPhase();

    /** 视频播放阶段。 */
    enum NativePlayerPhase {
        /**
         * 视频播放尚未开始或已经结束。
         */
        Idle,
        /**
         * 视频播放已暂停。
         */
        Pause,
        /**
         * 正在播放视频。
         */
        Playing,
        /**
         * 视频正在缓冲。
         */
        Buffering,
    }
}
