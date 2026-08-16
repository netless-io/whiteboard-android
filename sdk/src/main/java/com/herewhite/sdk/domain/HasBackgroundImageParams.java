package com.herewhite.sdk.domain;

/** 查询指定白板、场景和 URL 是否已有背景图。 */
public class HasBackgroundImageParams extends WhiteObject {
    private final String viewId;
    private final String scenePath;
    private final String imageUrl;
    private final String[] sources;

    public HasBackgroundImageParams(
            String viewId,
            String scenePath,
            String imageUrl,
            String[] sources) {
        this.viewId = viewId;
        this.scenePath = scenePath;
        this.imageUrl = imageUrl;
        this.sources = sources == null ? new String[0] : sources.clone();
    }

    public String getViewId() {
        return viewId;
    }

    public String getScenePath() {
        return scenePath;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String[] getSources() {
        return sources.clone();
    }
}
