package com.herewhite.sdk.domain;

public class EventEntry {
    private String uuid;
    private String eventName;
    private Object payload;

    public EventEntry(String eventName, Object payload) {
        this.eventName = eventName;
        this.payload = payload;
    }

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

    public Object getPayload() {
        return payload;
    }

    public void setPayload(Object payload) {
        this.payload = payload;
    }
}
