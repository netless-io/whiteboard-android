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
     * 在加入房间时，带入的用户信息，可以为任意内容,建议各端传入字典。
     *
     * @return 用户 payload
     * @since 2.4.7
     */
    public Object getPayload() {
        return payload;
    }

    private Object payload;

    /**
     * 在白板内部对应的用户自增 id。
     *
     * @return 内部用户 id
     */
    public long getMemberId() {
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

    public MemberInformation getInformation() {
        return information;
    }

}
