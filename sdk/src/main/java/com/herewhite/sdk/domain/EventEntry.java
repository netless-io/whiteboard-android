package com.herewhite.sdk.domain;

import org.json.JSONObject;

public class EventEntry {
    private String uuid;
    private String eventName;
    private JSONObject Payload;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public JSONObject getPayload() {
        return Payload;
    }

    public void setPayload(JSONObject payload) {
        Payload = payload;
    }
}
