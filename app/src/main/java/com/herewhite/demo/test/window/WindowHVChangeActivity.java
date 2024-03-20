package com.herewhite.demo.test.window;

import android.content.res.Configuration;
import android.view.KeyEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.herewhite.demo.R;
import com.herewhite.demo.common.SampleBaseActivity;
import com.herewhite.demo.databinding.ActivityWindowHvChangeBinding;
import com.herewhite.sdk.RoomParams;
import com.herewhite.sdk.WhiteSdkConfiguration;
import com.herewhite.sdk.domain.WindowParams;

public class WindowHVChangeActivity extends SampleBaseActivity {

    private ActivityWindowHvChangeBinding binding;

    @Override
    protected View getContentView() {
        binding = ActivityWindowHvChangeBinding.inflate(getLayoutInflater());
        return binding.getRoot();
    }

    @Override
    protected void initView() {
        binding.white.setAutoResize(false);
        binding.change.setOnClickListener(v -> {
            if (getRequestedOrientation() == android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                setRequestedOrientation(android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            } else {
                setRequestedOrientation(android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            binding.change.callOnClick();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        ConstraintLayout.LayoutParams lp = (ConstraintLayout.LayoutParams) binding.white.getLayoutParams();
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            lp.dimensionRatio = "H,16:9";
        } else {
            lp.dimensionRatio = "W,9:16";
        }
        binding.white.setLayoutParams(lp);

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            binding.change.setImageResource(R.drawable.off_screen);
        } else {
            binding.change.setImageResource(R.drawable.full_screen);
        }
    }

    protected WhiteSdkConfiguration generateSdkConfig() {
        WhiteSdkConfiguration configuration = new WhiteSdkConfiguration(demoAPI.getAppId(), true);
        // 开启多窗口支持
        configuration.setUseMultiViews(true);
        return configuration;
    }

    @Override
    protected RoomParams generateRoomParams() {
        RoomParams roomParams = super.generateRoomParams();
        WindowParams windowParams = new WindowParams();
        windowParams.setContainerSizeRatio(9f / 16);
        roomParams.setWindowParams(windowParams);
        return roomParams;
    }

    @Override
    protected void onJoinRoomSuccess() {

    }
}
