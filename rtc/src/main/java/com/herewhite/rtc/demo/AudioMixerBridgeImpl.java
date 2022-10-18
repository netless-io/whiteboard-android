package com.herewhite.rtc.demo;

import android.util.Log;

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
        Log.d("AudioMixerBridgeImpl", "startAudioMixing");
        int code = rtcEngine.startAudioMixing(filepath, loopback, replace, cycle);
        if (code != 0) {
            onMediaStateChanged(714, code);
        }
    }

    @Override
    public void stopAudioMixing() {
        Log.d("AudioMixerBridgeImpl", "stopAudioMixing");
        int code = rtcEngine.stopAudioMixing();
        if (code != 0) {
            onMediaStateChanged(0, code);
        }
    }

    @Override
    public void setAudioMixingPosition(int position) {
        Log.d("AudioMixerBridgeImpl", "setAudioMixingPosition " + position);
        int code = rtcEngine.setAudioMixingPosition(position);
        if (code != 0) {
            onMediaStateChanged(0, code);
        }
    }

    @Override
    public void pauseAudioMixing() {
        Log.d("AudioMixerBridgeImpl", "pauseAudioMixing");
        int code = rtcEngine.pauseAudioMixing();
        if (code != 0) {
            onMediaStateChanged(0, code);
        }
    }

    @Override
    public void resumeAudioMixing() {
        Log.d("AudioMixerBridgeImpl", "resumeAudioMixing");
        int code = rtcEngine.resumeAudioMixing();
        if (code != 0) {
            onMediaStateChanged(0, code);
        }
    }

    private void onMediaStateChanged(int state, int code) {
        callback.onMediaStateChanged(state, code);
    }

    public interface ResultCallback {
        void onMediaStateChanged(int state, int code);
    }
}
