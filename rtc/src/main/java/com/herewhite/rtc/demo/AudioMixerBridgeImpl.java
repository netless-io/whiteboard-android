package com.herewhite.rtc.demo;

import android.util.Log;

import com.herewhite.sdk.AudioMixerBridge;

import io.agora.rtc.RtcEngine;

/**
 * 用户需要自己实现 rtc 混音逻辑
 */
public class AudioMixerBridgeImpl implements AudioMixerBridge {
    private RtcEngine rtcEngine;

    public AudioMixerBridgeImpl(RtcEngine rtcEngine) {
        this.rtcEngine = rtcEngine;
    }

    @Override
    public void startAudioMixing(String filepath, boolean loopback, boolean replace, int cycle) {
        rtcEngine.startAudioMixing(filepath, loopback, replace, cycle);
    }

    @Override
    public void stopAudioMixing() {
        Log.i("rtc stop ", "----");
        rtcEngine.stopAudioMixing();
    }

    @Override
    public void setAudioMixingPosition(int position) {
        rtcEngine.setAudioMixingPosition(position);
    }
}
