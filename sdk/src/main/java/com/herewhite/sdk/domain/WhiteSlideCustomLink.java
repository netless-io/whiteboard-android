package com.herewhite.sdk.domain;

/**
 * Slide 自定义链接
 */
public class WhiteSlideCustomLink {
    private int pageIndex;
    private String shapeId;
    private String link;

    public WhiteSlideCustomLink(int pageIndex, String shapeId, String link) {
        this.pageIndex = pageIndex;
        this.shapeId = shapeId;
        this.link = link;
    }
}