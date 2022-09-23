package com.herewhite.sdk.combinePlayer;

import android.os.Handler;
import android.os.Looper;

import com.herewhite.sdk.Player;
import com.herewhite.sdk.domain.PlayerPhase;

import java.util.concurrent.TimeUnit;

/**
 * `PlayerSyncManager` 类，用于同步 `NativePlayer` 和 `Player` 的状态。
 *
 * @since 2.4.23
 */
public class PlayerSyncManager {

    private Player whitePlayer;
    private PauseReason pauseReason = PauseReason.Init;
    private NativePlayer nativePlayer;
    private Callbacks callbacks;
    private Handler mainHandler = new Handler(Looper.getMainLooper());
    /**
     * `PlayerSyncManager` 构造方法，用于初始化 `PlayerSyncManager` 实例。
     *
     * @param whitePlayer 白板回放播放器，详见 {@link Player}。
     * @param nativePlayer 本地视频播放器，详见 {@link NativePlayer}。
     * @param callbacks 播放器事件回调，详见 {@link PlayerSyncManager#Callbacks Callbacks}。
     */
    public PlayerSyncManager(Player whitePlayer, NativePlayer nativePlayer, Callbacks callbacks) {
        this.whitePlayer = whitePlayer;
        this.nativePlayer = nativePlayer;
        this.callbacks = callbacks;
        this.updateNativePhase(nativePlayer.getPhase());
        this.updateWhitePlayerPhase(whitePlayer.getPlayerPhase());
    }

    /**
     * `PlayerSyncManager` 构造方法，用于初始化 `PlayerSyncManager` 实例。
     *
     * @param nativePlayer 本地视频播放器，详见 {@link NativePlayer}。
     * @param callbacks 播放器事件回调，详见 {@link PlayerSyncManager#Callbacks Callbacks}。
     */
    public PlayerSyncManager(NativePlayer nativePlayer, Callbacks callbacks) {
        this.nativePlayer = nativePlayer;
        this.callbacks = callbacks;
        this.updateNativePhase(nativePlayer.getPhase());
    }

    /**
     * 设置白板回放播放器。
     *
     * @param whitePlayer 白板回放播放器，详见 {@link Player}。
     */
    public void setWhitePlayer(Player whitePlayer) {
        this.whitePlayer = whitePlayer;
        this.updateWhitePlayerPhase(whitePlayer.getPlayerPhase());
    }

    /**
     * 播放视频。
     */
    public void play() {

        pauseReason = pauseReason.removeFlag(PauseReason.Pause);

        playNativePlayer();
        if (nativePlayer != null && nativePlayer.hasEnoughBuffer()) {
            playWhitePlayer();
        }
    }

    /**
     * 暂停播放视频。
     */
    public void pause() {

        pauseReason = pauseReason.addFlag(PauseReason.Pause);
        pauseNativePlayer();
        pauseWhitePlayer();
    }

    /**
     * 调整白板回放的播放进度。
     *
     * 调整本地视频播放的进度后，你需要调用该方法，将白板回放的播放进度调整到对应位置。
     *
     * @param time     白板回放的播放进度。
     * @param timeUnit 时长单位，默认值为毫秒 （`MILLISECONDS`），取值详见 [TimeUnit](https://www.android-doc.com/reference/java/util/concurrent/TimeUnit.html)。
     */
    public void seek(long time, TimeUnit timeUnit) {
        // Android 端比较适合由 NativePlayer 进行 seek。 seek 完成后，再调用 PlayerSyncManager 的 seek 方法，
        // 将 whitePlayer seek 到对应位置
        Long milliseconds = TimeUnit.MILLISECONDS.convert(time, timeUnit);
        if (whitePlayer != null) {
            whitePlayer.seekToScheduleTime(milliseconds.intValue());
        }
    }

    /**
     * 向 `PlayerSyncManager` 同步 `NativePlayer` 的状态。
     *
     * `PlayerSyncManager` 接收到 `NativePlayer` 的状态后会同步给 `Player`，以确保 `Player` 和 `NativePlayer` 的状态同步。
     *
     * @param phase `NativePlayer` 的播放状态，详见 {@link NativePlayer#NativePlayerPhase NativePlayerPhase}。
     */
    public void updateNativePhase(NativePlayer.NativePlayerPhase phase) {
        if (phase == NativePlayer.NativePlayerPhase.Buffering || phase == NativePlayer.NativePlayerPhase.Idle) {
            nativeStartBuffering();
        } else {
            nativeEndBuffering();
        }
    }

    private void runOnMainThread(Runnable runnable) {
        if (Looper.getMainLooper() == Looper.myLooper()) {
            runnable.run();
            return;
        }
        mainHandler.post(runnable);
    }

    private void playNativePlayer() {
        if (nativePlayer != null) {
            runOnMainThread(new Runnable() {
                @Override
                public void run() {
                    nativePlayer.play();
                }
            });
        }
    }

    private void pauseNativePlayer() {
        if (nativePlayer != null) {
            runOnMainThread(new Runnable() {
                @Override
                public void run() {
                    nativePlayer.pause();
                }
            });
        }
    }

    private void playWhitePlayer() {
        if (whitePlayer != null) {
            whitePlayer.play();
        }
    }

    private void pauseWhitePlayer() {
        if (whitePlayer != null) {
            whitePlayer.pause();
        }
    }

    private void nativeStartBuffering() {

        pauseReason = pauseReason.addFlag(PauseReason.WaitingNativePlayerBuffering);

        callbacks.startBuffering();

        pauseWhitePlayer();
    }

    private void nativeEndBuffering() {

        boolean isBuffering = pauseReason.hasFlag(PauseReason.WaitingWhitePlayerBuffering) || pauseReason.hasFlag(PauseReason.WaitingNativePlayerBuffering);
        pauseReason = pauseReason.removeFlag(PauseReason.WaitingNativePlayerBuffering);

        if (pauseReason.hasFlag(PauseReason.WaitingWhitePlayerBuffering)) {
            pauseNativePlayer();
        } else if (isBuffering) {
            callbacks.endBuffering();
        }

        if (pauseReason.equals(PauseReason.None)) {
            playNativePlayer();
            playWhitePlayer();
        }
    }

    /**
     * 更新白板回放播放器的播放状态。
     *
     * @param phase 白板回放播放器的播放状态，详见 {@link com.herewhite.sdk.domain.PlayerPhase PlayerPhase}。
     */
    public void updateWhitePlayerPhase(PlayerPhase phase) {
        if (phase == PlayerPhase.buffering || phase == PlayerPhase.waitingFirstFrame) {
            whitePlayerStartBuffering();
        } else if (phase == PlayerPhase.pause || phase == PlayerPhase.playing) {
            whitePlayerEndBuffering();
        }
    }

    private void whitePlayerStartBuffering() {

        pauseReason = pauseReason.addFlag(PauseReason.WaitingWhitePlayerBuffering);

        pauseNativePlayer();

        callbacks.startBuffering();
    }

    private void whitePlayerEndBuffering() {

        boolean isBuffering = pauseReason.hasFlag(PauseReason.WaitingWhitePlayerBuffering) || pauseReason.hasFlag(PauseReason.WaitingNativePlayerBuffering);
        pauseReason = pauseReason.removeFlag(PauseReason.WaitingWhitePlayerBuffering);

        if (pauseReason.hasFlag(PauseReason.WaitingNativePlayerBuffering)) {
            pauseWhitePlayer();
        } else if (isBuffering) {
            callbacks.endBuffering();
        }

        if (pauseReason.equals(PauseReason.None)) {
            playNativePlayer();
            playWhitePlayer();
        } else if (pauseReason.hasFlag(PauseReason.Pause)) {
            pauseWhitePlayer();
            pauseNativePlayer();
        }
    }

    private enum PauseReason {
        None(0),
        WaitingWhitePlayerBuffering(1),
        WaitingNativePlayerBuffering(1 << 1),
        WaitingBothBuffering(1 << 1 | 1),
        Pause(1 << 2),
        PauseAndWhiteBuffering(1 << 2 | 1),
        PauseAndNativeBuffering(1 << 2 | 1 << 1),
        PauseAndBothBuffering(1 << 2 | 1 << 1 | 1),
        Init(1 | 1 << 1 | 1 << 2);

        private int flag;

        PauseReason(int flag) {
            this.flag = flag;
        }

        public int getValue() {
            return flag;
        }

        public boolean equals(PauseReason flag) {
            return flag.getValue() == getValue();
        }

        public boolean hasFlag(PauseReason flag) {
            return (getValue() & flag.getValue()) != PauseReason.None.getValue();
        }

        public PauseReason removeFlag(PauseReason flag) {
            int value = getValue() & ~flag.getValue();
            for (PauseReason p : PauseReason.values()) {
                if (value == p.getValue()) {
                    return p;
                }

            }
            return PauseReason.None;
        }

        public PauseReason addFlag(PauseReason flag) {
            int value = getValue() | flag.getValue();
            for (PauseReason p : PauseReason.values()) {
                if (value == p.getValue()) {
                    return p;
                }

            }
            return PauseReason.None;
        }
    }

    /**
     * `Callbacks` 接口，用于监听 `PlayerSyncManager` 对象的事件。
     */
    public interface Callbacks {
        /**
         * 开始缓冲。
         */
        void startBuffering();

        /**
         * 结束缓冲。
         */
        void endBuffering();
    }

}
