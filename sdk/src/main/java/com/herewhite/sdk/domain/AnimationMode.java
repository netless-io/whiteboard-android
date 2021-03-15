package com.herewhite.sdk.domain;

import com.google.gson.annotations.SerializedName;

/**
 * 视野调整、视角移动时，动画选项
 *
 * @since 2.3.2
 */
public enum AnimationMode {
    /**
     * 连续变化，移动视角/调整视野时，会进行补间动画（默认）
     */
    @SerializedName("continuous")
    Continuous,
    /**
     * 瞬间切换
     */
    @SerializedName("immediately")
    Immediately
}
