package com.herewhite.sdk.domain;

import com.google.gson.annotations.SerializedName;
/**
 * 图形工具。
 */
public enum ShapeType {
    /**
     * `Triangle`：（默认）三角形。
     */
    @SerializedName("triangle")
    Triangle,
    /**
     * `Rhombus`：菱形。
     */
    @SerializedName("rhombus")
    Rhombus,
    /**
     * `Pentagram`：五角星。
     */
    @SerializedName("pentagram")
    Pentagram,
    /**
     * `SpeechBalloon`：对话气泡。
     */
    @SerializedName("speechBalloon")
    SpeechBalloon,
}
