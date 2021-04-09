package com.herewhite.sdk.domain;

/**
 * 白板回放的播放时间信息。
 */
// TODO: 大版本更新时，scheduleTime 这种实时数据，应该拆分独立到其他类中。当前类应该改名。
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
     * 获取当前的回放进度。
     *
     * @return 当前的回放进度，单位为毫秒。
     */
    public long getScheduleTime() {
        return scheduleTime;
    }

    /**
     * 获取回放的总时长。
     *
     * @return 回放的总时长，单位为毫秒。
     */
    public long getTimeDuration() {
        return timeDuration;
    }

    /**
     * 文档中隐藏
     */
    public int getFramesCount() {
        return framesCount;
    }

    /**
     * 获取白板回放的起始时间。
     *
     * 该方法会返回单位为毫秒的 Unix 时间戳，你需要自行转换为 UTC 时间。例如，如果返回 `1615370614269`，表示的 UTC 时间为 2021-03-10 18:03:34 GMT+0800。
     *
     * @return Unix 时间戳，单位为毫秒。
     */
    public long getBeginTimestamp() {
        return beginTimestamp;
    }
}
