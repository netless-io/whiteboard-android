package com.herewhite.sdk.CombinePlayer;

import java.util.concurrent.TimeUnit;

public interface NativePlayer {
    void play();
    void pause();
    //TODO:rate等待WhitePlayer支持后再添加
    //void rate(int rate);
    void seek(long time, TimeUnit unit);
    boolean hasEnoughNativeBuffer();

    enum NativePlayerPhase {
        Pause,
        Playing,
        Buffering,
    }
}
