package com.herewhite.sdk.domain;

public class AkkoEvent extends WhiteObject {
    private String eventName;
    private Object payload;

    public AkkoEvent(String eventName, Object payload) {
        this.eventName = eventName;
        this.payload = payload;
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
