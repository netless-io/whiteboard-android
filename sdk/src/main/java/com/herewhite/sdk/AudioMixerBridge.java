package com.herewhite.sdk;

/**
 * `AudioMixerBridge` 接口，用于桥接 RTC SDK 的混音方法和白板 SDK。
 *
 * @note 仅当用户使用的 RTC SDK 支持混音方法时，该方法才会生效。
 * @since 2.9.15
 * <p>
 * 当用户同时使用音视频功能和互动白板，且在互动白板中展示的动态 PPT 包含音频文件时，可能遇到以下问题：
 * - 播放 PPT 内的音频时声音很小。
 * - 播放 PPT 内的音频时有回声。
 * <p>
 * 为解决上述问题，你可以使用 `AudioMixerBridge` 接口，以调用 RTC SDK 的混音方法播放动态 PPT 中的音频文件。
 */
public interface AudioMixerBridge {

    /**
     * 开始播放音乐文件及混音。
     * <p>
     * 进行混音后，需要将混音结果通过 {@link AudioMixerImplement#setMediaState(long, long)} 传递给动态 PPT 内部。
     *
     * @param filepath 指定需要混音的本地或在线音频文件的绝对路径。
     * @param loopback 是否只有本地用户可以听到混音后的音频流：
     *                 - `true`：只有本地可以听到混音的音频流。
     *                 - `false`：本地和对方都可以听到混音的音频流。
     * @param replace  是否播放麦克风采集的音频：
     *                 - `true`： 只播音频文件，不播麦克风采集的音频。
     *                 - `false`: 将音频文件和麦克风采集的音频混音。
     * @param cycle    音频文件循环播放的次数：
     *                 - 正整数：循环的次数。
     *                 - -1：无限循环。
     */
    void startAudioMixing(String filepath, boolean loopback, boolean replace, int cycle);

    /**
     * 停止播放音乐文件及混音。
     */
    void stopAudioMixing();

    /**
     * 设置音乐文件的播放位置。
     *
     * @param position 整数。进度条位置，单位为毫秒。
     */
    void setAudioMixingPosition(int position);
}
