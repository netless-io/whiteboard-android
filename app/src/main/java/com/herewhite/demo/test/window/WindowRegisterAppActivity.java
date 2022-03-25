
package com.herewhite.demo.test.window;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.herewhite.demo.R;
import com.herewhite.demo.common.DemoAPI;
import com.herewhite.sdk.AbstractRoomCallbacks;
import com.herewhite.sdk.CommonCallback;
import com.herewhite.sdk.Room;
import com.herewhite.sdk.RoomParams;
import com.herewhite.sdk.WhiteSdk;
import com.herewhite.sdk.WhiteSdkConfiguration;
import com.herewhite.sdk.WhiteboardView;
import com.herewhite.sdk.domain.Promise;
import com.herewhite.sdk.domain.RoomPhase;
import com.herewhite.sdk.domain.RoomState;
import com.herewhite.sdk.domain.SDKError;
import com.herewhite.sdk.domain.WindowAppParam;
import com.herewhite.sdk.domain.WindowParams;
import com.herewhite.sdk.domain.WindowRegisterAppParams;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;

import wendu.dsbridge.DWebView;

public class WindowRegisterAppActivity extends AppCompatActivity {
    private static final String ROOM_INFO = "RoomInfo";
    private static final String ROOM_ACTION = "RoomAction";

    final Gson gson = new Gson();
    final DemoAPI demoAPI = DemoAPI.get();

    WhiteboardView mWhiteboardView;
    WhiteSdk mWhiteSdk;
    Room mRoom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_window_register_app);
        getSupportActionBar().hide();

        mWhiteboardView = findViewById(R.id.white);
        mWhiteboardView.getSettings().setAllowUniversalAccessFromFileURLs(true);

        findViewById(R.id.addMonaco).setOnClickListener(v -> {
            WindowAppParam.Options options = new WindowAppParam.Options("VSCode");
            WindowAppParam appParam = new WindowAppParam(
                    "Monaco",
                    options,
                    null
            );
            mRoom.addApp(appParam, new Promise<String>() {
                @Override
                public void then(String appId) {

                }

                @Override
                public void catchEx(SDKError t) {

                }
            });
        });

        findViewById(R.id.addEmbedPage).setOnClickListener(v -> {
            WindowAppParam appParam = new WindowAppParam(
                    "EmbeddedPage",
                    new EmbedPageOptions("A Embed Page", "/embedPage"),
                    new EmbedPageAttributes("https://www.baidu.com")
            );
            mRoom.addApp(appParam, new Promise<String>() {
                @Override
                public void then(String appId) {

                }

                @Override
                public void catchEx(SDKError t) {

                }
            });
        });


        joinRoom(demoAPI.getRoomUUID(), demoAPI.getRoomToken());
    }

    static class EmbedPageOptions extends WindowAppParam.Options {
        private final String scenePath;

        public EmbedPageOptions(String title, String scenePath) {
            super(title);
            this.scenePath = scenePath;
        }
    }

    static class EmbedPageAttributes extends WindowAppParam.Attributes {
        private final String src;

        public EmbedPageAttributes(String src) {
            this.src = src;
        }
    }

    private void registerMonaco() {
        // register local script
        String jsString = getAppJsFromAsserts("app/monaco.iife.js");
        String kind = "Monaco";
        String variable = "NetlessAppMonaco.default";

        WindowRegisterAppParams params = new WindowRegisterAppParams(
                jsString,
                kind,
                variable,
                Collections.emptyMap()
        );
        mWhiteSdk.registerApp(params);

        // register remote script
        String url = "https://cdn.jsdelivr.net/npm/@netless/app-monaco@0.1.13-beta.0/dist/main.iife.js";
        WindowRegisterAppParams paramsRemote = new WindowRegisterAppParams(
                url,
                kind,
                Collections.emptyMap()
        );
        // mWhiteSdk.registerApp(params);
    }

    private void registerEmbedPage() {
        String jsString = getAppJsFromAsserts("app/embedPage.iife.js");
        String kind = "EmbeddedPage";
        String variable = "NetlessAppEmbeddedPage.default";

        WindowRegisterAppParams params = new WindowRegisterAppParams(
                jsString,
                kind,
                variable,
                Collections.emptyMap()
        );
        mWhiteSdk.registerApp(params);
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
        configuration.setUserCursor(true);
        configuration.setUseMultiViews(true);

        mWhiteSdk = new WhiteSdk(mWhiteboardView, this, configuration);
        mWhiteSdk.setCommonCallbacks(new CommonCallback() {
            @Override
            public void onLogger(JSONObject object) {
                logAction(object.toString());
            }
        });

        // 如需支持用户头像，请在设置 WhiteSdkConfiguration 后，再调用 setUserPayload 方法，传入符合用户信息
        RoomParams roomParams = new RoomParams(uuid, token, DemoAPI.DEFAULT_UID);
        roomParams.setWritable(true);

        HashMap<String, String> styleMap = new HashMap<>();
        styleMap.put("bottom", "12px");
        styleMap.put("left", "60px");
        styleMap.put("position", "fixed");
        WindowParams windowParams = new WindowParams()
                .setContainerSizeRatio(3f / 4)
                .setChessboard(true)
                .setDebug(true)
                .setCollectorStyles(styleMap);

        roomParams.setWindowParams(windowParams);

        registerMonaco();
        registerEmbedPage();

        mWhiteSdk.joinRoom(roomParams, new AbstractRoomCallbacks() {

            @Override
            public void onPhaseChanged(RoomPhase phase) {
                logRoomInfo("onPhaseChanged: " + phase.name());
                showToast(phase.name());
            }

            @Override
            public void onRoomStateChanged(RoomState roomState) {
                if (roomState.getWindowBoxState() != null) {
                    logRoomInfo("WindowBoxState " + roomState.getWindowBoxState());
                }
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

    private String getAppJsFromAsserts(String path) {
        String result = null;
        try {
            result = getStringFromAsserts(path);
        } catch (IOException ignored) {
        }
        return result;
    }

    String getStringFromAsserts(String path) throws IOException {
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
