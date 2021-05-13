package com.herewhite.sdk;

/**
 * `AudioMixerImplement` 类，用于实现混音。
 *
 * @note 该类基于 Agora RTC SDK 的混音方法设计，如果你使用的实时音视频 SDK 不是 Agora RTC SDK，但也具有混音接口和混音状态回调，你也可以调用该类中的方法。
 */
public class AudioMixerImplement {
    private final JsBridgeInterface bridge;

    AudioMixerImplement(JsBridgeInterface bridge) {
        this.bridge = bridge;
    }

    /**
     * 设置音乐文件播放状态。
     * <p>
     * 你需要在 Agora RTC SDK 触发的 `onAudioMixingStateChanged` 回调中调用该方法，将音乐文件播放状态传递给白板中的 PPT。
     * PPT 根据收到的音频播放状态判断是否显示画面，以确保音画同步。
     *
     * @note 如果你使用的实时音视频 SDK 没有混音状态回调方法，会导致播放的 PPT音画不同步。
     *
     * @param state     音乐文件播放状态：
     *                  - `MEDIA_ENGINE_AUDIO_EVENT_MIXING_PLAY(710)`: RTC SDK 成功调用 `startAudioMixing` 播放音乐文件或 `resumeAudioMixing` 恢复播放音乐文件。
     *                  - `MEDIA_ENGINE_AUDIO_EVENT_MIXING_PAUSED(711)`：RTC SDK 成功调用 `pauseAudioMixing` 暂停播放音乐文件。
     *                  - `MEDIA_ENGINE_AUDIO_EVENT_MIXING_STOPPED(713)`：RTC SDK 成功调用 `stopAudioMixing` 停止播放音乐文件。
     *                  - `MEDIA_ENGINE_AUDIO_EVENT_MIXING_ERROR(714)`：音乐文件播放失败。SDK 会在 `errorCode` 参数中返回具体的报错原因。
     * @param errorCode 音乐文件播放失败的原因：
     *                  - `MEDIA_ENGINE_AUDIO_ERROR_MIXING_OPEN(701)`：音乐文件打开出错。
     *                  - `MEDIA_ENGINE_AUDIO_ERROR_MIXING_TOO_FREQUENT(702)`：音乐文件打开太频繁。
     *                  - `MEDIA_ENGINE_AUDIO_EVENT_MIXING_INTERRUPTED_EOF(703)`：音乐文件播放异常中断。
     *
     */
    public void setMediaState(long state, long errorCode) {
        bridge.callHandler("rtc.callback", new Object[]{state, errorCode});
    }
}
