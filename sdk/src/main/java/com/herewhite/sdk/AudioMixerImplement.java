package com.herewhite.sdk;

import android.webkit.JavascriptInterface;

import org.json.JSONException;
import org.json.JSONObject;

public class AudioMixerImplement {

    AudioMixerImplement(WhiteboardView bridge, AudioMixerBridge mixerBridge) {
        this.bridge = bridge;
        this.mixerBridge = mixerBridge;
    }

    /**
     * 混音 API 完成后的状态回调
     * @param state 混音状态
     *  710: 成功调用 startAudioMixing 或 resumeAudioMixing
     *  711: 成功调用 pauseAudioMixing
     *  713: 成功调用 stopAudioMixing
     *  714: 播放失败，error code 会有具体原因,
     * @param errorCode 当播放失败时，该值有意义
     */
    public void setMediaState(long state, long errorCode) {
        this.bridge.callHandler("rtc.callback", new Object[]{state, errorCode});
    }

    private WhiteboardView bridge;
    private AudioMixerBridge mixerBridge;

    @JavascriptInterface
    public void startAudioMixing(Object args) {
        if (this.mixerBridge != null && args instanceof JSONObject) {
            JSONObject jsonObject = (JSONObject)args;
            try {
                String filePath = jsonObject.getString("filePath");
                boolean loopback = jsonObject.getBoolean("loopback");
                boolean replace = jsonObject.getBoolean("replace");
                long cycle = jsonObject.getLong("cycle");
                this.mixerBridge.startAudioMixing(filePath, loopback, replace, cycle);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @JavascriptInterface
    public void stopAudioMixing(Object args) {
        if (this.mixerBridge != null) {
            this.mixerBridge.stopAudioMixing();
        }
    }

    @JavascriptInterface
    public void pauseAudioMixing(Object args) {
        if (this.mixerBridge != null) {
            this.mixerBridge.pauseAudioMixing();
        }
    }

    @JavascriptInterface
    public void resumeAudioMixing(Object args) {
        if (this.mixerBridge != null) {
            this.mixerBridge.resumeAudioMixing();
        }
    }

    @JavascriptInterface
    public void setAudioMixingPosition(Object args) {
        if (this.mixerBridge != null) {
            int pos = Integer.valueOf((Integer) args);
            this.mixerBridge.setAudioMixingPosition(pos);
        }
    }
}
