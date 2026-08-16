package com.herewhite.sdk.domain;

import org.json.JSONObject;

public final class BackgroundImageLoadEvent {
    public final String name;
    public final String state;
    public final String source;
    public final String resourceId;
    public final String resourceUrl;
    public final String viewId;
    public final String scenePath;

    public BackgroundImageLoadEvent(JSONObject json) {
        name = json.optString("name");
        state = json.optString("state");
        source = json.optString("source");
        resourceId = json.optString("resourceId");
        resourceUrl = json.optString("resourceUrl");
        viewId = json.optString("viewId");
        scenePath = json.optString("scenePath");
    }
}
