package com.herewhite.sdk.domain;

public class PlayerConfiguration extends WhiteObject {
    private String room;
    private String roomToken;
    private String slice;
    private Long beginTimestamp;
    private Long duration;
    /*
    音频地址，暂不支持视频。
    Player 会自动与音视频播放做同步，保证同时播放，当一方缓冲时，会暂停。
    */
    private String audioUrl;

    public PlayerConfiguration(String room, String roomToken) {
        this.room = room;
        this.roomToken = roomToken;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public String getRoomToken() { return roomToken; }

    public void setRoomToken(String roomToken) { this.roomToken = roomToken; }

    public String getSlice() {
        return slice;
    }

    public void setSlice(String slice) {
        this.slice = slice;
    }

    public Long getBeginTimestamp() {
        return beginTimestamp;
    }

    public void setBeginTimestamp(Long beginTimestamp) {
        this.beginTimestamp = beginTimestamp;
    }

    public Long getDuration() {
        return duration;
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }

    public String getAudioUrl() {
        return audioUrl;
    }

    public void setAudioUrl(String audioUrl) {
        this.audioUrl = audioUrl;
    }
}
