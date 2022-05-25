
package com.herewhite.demo.test;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.herewhite.demo.BaseActivity;
import com.herewhite.demo.R;
import com.herewhite.demo.StartActivity;
import com.herewhite.demo.common.DemoAPI;
import com.herewhite.sdk.Room;
import com.herewhite.sdk.RoomCallbacks;
import com.herewhite.sdk.RoomParams;
import com.herewhite.sdk.WhiteSdk;
import com.herewhite.sdk.WhiteSdkConfiguration;
import com.herewhite.sdk.WhiteboardView;
import com.herewhite.sdk.domain.PptPage;
import com.herewhite.sdk.domain.Promise;
import com.herewhite.sdk.domain.RoomPhase;
import com.herewhite.sdk.domain.RoomState;
import com.herewhite.sdk.domain.SDKError;
import com.herewhite.sdk.domain.Scene;
import com.herewhite.sdk.domain.SceneState;

public class ScenesActivity extends BaseActivity implements View.OnClickListener {
    static final String TAG = ScenesActivity.class.getSimpleName();

    private static final String ROOM_INFO = "RoomInfo";
    final String SCENE_DIR = "/scene_dir";

    final DemoAPI demoAPI = DemoAPI.get();
    WhiteboardView mWhiteboardView;
    WhiteSdk mWhiteSdk;
    Room mRoom;

    Scene[] scenes = new Scene[]{
            new Scene("slide0", new PptPage("https://white-pan.oss-cn-shanghai.aliyuncs.com/101/image/alin-rusu-1239275-unsplash_opt.jpg", 600d, 600d)),
            new Scene("slide1"),
            new Scene("slide2")};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scenes);
        getSupportActionBar().hide();
        mWhiteboardView = findViewById(R.id.white);

        setupRoom();

        findViewById(R.id.putScenes).setOnClickListener(this);
        findViewById(R.id.setScenePath).setOnClickListener(this);
        findViewById(R.id.setSceneIndex).setOnClickListener(this);
        findViewById(R.id.getRoomState).setOnClickListener(this);
    }

    // sample of scenes apis
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.putScenes:
                // create slides on scenes
                // it should run only once
                mRoom.putScenes(SCENE_DIR, scenes, 0);
                mRoom.setScenePath(SCENE_DIR);
                break;
            case R.id.setScenePath:
                mRoom.setScenePath(SCENE_DIR + "/" + scenes[1].getName());
                break;
            case R.id.setSceneIndex:
                // switch scene by index
                mRoom.setSceneIndex(2, new Promise<Boolean>() {
                    @Override
                    public void then(Boolean aBoolean) {
                    }

                    @Override
                    public void catchEx(SDKError t) {
                    }
                });
                break;
            case R.id.getRoomState:
                // sync SceneState
                SceneState sceneState = mRoom.getSceneState();
                logRoomInfo("current sceneState " + sceneState.toString());

                // get sceneState by async
                mRoom.getSceneState(new Promise<SceneState>() {
                    @Override
                    public void then(SceneState sceneState) {
                        logRoomInfo("current sceneState " + sceneState.toString());
                    }

                    @Override
                    public void catchEx(SDKError t) {

                    }
                });
                break;
        }
    }

    private void setupRoom() {
        String uuid = getIntent().getStringExtra(StartActivity.EXTRA_ROOM_UUID);

        DemoAPI.Result result = new DemoAPI.Result() {
            @Override
            public void success(String uuid, String token) {
                joinRoom(uuid, token);
            }

            @Override
            public void fail(String message) {
                // alert("创建房间失败", message);
            }
        };

        if (uuid != null) {
            demoAPI.getRoomToken(uuid, result);
        } else {
            demoAPI.getNewRoom(result);
        }
    }

    //region room
    private void joinRoom(String uuid, String token) {
        logRoomInfo("room uuid: " + uuid + "\nroom token: " + token);

        WhiteSdkConfiguration configuration = new WhiteSdkConfiguration(demoAPI.getAppId(), true);
        mWhiteSdk = new WhiteSdk(mWhiteboardView, this, configuration);

        RoomParams roomParams = new RoomParams(uuid, token, DemoAPI.DEFAULT_UID);
        roomParams.setDisableNewPencil(false);
        roomParams.setWritable(true);

        mWhiteSdk.joinRoom(roomParams, new RoomCallbacks() {
            @Override
            public void onCanUndoStepsUpdate(long canUndoSteps) {
            }

            @Override
            public void onCanRedoStepsUpdate(long canRedoSteps) {
            }

            @Override
            public void onCatchErrorWhenAppendFrame(long userId, Exception error) {
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
                logRoomInfo("onRoomStateChanged: " + modifyState);
                if (modifyState.getSceneState() != null) {
                    // callback when sceneState changed
                    SceneState sceneState = modifyState.getSceneState();
                    logRoomInfo("sceneState changed " + sceneState.toString());
                }
            }
        }, new Promise<Room>() {
            @Override
            public void then(Room room) {
                mRoom = room;
            }

            @Override
            public void catchEx(SDKError t) {
                showToast(t.getMessage());
            }
        });
    }

    void logRoomInfo(String str) {
        Log.i(ROOM_INFO, Thread.currentThread().getStackTrace()[3].getMethodName() + " " + str);
    }
}
