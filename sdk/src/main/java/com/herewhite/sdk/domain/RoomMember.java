package com.herewhite.sdk.domain;

/**
 * Created by buhe on 2018/8/13.
 */

public class RoomMember {
    private Long memberId;
    private MemberInformation information;

    /**
     * 用户的所有教具信息
     *
     * @return 教具信息
     * @see MemberState
     * @since 2.4.7
     */
    public MemberState getMemberState() {
        return memberState;
    }

    private MemberState memberState;

    /**
     * 在加入房间时，带入的用户信息，可以为任意内容,建议各端传入字典。key-value 形式的 payload 会由 Gson 自动转成Map
     *
     * @return 用户 payload
     * @since 2.4.7
     */
    public Object getPayload() {
        return payload;
    }

    private Object payload;

    /**
     * 在白板内部对应的用户自增 id，从 1 开始计算。（0 为 admin，已被占用）
     *
     * @return 内部用户 id
     */
    public Long getMemberId() {
        return memberId;
    }

    /**
     * 返回当前用户的教具类型
     *
     * @deprecated 请使用 {@link #getMemberState()} 获取教具详细信息
     * @return 教具名称
     * @since 2.4.7
     */
    @Deprecated
    public String getCurrentApplianceName() {
        return memberState.getCurrentApplianceName();
    }

    /**
     *
     * 获取用户信息（加入房间时，自带内容）
     *
     * @deprecated 请使用 {@link #getPayload()} 获取用户信息
     * @return the information
     */
    public MemberInformation getInformation() {
        return information;
    }

}
