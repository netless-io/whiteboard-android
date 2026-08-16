package com.herewhite.sdk.domain;

public class ReloadBackgroundImageParams extends WhiteObject {
    private final String source;
    private final String viewId;
    private String scenePath;
    private String url;

    public ReloadBackgroundImageParams(String source, String viewId) {
        this.source = source;
        this.viewId = viewId;
    }

    public ReloadBackgroundImageParams(String source, String viewId, String scenePath) {
        this.source = source;
        this.viewId = viewId;
        this.scenePath = scenePath;
    }

    public String getSource() { return source; }
    public String getViewId() { return viewId; }
    public String getScenePath() { return scenePath; }
    public void setScenePath(String value) { scenePath = value; }
    public String getUrl() { return url; }
    public void setUrl(String value) { url = value; }
}
