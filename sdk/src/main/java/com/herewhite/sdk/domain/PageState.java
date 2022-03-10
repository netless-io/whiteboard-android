package com.herewhite.sdk.domain;

/**
 * 页面状态。
 */
public class PageState extends WhiteObject {
    /**
     * 当前主窗口页面索引
     */
    private int index;
    /**
     * 当前主窗口页面数量
     */
    private int length;

    public PageState(int index, int length) {
        this.index = index;
        this.length = length;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }
}
