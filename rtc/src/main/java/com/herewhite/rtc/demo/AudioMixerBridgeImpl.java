package com.herewhite.rtc.demo;

import com.herewhite.sdk.AudioMixerBridge;

import io.agora.rtc.RtcEngine;

/**
 * 用户需要自己实现 rtc 混音逻辑
 */
public class AudioMixerBridgeImpl implements AudioMixerBridge {
    private RtcEngine rtcEngine;
    private ResultCallback callback;

    public AudioMixerBridgeImpl(RtcEngine rtcEngine, ResultCallback callback) {
        this.rtcEngine = rtcEngine;
        this.callback = callback;
    }

    @Override
    public void startAudioMixing(String filepath, boolean loopback, boolean replace, int cycle) {
        int code = rtcEngine.startAudioMixing(filepath, loopback, replace, cycle);
        if (code != 0) {
            returnResult(714, code);
        }
    }

    @Override
    public void stopAudioMixing() {
        int code = rtcEngine.stopAudioMixing();
        if (code != 0) {
            returnResult(0, code);
        }
    }

    @Override
    public void setAudioMixingPosition(int position) {
        int code = rtcEngine.setAudioMixingPosition(position);
        if (code != 0) {
            returnResult(0, code);
        }
    }

    @Override
    public void pauseAudioMixing() {
        int code = rtcEngine.pauseAudioMixing();
        if (code != 0) {
            returnResult(0, code);
        }
    }

    @Override
    public void resumeAudioMixing() {
        int code = rtcEngine.resumeAudioMixing();
        if (code != 0) {
            returnResult(0, code);
        }
    }

    private void returnResult(int state, int code) {
        callback.onResult(state, code);
    }

    public interface ResultCallback {
        void onResult(int state, int code);
    }
}
