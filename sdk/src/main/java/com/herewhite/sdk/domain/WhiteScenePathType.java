package com.herewhite.sdk.domain;

import com.google.gson.annotations.SerializedName;

/**
 * 白板场景的路径类型。
 *
 * @since 2.6.4
 */
public enum WhiteScenePathType {
    /**
     * 查询的路径不存在。
     */
    @SerializedName("none")
    Empty,
    /**
     * 查询路径为场景路径。
     */
    @SerializedName("page")
    Page,
    /**
     * 查询路径为场景组路径。
     */
    @SerializedName("dir")
    Dir,
}
