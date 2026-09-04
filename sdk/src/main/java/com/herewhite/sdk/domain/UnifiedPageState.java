package com.herewhite.sdk.domain;

import androidx.annotation.Nullable;

public class UnifiedPageState extends WhiteObject {
    private String target;
    @Nullable private String appId;
    private int page;
    private int pageCount;
    @Nullable private Double scale;

    public String getTarget() { return target; }
    @Nullable public String getAppId() { return appId; }
    public int getPage() { return page; }
    public int getPageCount() { return pageCount; }
    @Nullable public Double getScale() { return scale; }
}
