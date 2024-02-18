package com.herewhite.demo.test;

import android.view.View;

import com.herewhite.demo.common.SampleBaseActivity;
import com.herewhite.demo.databinding.ActivityCameraBoundBinding;
import com.herewhite.sdk.AbstractRoomCallbacks;
import com.herewhite.sdk.domain.AnimationMode;
import com.herewhite.sdk.domain.Appliance;
import com.herewhite.sdk.domain.CameraBound;
import com.herewhite.sdk.domain.CameraConfig;
import com.herewhite.sdk.domain.ContentModeConfig;
import com.herewhite.sdk.domain.MemberState;
import com.herewhite.sdk.domain.PptPage;
import com.herewhite.sdk.domain.RoomState;
import com.herewhite.sdk.domain.Scene;
import com.herewhite.sdk.domain.SceneState;

import java.util.ArrayList;
import java.util.List;

public class CameraBoundScenesActivity extends SampleBaseActivity {

    List<Image> images = new ArrayList<>();
    private ActivityCameraBoundBinding binding;

    @Override
    protected View getContentView() {
        binding = ActivityCameraBoundBinding.inflate(getLayoutInflater());
        return binding.getRoot();
    }

    @Override
    protected void initView() {
        mRoomCallbackHock = new AbstractRoomCallbacks() {
            @Override
            public void onRoomStateChanged(RoomState modifyState) {
                if (modifyState.getSceneState() != null) {
                    updateScene(modifyState.getSceneState());
                }
            }
        };

        binding.prevPage.setOnClickListener(v -> {
            room.prevPage(null);
        });
        binding.nextPage.setOnClickListener(v -> {
            room.nextPage(null);
        });
    }

    private void updateScene(SceneState sceneState) {
        Image image = images.get(sceneState.getIndex());

        ContentModeConfig minContentConfig = new ContentModeConfig();
        minContentConfig.setMode(ContentModeConfig.ScaleMode.CENTER_CROP);

        ContentModeConfig maxContentConfig = new ContentModeConfig();
        maxContentConfig.setScale(3.0);
        maxContentConfig.setMode(ContentModeConfig.ScaleMode.CENTER_CROP_SPACE);

        double widthScale = (double) px2dp(whiteboardView.getWidth()) / image.getWidth();
        double whiteboardHeight = (double) px2dp(whiteboardView.getHeight()) / widthScale;
        double offset = (whiteboardHeight - image.getHeight()) / 2;

        CameraBound cameraBound = new CameraBound();
        if (whiteboardHeight >= image.getHeight()) {
            cameraBound.setMinContentMode(minContentConfig);
            cameraBound.setMaxContentMode(maxContentConfig);
            cameraBound.setCenterX(0.0);
            cameraBound.setCenterY(offset);
            cameraBound.setWidth(image.getWidth());
            cameraBound.setHeight(whiteboardHeight);
        } else {
            cameraBound.setCenterX(0.0);
            cameraBound.setCenterY(0.0);
            cameraBound.setMinContentMode(minContentConfig);
            cameraBound.setMaxContentMode(maxContentConfig);
            cameraBound.setWidth(image.getWidth());
            cameraBound.setHeight(image.getHeight());
        }
        room.setCameraBound(cameraBound);

        CameraConfig cameraConfig = new CameraConfig();
        cameraConfig.setCenterX(0.0);
        cameraConfig.setCenterY(offset);
        cameraConfig.setScale(widthScale);
        cameraConfig.setAnimationMode(AnimationMode.Continuous);
        room.moveCamera(cameraConfig);
    }

    public float px2dp(final float px) {
        return px / getResources().getDisplayMetrics().density;
    }

    @Override
    protected void onJoinRoomSuccess() {
        images.add(new Image(61, 1280.0, "https://white-pan.oss-cn-shanghai.aliyuncs.com/101/image/alin-rusu-1239275-unsplash_opt.jpg"));
        images.add(new Image(720.0, 1280.0, "https://white-pan.oss-cn-shanghai.aliyuncs.com/101/image/alin-rusu-1239275-unsplash_opt.jpg"));
        images.add(new Image(1080.0, 600.0, "https://white-pan.oss-cn-shanghai.aliyuncs.com/101/image/alin-rusu-1239275-unsplash_opt.jpg"));
        images.add(new Image(961.0, 1280.0, "https://white-pan.oss-cn-shanghai.aliyuncs.com/101/image/alin-rusu-1239275-unsplash_opt.jpg"));

        String dirName = "/images_dir";
        Scene[] scenes = new Scene[images.size()];
        for (int i = 0; i < images.size(); i++) {
            Image image = images.get(i);
            Scene scene = new Scene(
                    String.valueOf(i + 1),
                    new PptPage(image.getUrl(), image.getWidth(), image.getHeight())
            );
            scenes[i] = scene;
        }
        room.putScenes(dirName, scenes, 0);
        room.setScenePath(dirName + "/1");

        // 设置工具为抓手
        MemberState memberState = new MemberState();
        memberState.setCurrentApplianceName(Appliance.HAND);
        room.setMemberState(memberState);
    }

    static class Image {
        private final double width;
        private final double height;
        private final String url;

        public Image(double width, double height, String url) {
            this.width = width;
            this.height = height;
            this.url = url;
        }

        public double getWidth() {
            return width;
        }

        public double getHeight() {
            return height;
        }

        public String getUrl() {
            return url;
        }
    }
}
