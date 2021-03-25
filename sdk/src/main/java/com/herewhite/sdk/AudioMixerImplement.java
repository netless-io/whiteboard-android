package com.herewhite.sdk;

public class AudioMixerImplement {
    private final JsBridgeInterface bridge;

    AudioMixerImplement(JsBridgeInterface bridge) {
        this.bridge = bridge;
    }

    /**
     * 混音 API 完成后的状态回调
     *
     * @param state     混音状态
     *                  710: 成功调用 startAudioMixing 或 resumeAudioMixing
     *                  711: 成功调用 pauseAudioMixing
     *                  713: 成功调用 stopAudioMixing
     *                  714: 播放失败，error code 会有具体原因,
     * @param errorCode 当播放失败时，该值有意义
     */
    public void setMediaState(long state, long errorCode) {
        bridge.callHandler("rtc.callback", new Object[]{state, errorCode});
    }
}
