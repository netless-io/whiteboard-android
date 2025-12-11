package com.herewhite.rtc.demo.rtc3;

import android.util.Log;

import com.herewhite.sdk.AudioEffectBridge;

import io.agora.rtc.IAudioEffectManager;
import io.agora.rtc.RtcEngine;

public class AudioEffectBridgeRtc3Impl implements AudioEffectBridge {
    private String TAG = "AgoraAudioEffectBridge";

    private final IAudioEffectManager audioEffectManager;

    public AudioEffectBridgeRtc3Impl(RtcEngine rtcEngine) {
        this.audioEffectManager = rtcEngine.getAudioEffectManager();
    }

    @Override
    public double getEffectsVolume() {
        return audioEffectManager.getEffectsVolume();
    }

    @Override
    public int setEffectsVolume(double volume) {
        Log.i(TAG, "setEffectsVolume: " + volume);
        return audioEffectManager.setEffectsVolume(volume);
    }

    @Override
    public int setVolumeOfEffect(int soundId, double volume) {
        Log.i(TAG, "setVolumeOfEffect: " + soundId + " " + volume);
        return audioEffectManager.setVolumeOfEffect(soundId, volume);
    }

    @Override
    public int playEffect(int soundId, String filePath, int loopCount, double pitch, double pan, double gain, boolean publish, int startPos) {
        Log.i(TAG, "playEffect: " + soundId + " " + filePath + " " + loopCount + " " + pitch + " " + pan + " " + gain + " " + publish + " " + startPos);
        return audioEffectManager.playEffect(soundId, filePath, loopCount, pitch, pan, gain, publish, startPos);
    }

    @Override
    public int stopEffect(int soundId) {
        Log.i(TAG, "stopEffect: " + soundId);
        return audioEffectManager.stopEffect(soundId);
    }

    @Override
    public int stopAllEffects() {
        Log.i(TAG, "stopAllEffects: ");
        return audioEffectManager.stopAllEffects();
    }

    @Override
    public int preloadEffect(int soundId, String filePath, int startPos) {
        Log.i(TAG, "preloadEffect: " + soundId + " " + filePath + " " + startPos);
        return audioEffectManager.preloadEffect(soundId, filePath);
    }

    @Override
    public int unloadEffect(int soundId) {
        Log.i(TAG, "unloadEffect: " + soundId);
        return audioEffectManager.unloadEffect(soundId);
    }

    @Override
    public int pauseEffect(int soundId) {
        Log.i(TAG, "pauseEffect: " + soundId);
        return audioEffectManager.pauseEffect(soundId);
    }

    @Override
    public int pauseAllEffects() {
        Log.i(TAG, "pauseAllEffects: ");
        return audioEffectManager.pauseAllEffects();
    }

    @Override
    public int resumeEffect(int soundId) {
        Log.i(TAG, "resumeEffect: " + soundId);
        return audioEffectManager.resumeEffect(soundId);
    }

    @Override
    public int resumeAllEffects() {
        Log.i(TAG, "resumeAllEffects: ");
        return audioEffectManager.resumeAllEffects();
    }

    @Override
    public int getEffectDuration(String filePath) {
        Log.i(TAG, "getEffectDuration: " + filePath);
        return audioEffectManager.getEffectDuration(filePath);
    }

    @Override
    public int setEffectPosition(int soundId, int pos) {
        Log.i(TAG, "setEffectPosition: " + soundId + " " + pos);
        return audioEffectManager.setEffectPosition(soundId, pos);
    }

    @Override
    public int getEffectCurrentPosition(int soundId) {
        // Log.i(TAG, "getEffectCurrentPosition: " + soundId);
        return audioEffectManager.getEffectCurrentPosition(soundId);
    }
}
