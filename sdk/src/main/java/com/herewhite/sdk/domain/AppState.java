package com.herewhite.sdk.domain;

public class AppState extends WhiteObject {
    /**
     * 当前主窗口页面索引
     */
    private String focusedId;
    /**
     * 当前主窗口页面数量
     */
    private String[] appIds;

    public AppState() {
    }

    public String getFocusedId() {
        return focusedId;
    }

    public String[] getAppIds() {
        return appIds;
    }
}