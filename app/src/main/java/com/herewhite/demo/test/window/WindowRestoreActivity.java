package com.herewhite.demo.test.window;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;

import com.herewhite.demo.BaseActivity;
import com.herewhite.demo.R;
import com.herewhite.demo.common.ApiCallback;
import com.herewhite.demo.common.ApiService;
import com.herewhite.demo.common.RoomCreationResult;
import com.herewhite.demo.common.DemoAPI;
import com.herewhite.demo.utils.FileUtils;
import com.herewhite.demo.utils.MapBuilder;
import com.herewhite.sdk.CommonCallback;
import com.herewhite.sdk.Room;
import com.herewhite.sdk.RoomListener;
import com.herewhite.sdk.RoomParams;
import com.herewhite.sdk.WhiteSdk;
import com.herewhite.sdk.WhiteSdkConfiguration;
import com.herewhite.sdk.WhiteboardView;
import com.herewhite.sdk.domain.Promise;
import com.herewhite.sdk.domain.RoomPhase;
import com.herewhite.sdk.domain.RoomState;
import com.herewhite.sdk.domain.SDKError;
import com.herewhite.sdk.domain.WindowParams;

import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;

public class WindowRestoreActivity extends BaseActivity {
    public static final String ATTRIBUTES_FILE = "window_attributes";

    static String roomUuid;
    static String roomToken;

    private DemoAPI demoAPI = DemoAPI.get();
    private WhiteboardView mWhiteboardView;
    private WhiteSdk mWhiteSdk;
    private Room mRoom;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_window_restore);

        mWhiteboardView = findViewById(R.id.white);

        findViewById(R.id.restoreAttributes).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mRoom != null) {
                    mRoom.setWindowManagerAttributes(getAttributesFromCache());
                }
            }
        });
        createNewRoom();
    }

    private void createNewRoom() {
        String sdkToken = DemoAPI.get().getSdkToken();
        ApiService.createRoom(sdkToken, 100, "cn-hz", new ApiCallback<RoomCreationResult>() {
            @Override
            public void onSuccess(RoomCreationResult data) {
                roomUuid = data.uuid;
                ApiService.createRoomToken(sdkToken, roomUuid, "cn-hz", new ApiCallback<String>() {
                    @Override
                    public void onSuccess(String token) {
                        roomToken = token;
                        joinRoom(roomUuid, roomToken);
                    }

                    @Override
                    public void onFailure(String message) {
                        showToast(message);
                    }
                });
            }

            @Override
            public void onFailure(String message) {
                showToast(message);
            }
        });
    }

    private void joinRoom(String uuid, String token) {
        logRoomInfo("room uuid: " + uuid + "\nroom token: " + token);
        WhiteSdkConfiguration configuration = new WhiteSdkConfiguration(DemoAPI.get().getAppId(), true);
        configuration.setUserCursor(true);
        configuration.setFonts(new MapBuilder<String, String>().put("宋体", "https://your-cdn.com/Songti.ttf").build());
        configuration.setUseMultiViews(true);

        mWhiteSdk = new WhiteSdk(mWhiteboardView, this, configuration);
        mWhiteSdk.setCommonCallbacks(new CommonCallback() {
            @Override
            public void onLogger(JSONObject object) {
                logAction(object.toString());
            }
        });
        RoomParams roomParams = new RoomParams(uuid, token, DemoAPI.DEFAULT_UID);

        HashMap<String, String> styleMap = new HashMap<>();
        styleMap.put("backgroundColor", "red");
        styleMap.put("bottom", "12px");
        styleMap.put("left", "60px");
        styleMap.put("position", "fixed");

        WindowParams windowParams = new WindowParams()
                .setContainerSizeRatio(3f / 4)
                .setChessboard(true)
                .setDebug(true)
                .setCollectorStyles(styleMap);
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

    void logRoomInfo(String str) {
        Log.i("RoomInfo", Thread.currentThread().getStackTrace()[3].getMethodName() + " " + str);
    }

    void logAction(String str) {
        Log.i("RoomAction", Thread.currentThread().getStackTrace()[3].getMethodName() + " " + str);
    }

    void logAction() {
        Log.i("RoomAction", Thread.currentThread().getStackTrace()[3].getMethodName());
    }

    private String getAttributesFromCache() {
        try {
            File file = new File(getCacheDir(), ATTRIBUTES_FILE);
            return FileUtils.readFileToString(file);
        } catch (Exception e) {
            // ignore
        }
        return "";
    }
}
