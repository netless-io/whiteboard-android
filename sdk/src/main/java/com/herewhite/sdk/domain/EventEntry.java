package com.herewhite.sdk.domain;

/**
 * `EventEntry` 类。该类包含 SDK 可以触发的自定义事件回调。
 */
public class EventEntry extends WhiteObject {
    private String eventName;
    private Object payload;
    private String scope;
    private long authorId;

    /**
     * 文档中隐藏
     * 对外能修改，用户只应该查看 eventName 和 payload
     */
    public String getScope() {
        return scope;
    }

    /**
     * 获取事件触发者的用户 ID。
     * <p>
     * 若是系统事件，则为 `AdminObserverId`。
     *
     * @return 事件触发者的用户 ID。
     */
    public long getAuthorId() {
        return authorId;
    }

    /**
     * 获取回调事件的名称。
     *
     * @return 回调事件名称。
     */
    public String getEventName() {
        return eventName;
    }

    /**
     * 获取回调事件的内容。
     *
     * @return 回调事件内容。
     */
    public Object getPayload() {
        return payload;
    }
}
