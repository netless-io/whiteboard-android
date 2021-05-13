package com.herewhite.sdk.domain;

import com.google.gson.annotations.SerializedName;

public enum ShapeType {
    /**
     * 三角形（默认）
     */
    @SerializedName("triangle")
    Triangle,
    /**
     * 菱形
     */
    @SerializedName("rhombus")
    Rhombus,
    /**
     * 五角星
     */
    @SerializedName("pentagram")
    Pentagram,
    /**
     * 说话泡泡
     */
    @SerializedName("speechBalloon")
    SpeechBalloon,
}
