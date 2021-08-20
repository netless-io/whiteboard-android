
package com.herewhite.demo.test.window;

import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.herewhite.demo.DemoAPI;
import com.herewhite.demo.LocalFileWebViewClient;
import com.herewhite.demo.R;
import com.herewhite.demo.utils.MapBuilder;
import com.herewhite.sdk.CommonCallback;
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

import org.json.JSONObject;

import wendu.dsbridge.DWebView;


public class WindowTestActivity extends AppCompatActivity {
    private static final String ROOM_INFO = "RoomInfo";
    private static final String ROOM_ACTION = "RoomAction";

    final Gson gson = new Gson();
    final DemoAPI demoAPI = new DemoAPI();

    WhiteboardView mWhiteboardView;
    WhiteSdk mWhiteSdk;
    Room mRoom;
    FrameLayout mWhiteboardParent;
    DragViewPlugin dragViewPlugin;
    UserSyncedState state = new UserSyncedState();

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

        // 插入播放器
        findViewById(R.id.insertPlayer).setOnClickListener(v -> {
            WindowAppParam appParam = WindowAppParam.createMediaPlayerApp("https://white-pan.oss-cn-shanghai.aliyuncs.com/101/oceans.mp4", "player");
            mRoom.addApp(appParam, new Promise<String>() {
                @Override
                public void then(String appId) {
                    logRoomInfo("create App id " + appId);
                }

                @Override
                public void catchEx(SDKError t) {

                }
            });
        });

        // 插入静态场景
        findViewById(R.id.insertStatic).setOnClickListener(v -> {
            // 由转换后信息序列化
            String ppts = "[{\"name\":\"1\",\"ppt\":{\"height\":1010.0,\"src\":\"https://convertcdn.netless.link/staticConvert/0764816000c411ecbfbbb9230f6dd80f/1.png\",\"width\":714.0}},{\"name\":\"2\",\"ppt\":{\"height\":1010.0,\"src\":\"https://convertcdn.netless.link/staticConvert/0764816000c411ecbfbbb9230f6dd80f/2.png\",\"width\":714.0}},{\"name\":\"3\",\"ppt\":{\"height\":1010.0,\"src\":\"https://convertcdn.netless.link/staticConvert/0764816000c411ecbfbbb9230f6dd80f/3.png\",\"width\":714.0}},{\"name\":\"4\",\"ppt\":{\"height\":1010.0,\"src\":\"https://convertcdn.netless.link/staticConvert/0764816000c411ecbfbbb9230f6dd80f/4.png\",\"width\":714.0}}]";
            Scene[] scenes = gson.fromJson(ppts, Scene[].class);

            WindowAppParam param = WindowAppParam.createDocsViewerApp("/static", scenes, "static");
            mRoom.addApp(param, null);
        });

        // 插入动态场景
        findViewById(R.id.insertDynamic).setOnClickListener(v -> {
            // 由转换后信息序列化
            String ppts = "[{\"name\":\"1\",\"ppt\":{\"height\":720.0,\"previewURL\":\"https://convertcdn.netless.link/dynamicConvert/9e9d482000ae11ecbfbbb9230f6dd80f/preview/1.png\",\"src\":\"pptx://convertcdn.netless.link/dynamicConvert/9e9d482000ae11ecbfbbb9230f6dd80f/1.slide\",\"width\":960.0}},{\"name\":\"2\",\"ppt\":{\"height\":720.0,\"previewURL\":\"https://convertcdn.netless.link/dynamicConvert/9e9d482000ae11ecbfbbb9230f6dd80f/preview/2.png\",\"src\":\"pptx://convertcdn.netless.link/dynamicConvert/9e9d482000ae11ecbfbbb9230f6dd80f/2.slide\",\"width\":960.0}},{\"name\":\"3\",\"ppt\":{\"height\":720.0,\"previewURL\":\"https://convertcdn.netless.link/dynamicConvert/9e9d482000ae11ecbfbbb9230f6dd80f/preview/3.png\",\"src\":\"pptx://convertcdn.netless.link/dynamicConvert/9e9d482000ae11ecbfbbb9230f6dd80f/3.slide\",\"width\":960.0}},{\"name\":\"4\",\"ppt\":{\"height\":720.0,\"previewURL\":\"https://convertcdn.netless.link/dynamicConvert/9e9d482000ae11ecbfbbb9230f6dd80f/preview/4.png\",\"src\":\"pptx://convertcdn.netless.link/dynamicConvert/9e9d482000ae11ecbfbbb9230f6dd80f/4.slide\",\"width\":960.0}},{\"name\":\"5\",\"ppt\":{\"height\":720.0,\"previewURL\":\"https://convertcdn.netless.link/dynamicConvert/9e9d482000ae11ecbfbbb9230f6dd80f/preview/5.png\",\"src\":\"pptx://convertcdn.netless.link/dynamicConvert/9e9d482000ae11ecbfbbb9230f6dd80f/5.slide\",\"width\":960.0}},{\"name\":\"6\",\"ppt\":{\"height\":720.0,\"previewURL\":\"https://convertcdn.netless.link/dynamicConvert/9e9d482000ae11ecbfbbb9230f6dd80f/preview/6.png\",\"src\":\"pptx://convertcdn.netless.link/dynamicConvert/9e9d482000ae11ecbfbbb9230f6dd80f/6.slide\",\"width\":960.0}},{\"name\":\"7\",\"ppt\":{\"height\":720.0,\"previewURL\":\"https://convertcdn.netless.link/dynamicConvert/9e9d482000ae11ecbfbbb9230f6dd80f/preview/7.png\",\"src\":\"pptx://convertcdn.netless.link/dynamicConvert/9e9d482000ae11ecbfbbb9230f6dd80f/7.slide\",\"width\":960.0}},{\"name\":\"8\",\"ppt\":{\"height\":720.0,\"previewURL\":\"https://convertcdn.netless.link/dynamicConvert/9e9d482000ae11ecbfbbb9230f6dd80f/preview/8.png\",\"src\":\"pptx://convertcdn.netless.link/dynamicConvert/9e9d482000ae11ecbfbbb9230f6dd80f/8.slide\",\"width\":960.0}},{\"name\":\"9\",\"ppt\":{\"height\":720.0,\"previewURL\":\"https://convertcdn.netless.link/dynamicConvert/9e9d482000ae11ecbfbbb9230f6dd80f/preview/9.png\",\"src\":\"pptx://convertcdn.netless.link/dynamicConvert/9e9d482000ae11ecbfbbb9230f6dd80f/9.slide\",\"width\":960.0}}]";
            Scene[] scenes = gson.fromJson(ppts, Scene[].class);

            WindowAppParam param = WindowAppParam.createDocsViewerApp("/dynamic", scenes, "dynamic");
            mRoom.addApp(param, null);
        });

        // 插入本地同步信息
        findViewById(R.id.insertLocal).setOnClickListener(v -> {
            if (mRoom != null) {
                // 插入方初始化状态
                state = new UserSyncedState();
                state.dragViewState.w = 0.3f;
                state.dragViewState.h = 0.2f;
                mRoom.safeSetAttributes(state);
            }

            updateDragView();
            dragViewPlugin.startTimer(120_000);
        });

        // 16:9 限定
        findViewById(R.id.lockRatio).setOnClickListener(v -> {
            lockRatio();
        });

        joinRoom(demoAPI.getDemoUUID(), demoAPI.getDemoToken());
    }

    long lastUpdate = 0;

    private void safeUpdateAttributes() {
        if (mRoom != null && System.currentTimeMillis() - lastUpdate > 50) {
            lastUpdate = System.currentTimeMillis();
            mRoom.safeUpdateAttributes(new String[]{"dragViewState"}, state.dragViewState);
        }
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

    private void updateDragView() {
        if (dragViewPlugin == null) {
            dragViewPlugin = new DragViewPlugin(this);
            dragViewPlugin.setListener((dragViewState) -> {
                state.dragViewState = dragViewState;
                safeUpdateAttributes();
            });
            mWhiteboardParent.addView(dragViewPlugin, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        }

        dragViewPlugin.updateState(state.dragViewState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mRoom != null) {
            mRoom.disconnect();
        }
    }

    private void joinRoom(String uuid, String token) {
        logRoomInfo("room uuid: " + uuid + "\nroom token: " + token);
        WhiteSdkConfiguration configuration = new WhiteSdkConfiguration(demoAPI.getAppId(), true);
        /*显示用户头像*/
        configuration.setUserCursor(true);
        //动态 ppt 需要的自定义字体，如果没有使用，无需调用
        configuration.setFonts(new MapBuilder<String, String>().put("宋体", "https://your-cdn.com/Songti.ttf").build());
        configuration.setEnableSyncedStore(true);

        mWhiteSdk = new WhiteSdk(mWhiteboardView, this, configuration);
        mWhiteSdk.setCommonCallbacks(new CommonCallback() {
            @Override
            public void onLogger(JSONObject object) {
                logAction(object.toString());
            }
        });

        /** 设置自定义全局状态，在后续回调中 GlobalState 直接进行类型转换即可 */
        WhiteDisplayerState.setCustomGlobalStateClass(GlobalState.class);

        // 如需支持用户头像，请在设置 WhiteSdkConfiguration 后，再调用 setUserPayload 方法，传入符合用户信息
        RoomParams roomParams = new RoomParams(uuid, token);
        roomParams.setUseMultiViews(true);

        mWhiteSdk.joinRoom(roomParams, new RoomListener() {
            @Override
            public void onCanUndoStepsUpdate(long canUndoSteps) {
                logRoomInfo("canUndoSteps: " + canUndoSteps);
            }

            @Override
            public void onCanRedoStepsUpdate(long canRedoSteps) {
                logRoomInfo("onCanRedoStepsUpdate: " + canRedoSteps);
            }

            @Override
            public void onCatchErrorWhenAppendFrame(long userId, Exception error) {
                logRoomInfo("onCatchErrorWhenAppendFrame: " + userId + " error " + error.getMessage());
            }

            @Override
            public void onPhaseChanged(RoomPhase phase) {
                //在此处可以处理断连后的重连逻辑
                logRoomInfo("onPhaseChanged: " + phase.name());
                showToast(phase.name());
            }

            @Override
            public void onDisconnectWithError(Exception e) {
                logRoomInfo("onDisconnectWithError: " + e.getMessage());
            }

            @Override
            public void onKickedWithReason(String reason) {
                logRoomInfo("onKickedWithReason: " + reason);
            }

            @Override
            public void onRoomStateChanged(RoomState modifyState) {
                logRoomInfo("onRoomStateChanged:" + gson.toJson(modifyState));
            }

            @Override
            public void onAttributesUpdate(String valueOf) {
                state = gson.fromJson(valueOf, UserSyncedState.class);
                logRoomInfo("onAttributesUpdate:" + state);
                updateDragView();
            }
        }, new Promise<Room>() {
            @Override
            public void then(Room room) {
                mRoom = room;
            }

            @Override
            public void catchEx(SDKError t) {
                logRoomInfo("native join fail: " + t.getMessage());
                showToast(t.getMessage());
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
}
