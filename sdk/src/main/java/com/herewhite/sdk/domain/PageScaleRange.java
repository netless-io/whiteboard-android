package com.herewhite.sdk.domain;

/** Optional page scale bounds relative to fitted content size. */
public class PageScaleRange extends WhiteObject {
    private Double minScale;
    private Double maxScale;

    public Double getMinScale() { return minScale; }
    public PageScaleRange setMinScale(Double minScale) { this.minScale = minScale; return this; }
    public Double getMaxScale() { return maxScale; }
    public PageScaleRange setMaxScale(Double maxScale) { this.maxScale = maxScale; return this; }
}
