package com.herewhite.sdk.domain;

import com.google.gson.annotations.SerializedName;

public enum SlideSyncEventQueuePolicy {
    @SerializedName("fifo")
    Fifo,

    @SerializedName("latest-pending-render")
    LatestPendingRender
}
