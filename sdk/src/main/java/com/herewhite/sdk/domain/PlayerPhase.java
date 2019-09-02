package com.herewhite.sdk.domain;

import com.google.gson.annotations.SerializedName;

public enum PlayerPhase {
    waitingFirstFrame,  //等待第一帧
    playing,            //播放状态
    pause,              //暂停状态
    @SerializedName("stop")
    stopped,            //停止
    ended,              //播放结束
    buffering,
}
