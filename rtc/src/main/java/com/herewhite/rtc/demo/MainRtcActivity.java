package com.herewhite.rtc.demo;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.herewhite.rtc.demo.databinding.ActivityMainRtcBinding;

/**
 * RTC Activity 入口类
 * 通过 BuildConfig.RTC_VERSION 配置选择使用 3.x 或 4.x 版本的 RTC SDK
 */
public class MainRtcActivity extends AppCompatActivity {
    private static final String TAG = "MainRtcActivity";

    private ActivityMainRtcBinding binding;
    private RtcActivityDelegate delegate;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainRtcBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 根据配置创建对应版本的代理
        delegate = createDelegate();

        binding.rtcVersion.setText("RTC:" + BuildConfig.RTC_VERSION);
        binding.exit.setOnClickListener(v -> finish());

        // 如果用户需要用到 rtc 混音功能来解决回声和声音抑制问题，那么必须要在 whiteSDK 之前初始化 rtcEngine
        delegate.onCreate(this, binding, savedInstanceState);
    }

    /**
     * 根据 BuildConfig.RTC_VERSION 配置创建对应版本的代理
     * 使用反射避免编译时依赖问题
     * "3" -> RtcActivityDelegate3x (RTC SDK 3.7.1)
     * "4" -> RtcActivityDelegate4x (RTC SDK 4.3.2)
     */
    private RtcActivityDelegate createDelegate() {
        String className;
        if ("4".equals(BuildConfig.RTC_VERSION)) {
            className = "com.herewhite.rtc.demo.rtc4.Rtc4ActivityDelegate";
        } else {
            className = "com.herewhite.rtc.demo.rtc3.Rtc3ActivityDelegate";
        }

        try {
            Class<?> clazz = Class.forName(className);
            return (RtcActivityDelegate) clazz.newInstance();
        } catch (Exception e) {
            Log.e(TAG, "Failed to create delegate: " + className, e);
            throw new RuntimeException("Failed to create RtcActivityDelegate: " + className, e);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        delegate.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        delegate.onDestroy();
    }
}
