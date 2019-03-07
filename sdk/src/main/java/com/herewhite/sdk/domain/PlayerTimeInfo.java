package com.herewhite.sdk.domain;

public class PlayerTimeInfo {


    private long scheduleTime;

    /**
     * 总时长
     */
    private long timeDuration;

    /**
     * 一个回访中，含有的总 frame 数
     */
    private int framesCount;

    /**
     * 开始时间，UTC 时间戳
     */
    private long beginTimestamp;

    public long getScheduleTime() {
        return scheduleTime;
    }

    public void setScheduleTime(long scheduleTime) {
        this.scheduleTime = scheduleTime;
    }

    public long getTimeDuration() {
        return timeDuration;
    }

    public void setTimeDuration(long timeDuration) {
        this.timeDuration = timeDuration;
    }

    public int getFramesCount() {
        return framesCount;
    }

    public void setFramesCount(int framesCount) {
        this.framesCount = framesCount;
    }

    public long getBeginTimestamp() {
        return beginTimestamp;
    }

    public void setBeginTimestamp(long beginTimestamp) {
        this.beginTimestamp = beginTimestamp;
    }
}
