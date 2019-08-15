package com.herewhite.sdk.domain;

public class WhiteDisplayerState extends WhiteObject {

    /**
     * 全局状态，所有用户可见，实时房间时，可读可写；回放房间只读。返回内容为 sdk 默认全局状态
     *
     * @return 全局状态
     */
    public GlobalState getGlobalState() {
        return globalState;
    }

    /**
     * 设置全局状态，可以传入自定义 GlobalState 子类，其中字段会加入到房间全局状态中
     *
     * @param globalState the global state
     */
    public void setGlobalState(GlobalState globalState) {
        this.globalState = globalState;
    }

    /**
     * 获取房间中所有用户列表
     *
     * @return 用户成员状态列表 [ ]
     * @see RoomMember
     */
    public RoomMember[] getRoomMembers() {
        return roomMembers;
    }

    /**
     * 获取当前场景目录下的场景状态
     *
     * @return 当前场景目录下的场景状态
     * @see SceneState
     */
    public SceneState getSceneState() {
        return sceneState;
    }

    private GlobalState globalState;
    private RoomMember[] roomMembers;
    private SceneState sceneState;

}
