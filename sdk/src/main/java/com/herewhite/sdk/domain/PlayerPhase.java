package com.herewhite.sdk.domain;

public enum PlayerPhase {
    waitingFirstFrame,  //等待第一帧
    playing,            //播放状态
    pause,              //暂停状态
    stopped,            //停止
    ended,              //播放结束
    buffering,
}
