package com.herewhite.sdk;

/**
 * 当动态 PPT 触发音视频操作时，如果
 */
public interface AudioMixerBridge {

    /**
     * 进行混音，在混音后，需要将混音结果通过 {@link AudioMixerImplement#setMediaState(long, long)} 传递给动态 ppt 内部。
     * @param filepath 文件路径，可以是本地文件或者网络地址
     * @param loopback true 则音频不通过 rtc 传播
     * @param replace true 则只播文件声音，不播麦克风声音，false 则是将文件和麦克风混音
     * @param cycle 循环播放文件的次数，-1 是无限循环
     */
    void startAudioMixing(String filepath, boolean loopback, boolean replace, long cycle);

    /**
     * 停止混音
     */
    void stopAudioMixing();

    /**
     * 设置混音文件的播放进度，相当于对混音源文件进行 seek 操作
     * @param position 播放进度参数
     */
    void setAudioMixingPosition(int position);
}
