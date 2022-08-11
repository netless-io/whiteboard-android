package com.herewhite.rtc.demo;

import com.herewhite.sdk.AudioMixerBridge;
import com.herewhite.sdk.WhiteSdk;

import io.agora.rtc.RtcEngine;

/**
 * 用户需要自己实现 rtc 混音逻辑
 */
public class AudioMixerBridgeImpl implements AudioMixerBridge {
    private RtcEngine rtcEngine;
    private WhiteSdk whiteSdk;

    public AudioMixerBridgeImpl(RtcEngine rtcEngine, WhiteSdk whiteSdk) {
        this.rtcEngine = rtcEngine;
        this.whiteSdk = whiteSdk;
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
        if (whiteSdk.getAudioMixerImplement() != null) {
            whiteSdk.getAudioMixerImplement().setMediaState(state, code);
        }
    }

}
