package com.herewhite.sdk.domain;

import com.google.gson.annotations.SerializedName;

/**
 * Created by buhe on 2018/8/11.
 */

public enum ViewMode {
    @SerializedName("freedom")
    Freedom,
    @SerializedName("follower")
    Follower,
    @SerializedName("broadcaster")
    Broadcaster
}
