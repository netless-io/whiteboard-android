package com.herewhite.rtc.demo;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.herewhite.rtc.demo.databinding.ActivityMainRtcBinding;

/**
 * RTC Activity 代理接口
 * 用于抽象不同版本 RTC SDK 的实现
 */
public interface RtcActivityDelegate {

    void onCreate(AppCompatActivity activity, ActivityMainRtcBinding binding, Bundle savedInstanceState);

    void onDestroy();

    void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults);
}
