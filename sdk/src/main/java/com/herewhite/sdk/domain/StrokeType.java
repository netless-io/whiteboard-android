package com.herewhite.sdk.domain;

import com.google.gson.annotations.SerializedName;

/**
 * 图形工具。
 */
public enum StrokeType {
    @SerializedName("Normal")
    Normal,
    @SerializedName("Stroke")
    Stroke,
    @SerializedName("Dotted")
    Dotted,
    @SerializedName("LongDotted")
    LongDotted,
}
