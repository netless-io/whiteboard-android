package com.herewhite.sdk.domain;

import com.google.gson.annotations.SerializedName;

public enum WindowPrefersColorScheme {
    @SerializedName("light")
    Light,

    @SerializedName("dark")
    Dark,

    @SerializedName("auto")
    Auto,
}
