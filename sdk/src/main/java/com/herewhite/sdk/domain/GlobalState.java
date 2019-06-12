package com.herewhite.sdk.domain;

/**
 * Created by buhe on 2018/8/11.
 */

public class GlobalState extends WhiteObject {
    private Integer currentSceneIndex;

    public int getCurrentSceneIndex() {
        return currentSceneIndex;
    }

    public void setCurrentSceneIndex(int currentSceneIndex) {
        this.currentSceneIndex = currentSceneIndex;
    }
}
