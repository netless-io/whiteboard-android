package com.herewhite.sdk.domain;

import com.google.gson.annotations.SerializedName;

/** Slide 底部前后导航按钮的行为，不影响其他翻页入口。 */
public enum SlideNavigationButtonMode {
    /** 按页切换（默认行为）。 */
    @SerializedName("page")
    Page,

    /** 按动画步骤切换。 */
    @SerializedName("step")
    Step
}
