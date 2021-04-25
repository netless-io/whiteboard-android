package com.herewhite.sdk.domain;

/** `AkkoEvent` 类，用于设置自定义事件。 */
public class AkkoEvent extends WhiteObject {
    private String eventName;
    private Object payload;

    /**
     * `AkkoEvent` 构造方法，用于初始化自定义事件。
     *
     * @param eventName 自定义事件名称
     * @param payload   自定义事件信息，必须为 {@link WhiteObject} 子类，以保证数据格式正确。
     */
    public AkkoEvent(String eventName, Object payload) {
        this.eventName = eventName;
        this.payload = payload;
    }

    /**
     * 获取自定义事件的名称。
     *
     * @return 自定义事件的名称。
     */
    public String getEventName() {
        return eventName;
    }

    /**
     * 设置自定义事件的名称。
     *
     * @param eventName 自定义事件的名称。
     */
    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    /**
     * 设置自定义事件的内容。
     *
     * @return 自定义事件的内容。
     */
    public Object getPayload() {
        return payload;
    }

    /**
     * 设置自定义事件的内容。
     *
     * @param payload 自定义事件的内容，必须为 {@link WhiteObject} 子类，以保证数据格式正确。
     */
    public void setPayload(Object payload) {
        this.payload = payload;
    }
}
