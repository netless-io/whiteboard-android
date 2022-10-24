package com.herewhite.sdk.internal;

import org.json.JSONObject;

public interface PostMessageCallback {
    void onMessage(JSONObject jsonObject);
}
