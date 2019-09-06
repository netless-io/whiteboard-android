package com.herewhite.sdk.domain;

public class AkkoEvent extends WhiteObject {
    private String eventName;
    private Object payload;

    /**
     * 初始化一个自定义事件内容
     *
     * @param eventName 自定义事件名称
     * @param payload   自定义事件信息，传递 key-value 格式信息时，请传入自定义 WhiteObject 子类，以保持格式。
     */
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
