package com.herewhite.sdk;

/**
 * `AudioMixerImplement` 类，用于实现 `AudioMixerBridge` 接口。
 */
public class AudioEffectImplement {
    private final JsBridgeInterface bridge;

    /**
     * 构造函数
     * @param bridge JS 交互桥接对象
     */
    AudioEffectImplement(JsBridgeInterface bridge) {
        this.bridge = bridge;
    }

    /**
     * 通知白板音乐播放已结束。
     * @param soundId 播放的音乐 ID。
     */
    public void setEffectFinished(int soundId) {
        bridge.callHandler("rtc.setEffectFinished", new Object[]{soundId});
    }

    /**
     * 设置音乐文件播放状态。
     * <p>
     * 你需要在 Agora RTC SDK 触发的 {@code rtcEngine.onAudioEffectStateChanged} 回调中调用该方法，
     * 将音乐文件播放状态传递给白板中的 PPT。PPT 根据收到的音频播放状态判断是否显示画面，以确保音画同步。
     * </p>
     * <strong>注意：</strong>如果你使用的实时音视频 SDK 没有该状态回调方法，会导致播放的 PPT 音画不同步。
     * @param soundId 播放的音乐 ID。
     * @param state   音乐文件播放状态：
     * <ul>
     * <li>810：开始播放音乐</li>
     * <li>811：音乐被暂停</li>
     * <li>813：音乐被停止</li>
     * <li>814：音乐播放失败</li>
     * </ul>
     */
    public void setEffectSoundId(int soundId, int state) {
        bridge.callHandler("rtc.audioEffectCallback", new Object[]{soundId, state});
    }

    /**
     * 更新音频文件的总时长。
     * @param filePath 音乐文件的路径或 URL。
     * @param duration 音乐长度，单位为毫秒 (ms)。
     */
    public void setEffectDurationUpdate(String filePath, int duration) {
        bridge.callHandler("rtc.effectDurationCallback", new Object[]{filePath, duration});
    }
}