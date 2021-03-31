package com.herewhite.sdk.domain;

import com.google.gson.annotations.SerializedName;

/**
 * 数据中心。
 */
public enum Region {
    /**
     * 中国杭州。
     */
    @SerializedName("cn-hz")
    cn,
    /**
     * 美国硅谷。
     */
    @SerializedName("us-sv")
    us;
}
