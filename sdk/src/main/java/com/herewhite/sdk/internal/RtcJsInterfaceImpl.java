package com.herewhite.sdk.internal;

import android.webkit.JavascriptInterface;

import com.herewhite.sdk.AudioMixerBridge;

import org.json.JSONException;
import org.json.JSONObject;

public class RtcJsInterfaceImpl {
    private AudioMixerBridge mixerBridge;

    public RtcJsInterfaceImpl(AudioMixerBridge mixerBridge) {
        this.mixerBridge = mixerBridge;
    }

    @JavascriptInterface
    public void startAudioMixing(Object args) {
        if (this.mixerBridge != null && args instanceof JSONObject) {
            JSONObject jsonObject = (JSONObject) args;
            try {
                String filePath = jsonObject.getString("filePath");
                boolean loopback = jsonObject.getBoolean("loopback");
                boolean replace = jsonObject.getBoolean("replace");
                int cycle = jsonObject.getInt("cycle");
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
