package com.herewhite.sdk.domain;

import com.google.gson.annotations.SerializedName;

/**
 * Created by buhe on 2018/8/11.
 */

/**
 * 视角模式。
 */
public enum ViewMode {
    /**
     * （默认）自由模式。
     * <p>
     * 该模式下用户可以主动调整视角，不受其他用户视角模式设置的影响，也不会影响其他用户的视角模式设置。
     *
     * @note 当房间内不存在视角为主播模式的用户时，所有用户的视角都默认为自由模式。
     */
    @SerializedName("freedom")
    Freedom,
    /**
     * 跟随模式。
     * <p>
     * 该模式下用户的视角会跟随主播的视角。
     *
     * @note
     * - 当一个用户的视角设置为主播模式后，房间内其他所有用户（包括新加入房间的用户）的视角会被自动设置为跟随模式。
     * - 跟随模式的用户进行白板操作时，其视角会自动切换为自由模式。
     * 如有需要，可以调用 {@link com.herewhite.sdk.Room#disableOperations(boolean disableOperations) disableOperations} 禁止用户操作，以锁定用户的视角模式。
     */
    @SerializedName("follower")
    Follower,
    /**
     * 主播模式。
     * 该模式下用户可以主动调整视角，并将自己的视角同步给房间内所有其他用户。
     *
     * @note - 每个房间只能有一个主播模式视角的用户。
     * - 当一个用户的视角设置为主播模式后，房间内所有其他用户（包括新加入房间的用户）的视角会被自动设置为跟随模式。
     */
    @SerializedName("broadcaster")
    Broadcaster
}
