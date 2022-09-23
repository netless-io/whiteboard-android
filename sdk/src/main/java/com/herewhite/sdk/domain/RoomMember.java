package com.herewhite.sdk.domain;

/**
 * Created by buhe on 2018/8/13.
 */

/**
 * `RoomMember` 类，用于获取实时房间内互动模式（具有读写权限）用户信息。
 *
 * @note 该类仅适用于互动模式的用户。订阅模式的用户不属于房间成员。
 */
public class RoomMember {
    private Long memberId;
    private MemberInformation information;
    private MemberState memberState;
    private Object payload;

    /**
     * 获取互动模式用户的所有白板工具信息。
     *
     * @since 2.4.8
     *
     * @return 白板工具信息，详见 {@link MemberState MemberState}。
     */
    public MemberState getMemberState() {
        return memberState;
    }

    /**
     * 获取用户加入房间时携带的自定义用户信息。
     *
     * @since 2.4.8
     *
     */
    public Object getPayload() {
        return payload;
    }

    /**
     * 获取用户 ID。
     *
     * 在用户加入互动白板实时房间时，会自动分配用户 ID，用于标识房间内的用户。同一房间中的每个用户具有唯一的用户 ID。
     *
     * @return 用户 ID。
     */
    public Long getMemberId() {
        return memberId;
    }

    /**
     * 获取用户当前使用的白板工具。
     *
     * @since 2.4.8
     * @deprecated 该方法已废弃。请改用 {@link #getMemberState() getMemberState} 获取详细的白板工具信息。
     *
     * @return 白板工具名称。
     */
    @Deprecated
    public String getCurrentApplianceName() {
        return memberState.getCurrentApplianceName();
    }

    /**
     * 获取用户加入房间时携带的用户信息。
     *
     * @deprecated 该方法已经废弃。请改用 {@link #getPayload() getPayload} 获取用户信息。
     *
     * @return 用户信息。
     */
    public MemberInformation getInformation() {
        return information;
    }

}
