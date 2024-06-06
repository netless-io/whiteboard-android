package com.herewhite.demo.test.window;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.herewhite.demo.LocalFileWebViewClient;
import com.herewhite.demo.R;
import com.herewhite.demo.common.DemoAPI;
import com.herewhite.demo.utils.FileUtils;
import com.herewhite.demo.utils.MapBuilder;
import com.herewhite.sdk.Room;
import com.herewhite.sdk.RoomListener;
import com.herewhite.sdk.RoomParams;
import com.herewhite.sdk.WhiteSdk;
import com.herewhite.sdk.WhiteSdkConfiguration;
import com.herewhite.sdk.WhiteboardView;
import com.herewhite.sdk.domain.GlobalState;
import com.herewhite.sdk.domain.Promise;
import com.herewhite.sdk.domain.RoomPhase;
import com.herewhite.sdk.domain.RoomState;
import com.herewhite.sdk.domain.SDKError;
import com.herewhite.sdk.domain.Scene;
import com.herewhite.sdk.domain.WhiteDisplayerState;
import com.herewhite.sdk.domain.WindowAppParam;
import com.herewhite.sdk.domain.WindowAppSyncAttrs;
import com.herewhite.sdk.domain.WindowParams;
import com.herewhite.sdk.domain.WindowPrefersColorScheme;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import wendu.dsbridge.DWebView;

public class WindowTestActivity extends AppCompatActivity {
    private static final String ROOM_INFO = "RoomInfo";
    private static final String ROOM_ACTION = "RoomAction";
    private static final String CUSTOM_UI = "custom_ui";

    final Gson gson = new Gson();
    final DemoAPI demoAPI = DemoAPI.get();

    WhiteboardView mWhiteboardView;
    WhiteSdk mWhiteSdk;
    Room mRoom;
    FrameLayout mWhiteboardParent;

    Stack<String> appIds = new Stack<>();
    Map<String, WindowAppSyncAttrs> apps = new HashMap<>();
    long lastUpdate = 0;
    private Promise<String> insertPromise = new Promise<String>() {
        @Override
        public void then(String appId) {
            appIds.push(appId);
        }

        @Override
        public void catchEx(SDKError t) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_window_test);
        getSupportActionBar().hide();

        DWebView.setWebContentsDebuggingEnabled(true);

        mWhiteboardView = findViewById(R.id.white);
        mWhiteboardView.getSettings().setAllowUniversalAccessFromFileURLs(true);
        // 使用 LocalFileWebViewClient 对 动态 ppt 拦截进行替换，先查看本地是否有，如果没有再发出网络请求
        LocalFileWebViewClient client = new LocalFileWebViewClient();
        client.setPptDirectory(getCacheDir().getAbsolutePath());
        mWhiteboardView.setWebViewClient(client);

        mWhiteboardParent = findViewById(R.id.whiteParent);

        // Slide 音量测试
        findViewById(R.id.updateSlideVolume).setOnClickListener(new View.OnClickListener() {
            final List<Float> volumes = new ArrayList<Float>() {
                {
                    add(1.0f);
                    add(0f);
                    add(0.5f);
                }
            };
            int index = 0;

            @Override
            public void onClick(View v) {
                mWhiteSdk.updateSlideVolume(volumes.get(index++ % volumes.size()));
            }
        });

        findViewById(R.id.getSlideVolume).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mWhiteSdk.getSlideVolume(new Promise<Double>() {
                    @Override
                    public void then(Double volume) {
                        showToast("current volume is:" + volume);
                    }

                    @Override
                    public void catchEx(SDKError t) {

                    }
                });

            }
        });


        // 窗口比例
        findViewById(R.id.radio).setOnClickListener(new View.OnClickListener() {
            final List<Float> ratios = new ArrayList<Float>() {
                {
                    add(1.0f);
                    add(16f / 9);
                    add(9f / 16);
                }
            };
            int index = 0;

            @Override
            public void onClick(View v) {
                mRoom.setContainerSizeRatio(ratios.get(index++ % ratios.size()));
            }
        });

        // 窗口暗色模式
        findViewById(R.id.colorScheme).setOnClickListener(new View.OnClickListener() {
            final List<WindowPrefersColorScheme> colorSchemes = new ArrayList<WindowPrefersColorScheme>() {
                {
                    add(WindowPrefersColorScheme.Dark);
                    add(WindowPrefersColorScheme.Light);
                    add(WindowPrefersColorScheme.Auto);
                }
            };
            int index = 0;

            @Override
            public void onClick(View v) {
                mRoom.setPrefersColorScheme(colorSchemes.get(index++ % colorSchemes.size()));
            }
        });

        // 插入播放器
        findViewById(R.id.insertPlayer).setOnClickListener(v -> {
            WindowAppParam appParam = WindowAppParam.createMediaPlayerApp("https://white-pan.oss-cn-shanghai.aliyuncs.com/101/oceans.mp4", "player");
            mRoom.addApp(appParam, insertPromise);
        });

        // 插入静态场景
        findViewById(R.id.insertStatic).setOnClickListener(v -> {
            // 由转换后信息序列化
            String ppts = "[{\"name\":\"1\",\"ppt\":{\"height\":1010.0,\"src\":\"https://convertcdn.netless.link/staticConvert/0764816000c411ecbfbbb9230f6dd80f/1.png\",\"width\":714.0}},{\"name\":\"2\",\"ppt\":{\"height\":1010.0,\"src\":\"https://convertcdn.netless.link/staticConvert/0764816000c411ecbfbbb9230f6dd80f/2.png\",\"width\":714.0}},{\"name\":\"3\",\"ppt\":{\"height\":1010.0,\"src\":\"https://convertcdn.netless.link/staticConvert/0764816000c411ecbfbbb9230f6dd80f/3.png\",\"width\":714.0}},{\"name\":\"4\",\"ppt\":{\"height\":1010.0,\"src\":\"https://convertcdn.netless.link/staticConvert/0764816000c411ecbfbbb9230f6dd80f/4.png\",\"width\":714.0}}]";
            Scene[] scenes = gson.fromJson(ppts, Scene[].class);

            WindowAppParam param = WindowAppParam.createDocsViewerApp("/static", scenes, "static");
            mRoom.addApp(param, insertPromise);
        });

        // 插入动态场景
        findViewById(R.id.insertDynamic).setOnClickListener(v -> {
            // 由转换后信息序列化
            String ppts = " [{\"name\":\"1\",\"ppt\":{\"src\":\"pptx://convertcdn.netless.link/dynamicConvert/369ac28037d011ec99f08bddeae74404/1.slide\",\"width\":1280,\"height\":720,\"previewURL\":\"https://convertcdn.netless.link/dynamicConvert/369ac28037d011ec99f08bddeae74404/preview/1.png\"}},{\"name\":\"2\",\"ppt\":{\"src\":\"pptx://convertcdn.netless.link/dynamicConvert/369ac28037d011ec99f08bddeae74404/2.slide\",\"width\":1280,\"height\":720,\"previewURL\":\"https://convertcdn.netless.link/dynamicConvert/369ac28037d011ec99f08bddeae74404/preview/2.png\"}},{\"name\":\"3\",\"ppt\":{\"src\":\"pptx://convertcdn.netless.link/dynamicConvert/369ac28037d011ec99f08bddeae74404/3.slide\",\"width\":1280,\"height\":720,\"previewURL\":\"https://convertcdn.netless.link/dynamicConvert/369ac28037d011ec99f08bddeae74404/preview/3.png\"}},{\"name\":\"4\",\"ppt\":{\"src\":\"pptx://convertcdn.netless.link/dynamicConvert/369ac28037d011ec99f08bddeae74404/4.slide\",\"width\":1280,\"height\":720,\"previewURL\":\"https://convertcdn.netless.link/dynamicConvert/369ac28037d011ec99f08bddeae74404/preview/4.png\"}},{\"name\":\"5\",\"ppt\":{\"src\":\"pptx://convertcdn.netless.link/dynamicConvert/369ac28037d011ec99f08bddeae74404/5.slide\",\"width\":1280,\"height\":720,\"previewURL\":\"https://convertcdn.netless.link/dynamicConvert/369ac28037d011ec99f08bddeae74404/preview/5.png\"}},{\"name\":\"6\",\"ppt\":{\"src\":\"pptx://convertcdn.netless.link/dynamicConvert/369ac28037d011ec99f08bddeae74404/6.slide\",\"width\":1280,\"height\":720,\"previewURL\":\"https://convertcdn.netless.link/dynamicConvert/369ac28037d011ec99f08bddeae74404/preview/6.png\"}},{\"name\":\"7\",\"ppt\":{\"src\":\"pptx://convertcdn.netless.link/dynamicConvert/369ac28037d011ec99f08bddeae74404/7.slide\",\"width\":1280,\"height\":720,\"previewURL\":\"https://convertcdn.netless.link/dynamicConvert/369ac28037d011ec99f08bddeae74404/preview/7.png\"}},{\"name\":\"8\",\"ppt\":{\"src\":\"pptx://convertcdn.netless.link/dynamicConvert/369ac28037d011ec99f08bddeae74404/8.slide\",\"width\":1280,\"height\":720,\"previewURL\":\"https://convertcdn.netless.link/dynamicConvert/369ac28037d011ec99f08bddeae74404/preview/8.png\"}},{\"name\":\"9\",\"ppt\":{\"src\":\"pptx://convertcdn.netless.link/dynamicConvert/369ac28037d011ec99f08bddeae74404/9.slide\",\"width\":1280,\"height\":720,\"previewURL\":\"https://convertcdn.netless.link/dynamicConvert/369ac28037d011ec99f08bddeae74404/preview/9.png\"}},{\"name\":\"10\",\"ppt\":{\"src\":\"pptx://convertcdn.netless.link/dynamicConvert/369ac28037d011ec99f08bddeae74404/10.slide\",\"width\":1280,\"height\":720,\"previewURL\":\"https://convertcdn.netless.link/dynamicConvert/369ac28037d011ec99f08bddeae74404/preview/10.png\"}},{\"name\":\"11\",\"ppt\":{\"src\":\"pptx://convertcdn.netless.link/dynamicConvert/369ac28037d011ec99f08bddeae74404/11.slide\",\"width\":1280,\"height\":720,\"previewURL\":\"https://convertcdn.netless.link/dynamicConvert/369ac28037d011ec99f08bddeae74404/preview/11.png\"}},{\"name\":\"12\",\"ppt\":{\"src\":\"pptx://convertcdn.netless.link/dynamicConvert/369ac28037d011ec99f08bddeae74404/12.slide\",\"width\":1280,\"height\":720,\"previewURL\":\"https://convertcdn.netless.link/dynamicConvert/369ac28037d011ec99f08bddeae74404/preview/12.png\"}},{\"name\":\"13\",\"ppt\":{\"src\":\"pptx://convertcdn.netless.link/dynamicConvert/369ac28037d011ec99f08bddeae74404/13.slide\",\"width\":1280,\"height\":720,\"previewURL\":\"https://convertcdn.netless.link/dynamicConvert/369ac28037d011ec99f08bddeae74404/preview/13.png\"}},{\"name\":\"14\",\"ppt\":{\"src\":\"pptx://convertcdn.netless.link/dynamicConvert/369ac28037d011ec99f08bddeae74404/14.slide\",\"width\":1280,\"height\":720,\"previewURL\":\"https://convertcdn.netless.link/dynamicConvert/369ac28037d011ec99f08bddeae74404/preview/14.png\"}}]";
            Scene[] scenes = gson.fromJson(ppts, Scene[].class);

            WindowAppParam param = WindowAppParam.createSlideApp("/dynamic003", scenes, "dynamic");
            mRoom.addApp(param, insertPromise);
        });

        // 插入新的动态PPT
        findViewById(R.id.insertNewDynamic).setOnClickListener(v -> {
            // prefixUrl
            String prefixUrl = "https://convertcdn.netless.link/dynamicConvert";
            String taskUuid = "47f359400ab1444986872db1723bb793";
            WindowAppParam param = WindowAppParam.createSlideApp(taskUuid, prefixUrl, "Projector App");
            mRoom.addApp(param, insertPromise);
        });

        findViewById(R.id.closeApp).setOnClickListener(v -> {
            mRoom.closeApp(appIds.pop(), new Promise<Boolean>() {
                @Override
                public void then(Boolean aBoolean) {

                }

                @Override
                public void catchEx(SDKError t) {

                }
            });
        });

        findViewById(R.id.focusApp).setOnClickListener(v -> {
            String[] ids = WindowTestActivity.this.apps.keySet().toArray(new String[0]);
            mRoom.focusApp(ids[0]);
        });

        findViewById(R.id.queryApps).setOnClickListener(v -> {
            mRoom.queryAllApps(new Promise<Map<String, WindowAppSyncAttrs>>() {
                @Override
                public void then(Map<String, WindowAppSyncAttrs> apps) {
                    Log.e("queryAllApps", apps.toString());
                    WindowTestActivity.this.apps = apps;
                }

                @Override
                public void catchEx(SDKError t) {
                    Log.e("queryApps error", t.toString());
                }
            });
        });

        findViewById(R.id.queryApp).setOnClickListener(v -> {
            String[] ids = WindowTestActivity.this.apps.keySet().toArray(new String[0]);
            mRoom.queryApp(ids[0], new Promise<WindowAppSyncAttrs>() {
                @Override
                public void then(WindowAppSyncAttrs attrs) {
                    Log.e("queryApp", gson.toJson(attrs));
                }

                @Override
                public void catchEx(SDKError t) {
                    Log.e("queryApp error", t.toString());
                }
            });

            // 查询不存在的 appId 触发 catchEx
            mRoom.queryApp("not_exited_appId", new Promise<WindowAppSyncAttrs>() {
                @Override
                public void then(WindowAppSyncAttrs attrs) {
                }

                @Override
                public void catchEx(SDKError t) {
                    Log.e("queryApp error", t.getMessage());
                }
            });
        });

        // 16:9 限定
        findViewById(R.id.lockRatio).setOnClickListener(v -> {
            lockRatio();
        });

        findViewById(R.id.disableOperation).setOnClickListener(v -> {
            if (mRoom != null) {
                mRoom.disableWindowOperation(true);
            }
        });

        findViewById(R.id.saveAttributes).setOnClickListener(v -> {
            if (mRoom != null) {
                mRoom.getWindowManagerAttributes(new Promise<String>() {
                    @Override
                    public void then(String s) {
                        File file = new File(getCacheDir(), WindowRestoreActivity.ATTRIBUTES_FILE);
                        try {
                            FileUtils.writeStringToFile(file, s);
                            gotoWindowRestore();
                        } catch (IOException e) {
                            showToast("write file error");
                        }
                    }

                    @Override
                    public void catchEx(SDKError t) {

                    }
                });
            }
        });

        joinRoom(demoAPI.getRoomUUID(), demoAPI.getRoomToken());
    }

    private void gotoWindowRestore() {
        Intent intent = new Intent(this, WindowRestoreActivity.class);
        startActivity(intent);
    }

    private void lockRatio() {
        ViewGroup.LayoutParams layoutParams = mWhiteboardParent.getLayoutParams();
        int width = mWhiteboardParent.getWidth();
        int height = mWhiteboardParent.getHeight();
        float factor = (float) Math.min(width / 16.0, height / 9.0);
        layoutParams.width = (int) (factor * 16);
        layoutParams.height = (int) (factor * 9);
        mWhiteboardParent.setLayoutParams(layoutParams);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mRoom != null) {
            mRoom.disconnect();
        }
        mWhiteboardView.removeAllViews();
        mWhiteboardView.destroy();
    }

    private void joinRoom(String uuid, String token) {
        logRoomInfo("room uuid: " + uuid + "\nroom token: " + token);
        WhiteSdkConfiguration configuration = new WhiteSdkConfiguration(demoAPI.getAppId(), true);
        /*显示用户头像*/
        configuration.setUserCursor(true);
        //动态 ppt 需要的自定义字体，如果没有使用，无需调用
        configuration.setFonts(new MapBuilder<String, String>().put("宋体", "https://your-cdn.com/Songti.ttf").build());
        // configuration.setEnableSyncedStore(true);
        configuration.setUseMultiViews(true);
        configuration.setEnableSlideInterrupterAPI(true);

        WhiteSdkConfiguration.SlideAppOptions slideAppOptions = new WhiteSdkConfiguration.SlideAppOptions();
        slideAppOptions.setDebug(false);
        slideAppOptions.setShowRenderError(false);
        slideAppOptions.setEnableGlobalClick(false);
        slideAppOptions.setMinFPS(1);
        slideAppOptions.setMaxFPS(2);
        slideAppOptions.setResolution(0.5);
        slideAppOptions.setMaxResolutionLevel(1);
        configuration.setSlideAppOptions(slideAppOptions);

        mWhiteSdk = new WhiteSdk(mWhiteboardView, this, configuration);
        mWhiteSdk.setSlideListener((sourceUrl, resultCaller) -> {
            // ApiService.convertUrl(sourceUrl)
            resultCaller.call(sourceUrl);
        });

        /* 设置自定义全局状态，在后续回调中 GlobalState 直接进行类型转换即可 */
        WhiteDisplayerState.setCustomGlobalStateClass(GlobalState.class);

        // 如需支持用户头像，请在设置 WhiteSdkConfiguration 后，再调用 setUserPayload 方法，传入符合用户信息
        RoomParams roomParams = new RoomParams(uuid, token, DemoAPI.get().getUserId());

        HashMap<String, String> styleMap = new HashMap<>();
        styleMap.put("backgroundColor", "red");
        styleMap.put("top", "12px");
        styleMap.put("left", "60px");
        styleMap.put("position", "fixed");

        // String darkMode = darkModeStyle();
        WindowParams windowParams = new WindowParams()
                .setContainerSizeRatio(3f / 4)
                .setChessboard(true)
                .setDebug(true)
                // .setOverwriteStyles(cursorUserHideStyle())
                // .setOverwriteStyles(darkModeStyle())
                .setCollectorStyles(styleMap);
        // optional
        roomParams.setWindowParams(windowParams);

        mWhiteSdk.joinRoom(roomParams, new RoomListener() {

            @Override
            public void onPhaseChanged(RoomPhase phase) {
                logRoomInfo("onPhaseChanged: " + phase.name());
                showToast(phase.name());
            }

            @Override
            public void onDisconnectWithError(Exception e) {

            }

            @Override
            public void onKickedWithReason(String s) {

            }

            @Override
            public void onRoomStateChanged(RoomState roomState) {
                if (roomState.getWindowBoxState() != null) {
                    logRoomInfo("WindowBoxState " + roomState.getWindowBoxState());
                }
            }

            @Override
            public void onCanUndoStepsUpdate(long l) {

            }

            @Override
            public void onCanRedoStepsUpdate(long l) {

            }

            @Override
            public void onCatchErrorWhenAppendFrame(long l, Exception e) {

            }
        }, new Promise<Room>() {
            @Override
            public void then(Room room) {
                mRoom = room;
            }

            @Override
            public void catchEx(SDKError sdkError) {

            }
        });
    }

    //endregion

    //region log
    void logRoomInfo(String str) {
        Log.i(ROOM_INFO, Thread.currentThread().getStackTrace()[3].getMethodName() + " " + str);
    }

    void logAction(String str) {
        Log.i(ROOM_ACTION, Thread.currentThread().getStackTrace()[3].getMethodName() + " " + str);
    }

    void logAction() {
        Log.i(ROOM_ACTION, Thread.currentThread().getStackTrace()[3].getMethodName());
    }

    void showToast(Object o) {
        Log.i("showToast", o.toString());
        Toast.makeText(this, o.toString(), Toast.LENGTH_SHORT).show();
    }
    //endregion

    private String darkModeStyle() {
        String style = null;
        try {
            style = getStyleFromAsserts("dark-mode.css");
        } catch (IOException ignored) {
        }
        return style;
    }

    private String cursorUserHideStyle() {
        return ".netless-window-manager-cursor-name { display: none }";
    }

    String getStyleFromAsserts(String path) throws IOException {
        StringBuilder style = new StringBuilder();
        InputStream is = getAssets().open(path);
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String str;
            while ((str = br.readLine()) != null) {
                style.append(str);
            }
        }
        return style.toString();
    }
}
