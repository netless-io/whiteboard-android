package com.herewhite.demo.test.window;

import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;

import com.herewhite.demo.R;
import com.herewhite.demo.common.SampleBaseActivity;
import com.herewhite.demo.databinding.ActivityWindowAppliancePluginBinding;
import com.herewhite.sdk.WhiteSdkConfiguration;
import com.herewhite.sdk.domain.Appliance;
import com.herewhite.sdk.domain.CameraConfig;
import com.herewhite.sdk.domain.ImageInformationWithUrl;
import com.herewhite.sdk.domain.MemberState;
import com.herewhite.sdk.domain.Promise;
import com.herewhite.sdk.domain.SDKError;
import com.herewhite.sdk.domain.ShapeType;
import com.herewhite.sdk.domain.StrokeType;
import com.herewhite.sdk.domain.WindowAppParam;

public class WindowNoAppliancePluginActivity extends SampleBaseActivity {

    private ActivityWindowAppliancePluginBinding binding;

    @Override
    protected View getContentView() {
        binding = ActivityWindowAppliancePluginBinding.inflate(getLayoutInflater());
        return binding.getRoot();
    }

    @Override
    protected void initView() {
        binding.insertNewDynamic.setOnClickListener(v -> {
            String prefixUrl = "https://conversion-demo-cn.oss-cn-hangzhou.aliyuncs.com/demo/dynamicConvert";
            String taskUuid = "3e3a2b8845194f998e6e05adab70e1a1";
            WindowAppParam param = WindowAppParam.createSlideApp(taskUuid, prefixUrl, "Projector App");
            room.addApp(param, null);
        });

        binding.insertImage.setOnClickListener(v -> {
            room.insertImage(new ImageInformationWithUrl(0d, 0d, 100d, 200d, "https://p5.ssl.qhimg.com/t01a2bd87890397464a.png"));
        });

        binding.redo.setOnClickListener(v -> {
            room.redo();
        });

        binding.undo.setOnClickListener(v -> {
            room.undo();
        });

        binding.clear.setOnClickListener(v -> {
            room.cleanScene(true);
        });

        binding.pluginPencil.setOnClickListener(v -> {
            MemberState state = new MemberState();
            state.setCurrentApplianceName(Appliance.PENCIL);
            room.setMemberState(state);
        });

        binding.selector.setOnClickListener(v -> {
            MemberState state = new MemberState();
            state.setCurrentApplianceName(Appliance.SELECTOR);
            room.setMemberState(state);
        });

        binding.laserPen.setOnClickListener(v -> {
            MemberState state = new MemberState();
            state.setCurrentApplianceName(Appliance.LASER_PENCIL);
            state.setStrokeType(StrokeType.Normal);
            room.setMemberState(state);
        });

        binding.eraser.setOnClickListener(v -> {
            MemberState state = new MemberState();
            state.setCurrentApplianceName(Appliance.ERASER);
            room.setMemberState(state);
        });

        binding.text.setOnClickListener(v -> {
            MemberState state = new MemberState();
            state.setCurrentApplianceName(Appliance.TEXT);
            room.setMemberState(state);

        });

        binding.star.setOnClickListener(v -> {
            MemberState state = new MemberState();
            state.setShapeType(ShapeType.Pentagram);
            room.setMemberState(state);
        });

        binding.clicker.setOnClickListener(v -> {
            MemberState state = new MemberState();
            state.setCurrentApplianceName(Appliance.CLICKER);
            room.setMemberState(state);
        });

        binding.head.setOnClickListener(v -> {
            MemberState state = new MemberState();
            state.setCurrentApplianceName(Appliance.HAND);
            room.setMemberState(state);
        });

        binding.resetCamera.setOnClickListener(v -> {
            CameraConfig config = new CameraConfig();
            config.setCenterX(0d);
            config.setCenterY(0d);
            config.setScale(1d);
            room.moveCamera(config);
        });

        binding.snapshot.setOnClickListener(v -> {
            room.getSceneSnapshotImage("/init", new Promise<Bitmap>() {
                @Override
                public void then(Bitmap bitmap) {
                    ImageView viewById = findViewById(R.id.iv_bitmap);
                    viewById.setImageBitmap(bitmap);
                    logAction("get bitmap");
                }

                @Override
                public void catchEx(SDKError t) {
                    logAction("get bitmap error");
                }
            });
        });

        binding.scenePreview.setOnClickListener(v -> {
            room.getScenePreviewImage("/init", new Promise<Bitmap>() {
                @Override
                public void then(Bitmap bitmap) {
                    ImageView viewById = findViewById(R.id.iv_bitmap);
                    viewById.setImageBitmap(bitmap);
                    logAction("get bitmap");
                }

                @Override
                public void catchEx(SDKError t) {
                    logAction("get bitmap error");
                }
            });
        });
    }

    protected WhiteSdkConfiguration generateSdkConfig() {
        WhiteSdkConfiguration configuration = new WhiteSdkConfiguration(demoAPI.getAppId(), true);
        configuration.setUseMultiViews(true);
        return configuration;
    }

    @Override
    protected void onJoinRoomSuccess() {
        room.disableSerialization(false);
    }
}
