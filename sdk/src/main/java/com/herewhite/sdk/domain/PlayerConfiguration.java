package com.herewhite.sdk.domain;

import java.util.concurrent.TimeUnit;

public class PlayerConfiguration extends WhiteObject {
    private String room;
    private String roomToken;
    private String slice;
    private Long beginTimestamp;
    private Long duration;
    private CameraBound cameraBound;
    private Long step = 500L;

    public Region getRegion() {
        return region;
    }

    /**
     * 类似 {@link com.herewhite.sdk.RoomParams#setRegion(Region)}
     * @param region
     */
    public void setRegion(Region region) {
        this.region = region;
    }

    private Region region;

    /**
     * player 初始化方法
     * @param room 需要回放的房间 uuid
     * @param roomToken 房间 roomToken
     */
    public PlayerConfiguration(String room, String roomToken) {
        this.room = room;
        this.roomToken = roomToken;
    }

    public CameraBound getCameraBound() {
        return cameraBound;
    }

    /**
     * {@link com.herewhite.sdk.Room#setCameraBound(CameraBound)}
     * @param cameraBound
     */
    public void setCameraBound(CameraBound cameraBound) {
        this.cameraBound = cameraBound;
    }

    /**
     * 回放时，时间进度的调用频率
     * @param duration 时长长度
     * @param timeUnit 时间单位
     */
    public void setStep(Long duration, TimeUnit timeUnit) {
        this.step = TimeUnit.MILLISECONDS.convert(duration, timeUnit);
    }

    /*
      音频地址，暂不支持视频。
      Player 会自动与音视频播放做同步，保证同时播放，当一方缓冲时，会暂停。
    */
    private String mediaURL;

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public String getRoomToken() {
        return roomToken;
    }

    public void setRoomToken(String roomToken) {
        this.roomToken = roomToken;
    }

    /**
     * 文档中隐藏
     * @return
     */
    public String getSlice() {
        return slice;
    }

    /**
     * 文档中隐藏
     * @return
     */
    public void setSlice(String slice) {
        this.slice = slice;
    }

    public Long getBeginTimestamp() {
        return beginTimestamp;
    }

    /***
     * 回放房间的起始 UTC 时间戳(毫秒）
     * 比如，想要回放 Wed Mar 10 2021 18:03:34 GMT+0800 (中国标准时间) 的话，需要传入 1615370614269
     * @param beginTimestamp
     */
    public void setBeginTimestamp(Long beginTimestamp) {
        this.beginTimestamp = beginTimestamp;
    }

    public Long getDuration() {
        return duration;
    }

    /**
     * 设置持续时长（毫秒）
     * @param duration
     */
    public void setDuration(Long duration) {
        this.duration = duration;
    }

    public String getMediaURL() {
        return mediaURL;
    }

    public void setMediaURL(String mediaURL) {
        this.mediaURL = mediaURL;
    }
}
