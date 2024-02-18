package com.herewhite.demo.test.window;

import android.view.View;

import com.herewhite.demo.common.DemoAPI;
import com.herewhite.demo.common.SampleBaseActivity;
import com.herewhite.demo.databinding.ActivityWindowFullscreenBinding;
import com.herewhite.sdk.AbstractRoomCallbacks;
import com.herewhite.sdk.RoomParams;
import com.herewhite.sdk.WhiteSdkConfiguration;
import com.herewhite.sdk.domain.RoomState;
import com.herewhite.sdk.domain.WindowAppParam;
import com.herewhite.sdk.domain.WindowDocsEvent;
import com.herewhite.sdk.domain.WindowParams;

public class WindowFullscreenActivity extends SampleBaseActivity {

    private ActivityWindowFullscreenBinding binding;

    @Override
    protected View getContentView() {
        binding = ActivityWindowFullscreenBinding.inflate(getLayoutInflater());
        return binding.getRoot();
    }

    @Override
    protected void initView() {
        mRoomCallbackHock = new AbstractRoomCallbacks() {
            @Override
            public void onRoomStateChanged(RoomState modifyState) {

            }
        };

        binding.insertPpt.setOnClickListener(v -> {
            String prefixUrl = "https://convertcdn.netless.link/dynamicConvert";
            String taskUuid = "e6bc867b6cf84618b3bd5102ab47674a";
            WindowAppParam param = WindowAppParam.createSlideApp(taskUuid, prefixUrl, "Projector App");
            room.addApp(param, null);
        });

        binding.prevPage.setOnClickListener(v -> {
            room.dispatchDocsEvent(WindowDocsEvent.PrevPage, null);
        });

        binding.nextPage.setOnClickListener(v -> {
            room.dispatchDocsEvent(WindowDocsEvent.NextPage, null);
        });

        binding.prevStep.setOnClickListener(v -> {
            room.dispatchDocsEvent(WindowDocsEvent.PrevStep, null);
        });

        binding.nextStep.setOnClickListener(v -> {
            room.dispatchDocsEvent(WindowDocsEvent.NextStep, null);
        });

        binding.jumpTo.setOnClickListener(v -> {
            room.dispatchDocsEvent(WindowDocsEvent.JumpToPage(1), null);
        });
    }

    protected WhiteSdkConfiguration generateSdkConfig() {
        WhiteSdkConfiguration configuration = new WhiteSdkConfiguration(demoAPI.getAppId(), true);
        // 开启多窗口支持
        configuration.setUseMultiViews(true);
        return configuration;
    }

    @Override
    protected RoomParams generateRoomParams() {
        RoomParams roomParams = new RoomParams(uuid, token, DemoAPI.DEFAULT_UID);
        WindowParams windowParams = new WindowParams()
                // 设置显示比例，多端需要保证此参数一致
                .setContainerSizeRatio(9f / 16)
                // 启动全屏显示
                .setFullscreen(true);
        roomParams.setWindowParams(windowParams);
        return roomParams;
    }

    @Override
    protected void onJoinRoomSuccess() {

    }
}
