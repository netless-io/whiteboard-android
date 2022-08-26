package com.herewhite.demo.test;

import android.view.View;

import com.herewhite.demo.common.SampleBaseActivity;
import com.herewhite.demo.databinding.ActivityCameraBoundBinding;
import com.herewhite.sdk.domain.AnimationMode;
import com.herewhite.sdk.domain.Appliance;
import com.herewhite.sdk.domain.CameraBound;
import com.herewhite.sdk.domain.CameraConfig;
import com.herewhite.sdk.domain.ContentModeConfig;
import com.herewhite.sdk.domain.ImageInformationWithUrl;
import com.herewhite.sdk.domain.MemberState;

public class CameraBoundActivity extends SampleBaseActivity {

    ActivityCameraBoundBinding binding;

    @Override
    protected View getContentView() {
        binding = ActivityCameraBoundBinding.inflate(getLayoutInflater());
        return binding.getRoot();
    }

    @Override
    protected void initView() {
        binding.insertImage.setOnClickListener(v -> {
            double IMAGE_WIDTH = 2360;
            double IMAGE_HEIGHT = 1640;

            // 1: 插入图片到原点
            ImageInformationWithUrl image = new ImageInformationWithUrl(
                    0.0,
                    0.0,
                    IMAGE_WIDTH,
                    IMAGE_HEIGHT,
                    "https://flat-storage.oss-accelerate.aliyuncs.com/cloud-storage/2022-08/26/e068ac3d-0e54-41bf-b42f-646dc5d9e4af/e068ac3d-0e54-41bf-b42f-646dc5d9e4af.jpeg"
            );
            room.insertImage(image);

            // 2: 设置工具为抓手
            MemberState memberState = new MemberState();
            memberState.setCurrentApplianceName(Appliance.HAND);
            room.setMemberState(memberState);

            // 3: 设置视角边界
            ContentModeConfig minContentConfig = new ContentModeConfig();
            minContentConfig.setMode(ContentModeConfig.ScaleMode.CENTER_CROP);

            ContentModeConfig maxContentConfig = new ContentModeConfig();
            maxContentConfig.setScale(1.2);
            maxContentConfig.setMode(ContentModeConfig.ScaleMode.CENTER_CROP_SPACE);

            CameraBound cameraBound = new CameraBound();
            cameraBound.setMinContentMode(minContentConfig);
            cameraBound.setMaxContentMode(maxContentConfig);
            cameraBound.setWidth(IMAGE_WIDTH);
            cameraBound.setHeight(IMAGE_HEIGHT);
            room.setCameraBound(cameraBound);

            // 4: 移动到图片顶部
            double scale = whiteboardView.getWidth() / IMAGE_WIDTH;
            double viewportHeight = whiteboardView.getHeight() / scale;
            double offset = (IMAGE_HEIGHT - viewportHeight) / 2;

            CameraConfig cameraConfig = new CameraConfig();
            cameraConfig.setCenterX(0.0);
            cameraConfig.setCenterY(-offset);
            cameraConfig.setAnimationMode(AnimationMode.Continuous);
            room.moveCamera(cameraConfig);
        });

        binding.cleanImage.setOnClickListener(v -> {
            room.cleanScene(false);
        });
    }

    @Override
    protected void onJoinRoomSuccess() {

    }
}
