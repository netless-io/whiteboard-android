package com.herewhite.sdk.domain;

import com.google.gson.annotations.SerializedName;

public enum WhiteScenePathType {
    @SerializedName("none")
    Empty,
    @SerializedName("page")
    Page,
    @SerializedName("dir")
    Dir,
}
