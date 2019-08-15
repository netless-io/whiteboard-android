package com.herewhite.sdk.domain;

public class PlayerTimeInfo {

    private long scheduleTime;
    private long timeDuration;
    private int framesCount;
    private long beginTimestamp;

    public PlayerTimeInfo(long scheduleTime, long timeDuration, int framesCount, long beginTimestamp) {
        this.scheduleTime = scheduleTime;
        this.timeDuration = timeDuration;
        this.framesCount = framesCount;
        this.beginTimestamp = beginTimestamp;
    }

    /**
     * 当前时间进度（毫秒）
     */
    public long getScheduleTime() {
        return scheduleTime;
    }
    /**
     * 总时长(毫秒）
     */
    public long getTimeDuration() {
        return timeDuration;
    }

    public int getFramesCount() {
        return framesCount;
    }

    /**
     * 开始时间，UTC 时间戳（毫秒）
     */
    public long getBeginTimestamp() {
        return beginTimestamp;
    }
}