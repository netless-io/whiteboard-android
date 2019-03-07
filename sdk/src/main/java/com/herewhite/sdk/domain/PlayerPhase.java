package com.herewhite.sdk.domain;

public enum PlayerPhase {
    WhitePlayerPhaseWaitingFirstFrame,  //等待第一帧
    WhitePlayerPhasePlaying,            //播放状态
    WhitePlayerPhasePause,              //暂停状态
    WhitePlayerPhaseStopped,            //停止
    WhitePlayerPhaseEnded,              //播放结束
    WhitePlayerPhaseBuffering,
}
