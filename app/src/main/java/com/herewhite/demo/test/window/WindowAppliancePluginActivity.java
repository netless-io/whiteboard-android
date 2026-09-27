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
import com.herewhite.sdk.domain.DispatchDocsEventResult;
import com.herewhite.sdk.domain.ImageInformationWithUrl;
import com.herewhite.sdk.domain.LoggerOptions;
import com.herewhite.sdk.domain.LocalLogOptions;
import com.herewhite.sdk.domain.SlideSyncEventQueuePolicy;
import com.herewhite.sdk.domain.MemberState;
import com.herewhite.sdk.domain.Promise;
import com.herewhite.sdk.domain.PptPage;
import com.herewhite.sdk.domain.ReloadBackgroundImageParams;
import com.herewhite.sdk.domain.ReloadBackgroundImageResult;
import com.herewhite.sdk.domain.SDKError;
import com.herewhite.sdk.domain.Scene;
import com.herewhite.sdk.domain.ShapeType;
import com.herewhite.sdk.domain.StrokeType;
import com.herewhite.sdk.domain.WindowAppParam;
import com.herewhite.sdk.domain.WindowDocsEvent;
import com.herewhite.sdk.domain.WindowOriginSize;
import com.herewhite.sdk.domain.UnifiedPageState;
import com.herewhite.sdk.domain.UnifiedPageStateChange;
import com.herewhite.sdk.domain.WindowParams;
import com.herewhite.sdk.window.UnifiedPageStateListener;
import org.json.JSONObject;

import java.util.Map;

public class WindowAppliancePluginActivity extends SampleBaseActivity {

    private static final String E2E_LOG_PREFIX = "[UnifiedPageE2E] ";
    private static final double ORIGIN_WIDTH = 1280d;
    private static final double ORIGIN_HEIGHT = 720d;
    private ActivityWindowAppliancePluginBinding binding;

    @Override
    protected View getContentView() {
        binding = ActivityWindowAppliancePluginBinding.inflate(getLayoutInflater());
        return binding.getRoot();
    }

    @Override
    protected void initView() {
        binding.exitRoom.setOnClickListener(v -> leaveRoomAndFinish());

        binding.insertNewDynamic.setOnClickListener(v -> {
            String prefixUrl = "https://conversion-demo-cn.oss-cn-hangzhou.aliyuncs.com/demo/dynamicConvert";
            String taskUuid = "3e3a2b8845194f998e6e05adab70e1a1";
            WindowAppParam param = WindowAppParam.createSlideApp(taskUuid, prefixUrl, "Slide 1280x720")
                    .setOriginSize(originSize());
            addAppForE2E(param);
        });

        binding.insertPresentation.setOnClickListener(v -> {
            Scene[] scenes = new Scene[]{
                    new Scene("1", new PptPage(
                            "https://convertcdn.netless.link/staticConvert/18140800fe8a11eb8cb787b1c376634e/1.png",
                            714d,
                            1010d)),
                    new Scene("2", new PptPage(
                            "https://convertcdn.netless.link/staticConvert/18140800fe8a11eb8cb787b1c376634e/2.png",
                            714d,
                            1010d))
            };
            WindowAppParam param = WindowAppParam.createPresentationApp(
                    "/presentation-e2e",
                    scenes,
                    "Presentation 1280x720"
            ).setOriginSize(originSize());
            addAppForE2E(param);
        });

        binding.scalePage.setOnClickListener(v -> dispatchFocusedDocsEvent(WindowDocsEvent.ScalePage(2d)));
        binding.prevPage.setOnClickListener(v -> dispatchFocusedDocsEvent(WindowDocsEvent.PrevPage));
        binding.nextPage.setOnClickListener(v -> dispatchFocusedDocsEvent(WindowDocsEvent.NextPage));
        binding.prevStep.setOnClickListener(v -> dispatchFocusedDocsEvent(WindowDocsEvent.PrevStep));
        binding.nextStep.setOnClickListener(v -> dispatchFocusedDocsEvent(WindowDocsEvent.NextStep));

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
        slideAppOptions.setEnableGlobalClick(false);
        slideAppOptions.setEnableScale(true);
        slideAppOptions.setSyncEventQueuePolicy(SlideSyncEventQueuePolicy.LatestPendingRender);
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
        WindowParams windowParams = new WindowParams()
                .setChessboard(false)
                .setFullscreen(true)
                .setUseBoxesStatus(false)
                .setForceMaximized(true)
                .setLazySetupInMaximizedMode(true)
                .setMaxCachedAppsInMaximizedMode(3)
                .setOverwriteStyles(
                        ".telebox-collector{display:none !important;}" +
                                ".vjs-p .player-controller{display:none !important;}" +
                                ".netless-app-slide-wb-view {clip-path: none !important;}" +
                                ".telebox-box.telebox-blur.telebox-maximized {display: none !important;}" +
                                ".netless-app-slide-content [data-resizable-scroll=\"true\"] ~ .scroll-bar { display: none !important;}" +
                                ".netless-app-slide-content [data-resizable-scroll=\"true\"] { width: 100% !important; height: 100% !important;}"
                );
        roomParams.setWindowParams(windowParams);
        return roomParams;
    }

    private AppliancePluginOptions getAppliancePluginOptions() {
        Map<String, Object> extrasOptions = Map.of(
                "useSimple", true,
                "useBackgroundThread", true,
                "allowImageBitmapFallback", getIntent().getBooleanExtra(
                        SampleBaseActivity.EXTRA_ALLOW_IMAGE_BITMAP_FALLBACK, false),
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
        logE2E("room ready originSize=1280x720 target=focused");

        whiteSdk.setUnifiedPageStateListener(new UnifiedPageStateListener() {
            @Override
            public void onUnifiedPageStateChange(UnifiedPageStateChange state) {
                logE2E("stateChange=" + gson.toJson(state));
            }
        });

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

    private WindowOriginSize originSize() {
        return new WindowOriginSize(ORIGIN_WIDTH, ORIGIN_HEIGHT);
    }

    private void addAppForE2E(WindowAppParam param) {
        if (room == null) {
            logE2E("addApp ignored: room not ready");
            return;
        }
        logE2E("addApp request kind=" + param.getKind() + " originSize=1280x720");
        room.addApp(param, new Promise<String>() {
            @Override
            public void then(String appId) {
                logE2E("addApp success kind=" + param.getKind() + " appId=" + appId);
                queryFocusedPageState("addApp:" + param.getKind());
            }

            @Override
            public void catchEx(SDKError error) {
                logE2E("addApp failed kind=" + param.getKind() + " error=" + error);
            }
        });
    }

    private void dispatchFocusedDocsEvent(WindowDocsEvent event) {
        if (room == null) {
            logE2E(event.getEvent() + " ignored: room not ready");
            return;
        }
        logE2E("dispatch request event=" + event.getEvent() + " target=focused options="
                + gson.toJson(event.getOptions()));
        room.dispatchDocsEvent(event, new Promise<DispatchDocsEventResult>() {
            @Override
            public void then(DispatchDocsEventResult result) {
                logE2E("dispatch result event=" + event.getEvent() + " payload=" + gson.toJson(result));
                queryFocusedPageState(event.getEvent());
            }

            @Override
            public void catchEx(SDKError error) {
                logE2E("dispatch failed event=" + event.getEvent() + " error=" + error);
                queryFocusedPageState(event.getEvent() + ":failed");
            }
        });
    }

    private void queryFocusedPageState(String source) {
        room.getPageState(new Promise<UnifiedPageState>() {
            @Override
            public void then(UnifiedPageState state) {
                logE2E("getPageState source=" + source + " target=focused payload=" + gson.toJson(state));
            }

            @Override
            public void catchEx(SDKError error) {
                logE2E("getPageState failed source=" + source + " error=" + error);
            }
        });
    }

    private void logE2E(String message) {
        logAction(E2E_LOG_PREFIX + message);
        showLogDisplay(E2E_LOG_PREFIX + message);
    }
}
