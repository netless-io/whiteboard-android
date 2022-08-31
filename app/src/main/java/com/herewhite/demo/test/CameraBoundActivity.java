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

import java.util.ArrayList;
import java.util.List;

public class CameraBoundActivity extends SampleBaseActivity {

    private ActivityCameraBoundBinding binding;
    private ImageSizeProvider imageSizeProvider = new ImageSizeProvider();

    @Override
    protected View getContentView() {
        binding = ActivityCameraBoundBinding.inflate(getLayoutInflater());
        return binding.getRoot();
    }

    @Override
    protected void initView() {
        binding.insertImage.setOnClickListener(v -> {
            ImageSize<Double> imageSize = imageSizeProvider.getImageSize();
            imageSizeProvider.next();

            double IMAGE_WIDTH = imageSize.getWidth();
            double IMAGE_HEIGHT = imageSize.getHeight();

            // 此示例以 Width 为基准，图片顶部与视图顶部重合
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

            boolean oblateType = IMAGE_HEIGHT / IMAGE_WIDTH < (double) whiteboardView.getHeight() / whiteboardView.getWidth();
            // 3: 设置视角边界
            ContentModeConfig minContentConfig = new ContentModeConfig();
            ContentModeConfig maxContentConfig = new ContentModeConfig();
            if (oblateType) {
                minContentConfig.setMode(ContentModeConfig.ScaleMode.CENTER_INSIDE);
                maxContentConfig.setMode(ContentModeConfig.ScaleMode.CENTER_INSIDE);
            } else {
                minContentConfig.setMode(ContentModeConfig.ScaleMode.CENTER_CROP);
                maxContentConfig.setMode(ContentModeConfig.ScaleMode.CENTER_CROP);
            }

            double scale = px2dp(whiteboardView.getWidth()) / IMAGE_WIDTH;
            double viewportHeight = px2dp(whiteboardView.getHeight()) / scale;
            double offset = (viewportHeight - IMAGE_HEIGHT) / 2;

            CameraBound cameraBound = new CameraBound();
            cameraBound.setMinContentMode(minContentConfig);
            cameraBound.setMaxContentMode(maxContentConfig);
            cameraBound.setWidth(IMAGE_WIDTH);
            cameraBound.setHeight(IMAGE_HEIGHT);
            if (oblateType) {
                cameraBound.setCenterX(0.0);
                cameraBound.setCenterY(offset);
            } else {
                cameraBound.setCenterX(0.0);
                cameraBound.setCenterY(0.0);
            }

            room.setCameraBound(cameraBound);

            // 4: 移动到图片顶部
            CameraConfig cameraConfig = new CameraConfig();
            cameraConfig.setCenterX(0.0);
            cameraConfig.setCenterY(offset);
            cameraConfig.setScale(scale);
            cameraConfig.setAnimationMode(AnimationMode.Continuous);
            room.moveCamera(cameraConfig);
        });

        binding.cleanImage.setOnClickListener(v -> {
            room.cleanScene(false);
        });
    }

    public float px2dp(final float px) {
        return px / getResources().getDisplayMetrics().density;
    }

    @Override
    protected void onJoinRoomSuccess() {

    }

    abstract class Action {
        protected final ImageSize<Double> imageSize;

        Action(ImageSize<Double> imageSize) {
            this.imageSize = imageSize;
        }

        public abstract void run();
    }

    // 依据宽高，移动到初始位置,支持滚动
    class WOrHScrollAction extends Action {

        WOrHScrollAction(ImageSize<Double> imageSize) {
            super(imageSize);
        }

        public void run() {
            double IMAGE_WIDTH = imageSize.getWidth();
            double IMAGE_HEIGHT = imageSize.getHeight();

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
            double scaleByW = px2dp(whiteboardView.getWidth()) / IMAGE_WIDTH;
            double scaleByH = px2dp(whiteboardView.getHeight()) / IMAGE_HEIGHT;
            double scale;
            double offsetX = 0;
            double offsetY = 0;
            if (scaleByH < scaleByW) {
                scale = scaleByW;
                double viewportHeight = px2dp(whiteboardView.getHeight()) / scale;
                offsetY = (viewportHeight - IMAGE_HEIGHT) / 2;
            } else {
                scale = scaleByH;
                double viewportWidth = px2dp(whiteboardView.getWidth()) / scale;
                offsetX = (viewportWidth - IMAGE_WIDTH) / 2;
            }

            CameraConfig cameraConfig = new CameraConfig();
            cameraConfig.setCenterX(offsetX);
            cameraConfig.setCenterY(offsetY);
            cameraConfig.setScale(scale);
            cameraConfig.setAnimationMode(AnimationMode.Continuous);
            room.moveCamera(cameraConfig);
        }
    }

    // 居中显示，无移动示例
    class CenterLockedAction extends Action {

        CenterLockedAction(ImageSize<Double> imageSize) {
            super(imageSize);
        }

        public void run() {
            double IMAGE_WIDTH = imageSize.getWidth();
            double IMAGE_HEIGHT = imageSize.getHeight();

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
            minContentConfig.setMode(ContentModeConfig.ScaleMode.CENTER_INSIDE);

            ContentModeConfig maxContentConfig = new ContentModeConfig();
            maxContentConfig.setMode(ContentModeConfig.ScaleMode.CENTER_INSIDE);

            CameraBound cameraBound = new CameraBound();
            cameraBound.setMinContentMode(minContentConfig);
            cameraBound.setMaxContentMode(maxContentConfig);
            cameraBound.setWidth(IMAGE_WIDTH);
            cameraBound.setHeight(IMAGE_HEIGHT);
            room.setCameraBound(cameraBound);

            // 4: 移动到图片顶部
            double scaleByW = px2dp(whiteboardView.getWidth()) / IMAGE_WIDTH;
            double scaleByH = px2dp(whiteboardView.getHeight()) / IMAGE_HEIGHT;
            double scale = Math.min(scaleByW, scaleByH);

            CameraConfig cameraConfig = new CameraConfig();
            cameraConfig.setCenterX(0.0);
            cameraConfig.setCenterY(0.0);
            cameraConfig.setScale(scale);
            cameraConfig.setAnimationMode(AnimationMode.Continuous);
            room.moveCamera(cameraConfig);
        }
    }

    static class ImageSizeProvider {
        private final List<ImageSize<Double>> imageSizes = new ArrayList<>();
        int index = 0;

        {
            imageSizes.add(new ImageSize<>(750.0, 190.0));
            imageSizes.add(new ImageSize<>(190.0, 750.0));
            imageSizes.add(new ImageSize<>(2360.0, 1640.0));
            imageSizes.add(new ImageSize<>(3750.0, 950.0));
            imageSizes.add(new ImageSize<>(1360.0, 1640.0));
            imageSizes.add(new ImageSize<>(800.0, 1640.0));
        }

        public ImageSize<Double> getImageSize() {
            return imageSizes.get(index);
        }

        public void next() {
            index = ++index % imageSizes.size();
        }
    }

    static class ImageSize<T> {
        private final T width;
        private final T height;

        public ImageSize(T width, T height) {
            this.width = width;
            this.height = height;
        }

        public T getWidth() {
            return width;
        }

        public T getHeight() {
            return height;
        }
    }
}
