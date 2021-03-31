package com.herewhite.sdk.domain;

/**
 * Created by buhe on 2018/8/13.
 */

/**
 * `RoomMember` 类，用于获取实时房间内互动模式（具有读写权限）用户信息。
 */
public class RoomMember {
    private Long memberId;
    private MemberInformation information;

    /**
     * 获取互动模式用户的所有教具信息。
     *
     * @return 教具信息，详见 {@link MemberState MemberState}。
     * @since 2.4.8
     */
    public MemberState getMemberState() {
        return memberState;
    }

    private MemberState memberState;

    /**
     * 获取用户加入房间时携带的自定义用户信息。
     *
     * @return 自定义用户信息。
     * @since 2.4.8
     * <p>
     * 自定义用户信息需要在调用 `RoomParams` 初始化房间参数时，通过 `userPayload` 参数传入。`userPayload` 内容可以自定义，格式最好为 `key-value` 形式的字典结构。key-value 形式的 `userPayload` 会由 Gson 自动转成 Map。
     */
    public Object getPayload() {
        return payload;
    }

    private Object payload;

    /**
     * 获取用户 ID。
     * <p>
     * 在用户加入互动白板实时房间时，会自动分配用户 ID，用于标识房间内的用户。同一房间中的每个用户具有唯一的用户 ID。
     *
     * @return 用户 ID。
     */
    public Long getMemberId() {
        return memberId;
    }

    /**
     * 获取用户当前使用的教具。
     *
     * @return 教具名称。
     * @since 2.4.8
     * @deprecated 该方法已废弃。请改用 {@link #getMemberState() getMemberState} 获取详细的教具信息。
     */
    @Deprecated
    public String getCurrentApplianceName() {
        return memberState.getCurrentApplianceName();
    }

    /**
     * 获取用户加入房间时携带的用户信息。
     *
     * @return 用户信息。
     * @deprecated 该方法已经废弃。请改用 {@link #getPayload() getPayload} 获取用户信息。
     */
    public MemberInformation getInformation() {
        return information;
    }

}
