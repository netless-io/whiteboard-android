package com.herewhite.sdk.CombinePlayer;

import com.herewhite.sdk.Player;
import com.herewhite.sdk.domain.PlayerPhase;

import java.util.concurrent.TimeUnit;

public class CombinePlayer {

    public interface Callbacks {
        void startBuffering();
        void endBuffering();
    }

    private enum PauseReason {
        None(0),
        WaitingWhitePlayerBuffering(1),
        WaitingNativePlayerBuffering(1 << 1),
        Pause(1 << 2),
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
            for (PauseReason p:PauseReason.values()) {
                if (value == p.getValue()) {
                    return p;
                }

            }
            return PauseReason.None;
        }

        public PauseReason addFlag(PauseReason flag) {
            int value = getValue() | flag.getValue();
            for (PauseReason p:PauseReason.values()) {
                if (value == p.getValue()) {
                    return p;
                }

            }
            return PauseReason.None;
        }
    }

    private final Player whitePlayer;
    private PauseReason pauseReason = PauseReason.Init;
    private NativePlayer nativePlayer;
    private Callbacks callbacks;

    public CombinePlayer(Player whitePlayer, NativePlayer nativePlayer, Callbacks callbacks) {
        this.whitePlayer = whitePlayer;
        this.nativePlayer = nativePlayer;
        this.callbacks = callbacks;
    }

    public void play() {

        pauseReason = pauseReason.removeFlag(PauseReason.Pause);

        nativePlayer.play();
        if (nativePlayer.hasEnoughNativeBuffer()) {
            whitePlayer.play();
        }
    }

    public void pause() {

        pauseReason = pauseReason.addFlag(PauseReason.Pause);

        nativePlayer.pause();
        whitePlayer.pause();
    }

    public void seek(long time, TimeUnit timeUnit) {
        nativePlayer.seek(time, timeUnit);
    }

    public void updateNativePhase(NativePlayer.NativePlayerPhase phase) {
        if (phase == NativePlayer.NativePlayerPhase.Buffering) {
            nativeStartBuffering();
        } else {
            nativeEndBuffering();
        }
    }

    private void nativeStartBuffering() {

        pauseReason = pauseReason.addFlag(PauseReason.WaitingNativePlayerBuffering);

        callbacks.startBuffering();

        whitePlayer.pause();
    }

    private void nativeEndBuffering() {

        pauseReason = pauseReason.removeFlag(PauseReason.WaitingNativePlayerBuffering);

        if (pauseReason.hasFlag(PauseReason.WaitingWhitePlayerBuffering)) {
            whitePlayer.pause();
        } else {
            callbacks.endBuffering();
        }

        if (pauseReason.equals(PauseReason.None)) {
            nativePlayer.play();
            whitePlayer.play();
        }
    }

    public void updateWhitePlayerPhase(PlayerPhase phase) {
        if (phase == PlayerPhase.buffering || phase == PlayerPhase.waitingFirstFrame) {
            whitePlayerStartBuffering();
        } else if (phase == PlayerPhase.pause || phase == PlayerPhase.playing) {
            whitePlayerEndBuffering();
        }
    }

    private void whitePlayerStartBuffering() {

        pauseReason = pauseReason.addFlag(PauseReason.WaitingWhitePlayerBuffering);

        nativePlayer.pause();

        callbacks.startBuffering();
    }

    private void whitePlayerEndBuffering() {
        pauseReason = pauseReason.removeFlag(PauseReason.WaitingWhitePlayerBuffering);

        if (pauseReason.hasFlag(PauseReason.WaitingNativePlayerBuffering)) {
            whitePlayer.pause();
        } else {
            callbacks.endBuffering();
        }

        if (pauseReason.equals(PauseReason.None)) {
            nativePlayer.play();
            whitePlayer.play();
        } else if (pauseReason.hasFlag(PauseReason.Pause)) {
            nativePlayer.pause();
            whitePlayer.pause();
        }
    }

}
