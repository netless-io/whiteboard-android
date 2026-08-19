package com.herewhite.demo.test.window;

import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;

import com.herewhite.demo.R;
import com.herewhite.demo.common.SampleBaseActivity;
import com.herewhite.demo.databinding.ActivityWindowAppliancePluginBinding;
import com.herewhite.sdk.CommonCallback;
import com.herewhite.sdk.RoomParams;
import com.herewhite.sdk.WhiteSdkConfiguration;
import com.herewhite.sdk.domain.Appliance;
import com.herewhite.sdk.domain.AppliancePluginOptions;
import com.herewhite.sdk.domain.BackgroundImageLoadEvent;
import com.herewhite.sdk.domain.BackgroundImageLoadOptions;
import com.herewhite.sdk.domain.CameraConfig;
import com.herewhite.sdk.domain.ImageInformationWithUrl;
import com.herewhite.sdk.domain.LoggerOptions;
import com.herewhite.sdk.domain.LocalLogOptions;
import com.herewhite.sdk.domain.MemberState;
import com.herewhite.sdk.domain.Promise;
import com.herewhite.sdk.domain.ReloadBackgroundImageParams;
import com.herewhite.sdk.domain.ReloadBackgroundImageResult;
import com.herewhite.sdk.domain.SDKError;
import com.herewhite.sdk.domain.ShapeType;
import com.herewhite.sdk.domain.StrokeType;
import com.herewhite.sdk.domain.WindowAppParam;
import org.json.JSONObject;

import java.util.Map;

public class WindowAppliancePluginActivity extends SampleBaseActivity {

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
            room.insertImage(new ImageInformationWithUrl(0d,
                    0d,
                    100d,
                    200d,
                    "https://p5.ssl.qhimg.com/t01a2bd87890397464a.png"));
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
            state.setStrokeType(StrokeType.Stroke);
            room.setMemberState(state);
        });

        binding.selector.setOnClickListener(v -> {
            MemberState state = new MemberState();
            state.setCurrentApplianceName(Appliance.SELECTOR);
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
                    viewById.setVisibility(View.VISIBLE);
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
                    viewById.setVisibility(View.VISIBLE);
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
        configuration.setEnableAppliancePlugin(true);
        BackgroundImageLoadOptions loadOptions = new BackgroundImageLoadOptions();
        loadOptions.setMaxRetries(3);
        configuration.setBackgroundImageLoadOptions(loadOptions);
        WhiteSdkConfiguration.SlideAppOptions slideAppOptions = configuration.getSlideAppOptions();
        slideAppOptions.setResolution(1d);
        slideAppOptions.setMaxResolutionLevel(2);
        slideAppOptions.setMinFPS(5);
        slideAppOptions.setMaxFPS(15);
        slideAppOptions.setEnableGlobalClick(false);
        LoggerOptions loggerOptions = new LoggerOptions();
        loggerOptions.setLocalLog(new LocalLogOptions().setEnabled(true).setEnabledUpload(true));
        configuration.setLoggerOptions(loggerOptions);
        return configuration;
    }

    @Override
    protected RoomParams generateRoomParams() {
        RoomParams roomParams = super.generateRoomParams();
        roomParams.setWritable(false);
        roomParams.setAppliancePluginOptions(getAppliancePluginOptions());
        return roomParams;
    }

    private AppliancePluginOptions getAppliancePluginOptions() {
        Map<String, Object> extrasOptions = Map.of(
                "useWorker", "mainThread",
                "useSimple", true,
                "useBackgroundThread", true,
                // cursor 配置
                "cursor", Map.of(
                        "enable", true,
                        "expirationTime", 500,
                        "syncedLabel", Map.of(
                                "enableShowName", true
                        ),
                        "appearance", Map.of(
                                "pencil", Map.of(
                                        "synced", Map.of(
                                                "enableShowName", false
                                        )
                                ),
                                "clicker", Map.of(
                                        "synced", Map.of(
                                                "images", Map.of(
                                                        "standardResolution", "https://api.iconify.design/mdi:video-wireless-outline.svg?color=%237f7f7f"
                                                )
                                        )
                                )
                        )
                ),
                // bezier 配置
                "bezier", Map.of(
                        "enable", false,
                        "maxDrawCount", 200
                ),
                // textEditor 配置
                "textEditor", Map.of(
                        "showFloatBar", false,
                        "canSelectorSwitch", false,
                        "rightBoundBreak", true
                )
        );

        AppliancePluginOptions appliancePluginOptions = new AppliancePluginOptions();
        appliancePluginOptions.setExtras(extrasOptions);
        return appliancePluginOptions;
    }

    @Override
    protected void onJoinRoomSuccess() {
        room.disableSerialization(false);
        updateWritableUi();
        binding.exitRoom.setOnClickListener(v -> room.disconnect(new Promise<Object>() {
            @Override
            public void then(Object result) {
                runOnUiThread(() -> {
                    logAction("disconnect: " + result);
                    finish();
                });
            }

            @Override
            public void catchEx(SDKError t) {
                runOnUiThread(() -> {
                    logAction("disconnect failed: " + t.getMessage());
                    finish();
                });
            }
        }));

        binding.toggleWritable.setOnClickListener(v -> {
            boolean next = !Boolean.TRUE.equals(room.getWritable());
            room.setWritable(next, new Promise<Boolean>() {
                @Override
                public void then(Boolean result) {
                    runOnUiThread(() -> {
                        logAction("setWritable: " + result);
                        updateWritableUi();
                    });
                }

                @Override
                public void catchEx(SDKError t) {
                    runOnUiThread(() -> logAction("setWritable failed: " + t.getMessage()));
                }
            });
        });
        whiteSdk.setCommonCallbacks(new CommonCallback() {
            @Override
            public void onLocalLogStateChange(JSONObject state) {
                logAction("localLogStateChange: " + state);
            }

            @Override
            public void onBackgroundImageLoad(BackgroundImageLoadEvent event) {
                logAction("backgroundImageLoad: " + gson.toJson(event));
                if (!"failed".equals(event.state)) {
                    return;
                }
                // mainView 可直接比较；appId 对应的路径由 reload API 在插件内原子校验。
                if ("mainView".equals(event.viewId)
                        && !event.scenePath.equals(room.getSceneState().getScenePath())) {
                    return;
                }
                ReloadBackgroundImageParams params =
                        new ReloadBackgroundImageParams(event.source, event.viewId, event.scenePath);
                whiteSdk.reloadBackgroundImage(params, new Promise<ReloadBackgroundImageResult>() {
                    @Override
                    public void then(ReloadBackgroundImageResult result) {
                        logAction("reloadBackgroundImage: " + gson.toJson(result));
                    }

                    @Override
                    public void catchEx(SDKError error) {
                        logAction("reloadBackgroundImage failed: " + error.getMessage());
                    }
                });
            }
        });
    }

    private void updateWritableUi() {
        boolean writable = Boolean.TRUE.equals(room.getWritable());
        binding.toolbar.setVisibility(writable ? View.VISIBLE : View.GONE);
        binding.toggleWritable.setText(writable ? "移除可写" : "获取可写");
    }
}
