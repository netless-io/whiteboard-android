package com.herewhite.sdk.domain;

/**
 * Created by buhe on 2018/8/13.
 */

/**
 * 文档中隐藏
 */
public class FrameError extends WhiteObject {

    private Long userId;
    private String error;

    public FrameError(long userId, String error) {
        this.userId = userId;
        this.error = error;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getError() {
        return error;
    }

    /**
     * @param error
     */
    public void setError(String error) {
        this.error = error;
    }
}
