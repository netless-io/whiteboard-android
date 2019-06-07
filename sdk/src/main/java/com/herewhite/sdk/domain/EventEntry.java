package com.herewhite.sdk.domain;

public class EventEntry extends WhiteObject {
    private String uuid;
    private String eventName;
    private Object payload;
    private String scope;
    private long authorId;

    public String getScope() {
        return scope;
    }

    public long getAuthorId() {
        return authorId;
    }

    public String getUuid() {
        return uuid;
    }

    public String getEventName() {
        return eventName;
    }

    public Object getPayload() {
        return payload;
    }
}
