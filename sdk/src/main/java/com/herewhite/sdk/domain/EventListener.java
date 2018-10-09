package com.herewhite.sdk.domain;

import org.json.JSONObject;

public interface EventListener {
    void onEvent(EventEntry eventEntry);
}
