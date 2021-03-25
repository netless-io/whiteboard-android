package com.herewhite.sdk;

import android.webkit.JavascriptInterface;

import org.json.JSONException;
import org.json.JSONObject;

class RtcJsInterfaceImpl {
    private AudioMixerBridge mixerBridge;

    RtcJsInterfaceImpl(AudioMixerBridge mixerBridge) {
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
    public void setAudioMixingPosition(Object args) {
        if (this.mixerBridge != null) {
            int pos = Integer.valueOf((Integer) args);
            this.mixerBridge.setAudioMixingPosition(pos);
        }
    }
}
