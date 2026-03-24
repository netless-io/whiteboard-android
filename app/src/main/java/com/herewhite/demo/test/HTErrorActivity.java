package com.herewhite.demo.test;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.herewhite.demo.BaseActivity;
import com.herewhite.demo.R;
import com.herewhite.demo.common.DemoAPI;
import com.herewhite.sdk.Room;
import com.herewhite.sdk.RoomCallbacks;
import com.herewhite.sdk.RoomParams;
import com.herewhite.sdk.WhiteSdk;
import com.herewhite.sdk.WhiteSdkConfiguration;
import com.herewhite.sdk.WhiteboardView;
import com.herewhite.sdk.domain.AnimationMode;
import com.herewhite.sdk.domain.PptPage;
import com.herewhite.sdk.domain.Promise;
import com.herewhite.sdk.domain.RoomPhase;
import com.herewhite.sdk.domain.RoomState;
import com.herewhite.sdk.domain.SDKError;
import com.herewhite.sdk.domain.Scene;
import com.herewhite.sdk.domain.WindowParams;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Map;

/**
 * HTError 测试示例
 * 按顺序点击按钮来复现问题：
 * 1. room.removeScenes("/")
 * 2. room.putScenes("/", scenes, 0)
 * 3. room.scalePptToFit(immediately)
 * 4. room.entireScenes
 * 5. room.setScenePath (manager.setMainViewScenePath)
 * 6. room.scalePptToFit(immediately)
 */
public class HTErrorActivity extends BaseActivity {
    static final String TAG = HTErrorActivity.class.getSimpleName();
    private static final String ROOM_INFO = "RoomInfo";

    final DemoAPI demoAPI = DemoAPI.get();
    final Gson gson = new Gson();

    WhiteboardView mWhiteboardView;
    WhiteSdk mWhiteSdk;
    Room mRoom;
    TextView mLogView;

    Scene[] scenes = new Scene[]{
            new Scene("slide0", new PptPage("https://white-pan.oss-cn-shanghai.aliyuncs.com/101/image/alin-rusu-1239275-unsplash_opt.jpg", 600d, 600d)),
            // new Scene("slide1"),
            // new Scene("slide2")
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ht_error);
        getSupportActionBar().hide();

        mWhiteboardView = findViewById(R.id.white);

        setupRoom();

        findViewById(R.id.removeScenes).setOnClickListener(v -> {
            if (mRoom == null) return;
            logAction("removeScenes(\"/\")");
            mRoom.removeScenes("/");
        });

        // 2. room.putScenes("/", scenes, 0)
        findViewById(R.id.runTest).setOnClickListener(v -> {
            if (mRoom == null) return;
            logAction("putScenes(\"/\", " + Arrays.toString(scenes) + ", 0)");
            mRoom.putScenes("/", scenes, 0);
            mRoom.scalePptToFit(AnimationMode.Immediately);
            mRoom.getEntireScenes(new Promise<Map<String, Scene[]>>() {
                @Override
                public void then(Map<String, Scene[]> scenesMap) {
                    for (Map.Entry<String, Scene[]> entry : scenesMap.entrySet()) {
                        logAction("entireScenes: " + entry.getKey() + " -> " + Arrays.toString(entry.getValue()));
                    }

                    // 检查 "/" 路径下是否存在 scenes
                    Scene[] rootScenes = scenesMap.get("/");
                    if (rootScenes != null && rootScenes.length > 0) {
                        logAction("found scenes under /, proceeding with setScenePath and scalePptToFit");
                        String path = "/" + scenes[0].getName();
                        mRoom.setScenePath(path, new Promise<Boolean>() {
                            @Override
                            public void then(Boolean success) {
                                logAction("setScenePath success: " + success);
                                mRoom.scalePptToFit();
                                logAction("scalePptToFit called");
                            }

                            @Override
                            public void catchEx(SDKError t) {
                                logAction("setScenePath error: " + t.getMessage());
                            }
                        });
                    } else {
                        logAction("no scenes found under /, skip setScenePath and scalePptToFit");
                    }
                }

                @Override
                public void catchEx(SDKError t) {
                    logAction("entireScenes error: " + t.getMessage());
                }
            });
        });
    }

    private void setupRoom() {
        DemoAPI.Result result = new DemoAPI.Result() {
            @Override
            public void success(String uuid, String token) {
                joinRoom(uuid, token);
            }

            @Override
            public void fail(String message) {
                showAlert("创建房间失败", message);
            }
        };

        String uuid = demoAPI.getRoomUUID();
        if (uuid != null) {
            demoAPI.getRoomToken(uuid, result);
        } else {
            demoAPI.getNewRoom(result);
        }
    }

    private void joinRoom(String uuid, String token) {
        logRoomInfo("room uuid: " + uuid + "\nroom token: " + token);

        WhiteSdkConfiguration configuration = new WhiteSdkConfiguration(demoAPI.getAppId(), true);
        configuration.setUseMultiViews(true);
        mWhiteSdk = new WhiteSdk(mWhiteboardView, this, configuration);

        RoomParams roomParams = new RoomParams(uuid, token, DemoAPI.get().getUserId());
        roomParams.setDisableNewPencil(false);
        roomParams.setWritable(true);
        roomParams.setWindowParams(new WindowParams().setChessboard(false));

        mWhiteSdk.joinRoom(roomParams, new RoomCallbacks() {
            @Override
            public void onCanUndoStepsUpdate(long canUndoSteps) {
            }

            @Override
            public void onCanRedoStepsUpdate(long canRedoSteps) {
            }

            @Override
            public void onCatchErrorWhenAppendFrame(long userId, Exception error) {
                logRoomInfo("onCatchErrorWhenAppendFrame: " + error.getMessage());
            }

            @Override
            public void onPhaseChanged(RoomPhase phase) {
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
                logRoomInfo("onRoomStateChanged: " + gson.toJson(modifyState));
                logRoomInfo("currentScenePath: " + mRoom.getSceneState().getScenePath());
            }
        }, new Promise<Room>() {
            @Override
            public void then(Room room) {
                mRoom = room;
                logAction("joinRoom success");
                mRoom.setContainerSizeRatio(2 / 3f);

//                mWhiteboardView.postDelayed(() -> {
                    injectCssFromAssets(mWhiteboardView, HTErrorActivity.this, "whiteboard/netless.css");
                    injectCssFromAssets(mWhiteboardView, HTErrorActivity.this, "whiteboard/main.css");
//                }, 10);
                FrameLayout.LayoutParams params =
                        new FrameLayout.LayoutParams(
                                FrameLayout.LayoutParams.MATCH_PARENT,
                                FrameLayout.LayoutParams.MATCH_PARENT
                        );

                mWhiteboardView.setLayoutParams(params);
            }

            @Override
            public void catchEx(SDKError t) {
                logAction("joinRoom fail: " + t.getMessage());
                showToast(t.getMessage());
            }
        });
    }

    void logRoomInfo(String str) {
        Log.i(ROOM_INFO, Thread.currentThread().getStackTrace()[3].getMethodName() + " " + str);
    }

    void logAction(String str) {
        Log.i(TAG, str);
        runOnUiThread(() -> {
            if (mLogView != null) {
                String text = str + "\n" + mLogView.getText().toString();
                mLogView.setText(text);
            }
        });
    }

    public static void injectCssFromAssets(WebView webView, Context context, String assetFileName) {
        String css = readAssetText(context, assetFileName);
        if (css == null) {
            return;
        }

        String styleId = assetFileName.replaceAll("[^a-zA-Z0-9]", "_");
        styleId = "android-netless-id";
        injectCss(webView, css, styleId);
    }

    public static void injectCss(WebView webView, String css, String styleId) {
//        String escapedCss = css
//                .replace("\\", "\\\\")
//                .replace("`", "\\`")
//                .replace("$", "\\$");

        String escaped = css
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "");
        String escapedCss =  "\"" + escaped + "\"";

//        String js =
//                "(function() {" +
//                        "var style = document.getElementById('" + styleId + "');" +
//                        "if (!style) {" +
//                        "   style = document.createElement('style');" +
//                        "   style.id = '" + styleId + "';" +
//                        "   document.head.appendChild(style);" +
//                        "}" +
//                        "style.textContent = `" + escapedCss + "`;" +
//                        "})();";

        String js =
                "(function() {" +
                        "var s = document.createElement('style');" +
                        "s.textContent = " + escapedCss + ";" +
                        "document.head.appendChild(s);" +
                        "})();";

        webView.evaluateJavascript(js, null);
    }

    private static String readAssetText(Context context, String fileName) {
        InputStream inputStream = null;
        BufferedReader reader = null;
        try {
            inputStream = context.getAssets().open(fileName);
            reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line).append('\n');
            }
            return builder.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                if (reader != null) reader.close();
            } catch (IOException ignored) {}
            try {
                if (inputStream != null) inputStream.close();
            } catch (IOException ignored) {}
        }
    }
}
