package com.herewhite.sdk.CombinePlayer;

import java.util.concurrent.TimeUnit;

public interface NativePlayer {

    void play();
    void pause();

    //TODO:rate等待WhitePlayer支持后再添加
    //void rate(float rate);

    void seek(long time, TimeUnit unit);

    boolean isPlaying();
    boolean hasEnoughBuffer();

    enum NativePlayerPhase {
        Idle,
        Pause,
        Playing,
        Buffering,
    }
}
