package com.herewhite.demo.test;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import com.google.gson.Gson;
import com.herewhite.demo.BaseActivity;
import com.herewhite.demo.R;
import com.herewhite.demo.StartActivity;
import com.herewhite.demo.common.DemoAPI;
import com.herewhite.demo.utils.MapBuilder;
import com.herewhite.sdk.AbstractRoomCallbacks;
import com.herewhite.sdk.CommonCallback;
import com.herewhite.sdk.ConverterCallbacks;
import com.herewhite.sdk.Room;
import com.herewhite.sdk.RoomCallbacks;
import com.herewhite.sdk.RoomParams;
import com.herewhite.sdk.WhiteSdk;
import com.herewhite.sdk.WhiteSdkConfiguration;
import com.herewhite.sdk.WhiteboardView;
import com.herewhite.sdk.converter.ConvertType;
import com.herewhite.sdk.converter.ConverterV5;
import com.herewhite.sdk.domain.AnimationMode;
import com.herewhite.sdk.domain.ConversionInfo;
import com.herewhite.sdk.domain.ConvertException;
import com.herewhite.sdk.domain.ConvertedFiles;
import com.herewhite.sdk.domain.Promise;
import com.herewhite.sdk.domain.RoomPhase;
import com.herewhite.sdk.domain.RoomState;
import com.herewhite.sdk.domain.SDKError;

import org.json.JSONObject;

import java.util.Date;

/**
 * 依赖 Flat 的转换结果插入 PPT
 */
public class PptOldActivity extends BaseActivity {
    static final String TAG = PptOldActivity.class.getSimpleName();

    private static final String FLAT_CLOUD_FILE_JSON = "{\"convertStep\":\"Done\",\"createAt\":1651735378214,\"fileName\":\"合肥八年级积学提创第10课次.pptx\",\"fileSize\":10900619,\"fileURL\":" +
            "\"https://flat-storage.oss-accelerate.aliyuncs.com/cloud-storage/2022-05/05/c40e6d95-5e5f-471c-a792-672af8341f26/c40e6d95-5e5f-471c-a792-672af8341f26.pptx\"," +
            "\"fileUUID\":\"c40e6d95-5e5f-471c-a792-672af8341f26\",\"region\":\"cn-hz\"," +
            "\"taskToken\":\"NETLESSTASK_YWs9NWJod2NUeXk2MmRZWC11WiZub25jZT0zMjY3Zjk0MC1jYzQ0LTExZWMtYWJlMy1hM2FkZGI1NWEzYzImcm9sZT0yJnNpZz0yNDc1NjAxNDk4NjYyNjRhNmUxYjU0NTlmMjQxNDA2OTU4YjNhZWJjYzlkNWQ5MGUyZDYwZjNjMDBmYWZlMGQ3JnV1aWQ9MzI1NDk4NTBjYzQ0MTFlYzgxYWM0MzlhZDBmYWEzZTQ\"," +
            "\"taskUUID\":\"32549850cc4411ec81ac439ad0faa3e4\"}";
    private static final String EVENT_NAME = "WhiteCommandCustomEvent";
    private static final String ROOM_INFO = "RoomInfo";
    private static final String ROOM_ACTION = "RoomAction";
    final String SCENE_DIR = "/dynamic";
    final Gson gson = new Gson();
    final DemoAPI demoAPI = DemoAPI.get();
    WhiteboardView mWhiteboardView;
    WhiteSdk mWhiteSdk;
    Room mRoom;
    RoomCallbacks mRoomCallbackHock = new AbstractRoomCallbacks() {
    };
    // Room Params
    private String uuid;
    private String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ppt_old);
        getSupportActionBar().hide();

        mWhiteboardView = findViewById(R.id.white);

        findViewById(R.id.insertPpt).setOnClickListener(v -> insertPpt(SCENE_DIR));

        findViewById(R.id.removePpt).setOnClickListener(v -> mRoom.removeScenes(SCENE_DIR));

        findViewById(R.id.prevPpt).setOnClickListener(v -> mRoom.pptPreviousStep());

        findViewById(R.id.nextPpt).setOnClickListener(v -> mRoom.pptNextStep());

        setupRoom();
    }

    public void insertPpt(String dir) {
        CloudFile cloudStorageFile = gson.fromJson(FLAT_CLOUD_FILE_JSON, CloudFile.class);
        ConverterV5 converterV5 = new ConverterV5.Builder()
                .setResource(cloudStorageFile.fileURL)
                .setType(ConvertType.Dynamic)
                .setTaskUuid(cloudStorageFile.taskUUID)
                .setTaskToken(cloudStorageFile.taskToken)
                .setCallback(new ConverterCallbacks() {
                    @Override
                    public void onProgress(Double progress, ConversionInfo convertInfo) {

                    }

                    @Override
                    public void onFinish(ConvertedFiles ppt, ConversionInfo convertInfo) {
                        mRoom.putScenes(dir, ppt.getScenes(), 0);
                        mRoom.setScenePath(dir + "/1");
                    }

                    @Override
                    public void onFailure(ConvertException e) {

                    }
                }).build();
        converterV5.startConvertTask();
    }

    private void setupRoom() {

        DemoAPI.Result result = new DemoAPI.Result() {
            @Override
            public void success(String uuid, String token) {
                joinRoom(uuid, token);
            }

            @Override
            public void fail(String message) {
                alert("创建房间失败", message);
            }
        };

        String uuid = demoAPI.getRoomUUID();
        if (uuid != null) {
            demoAPI.getRoomToken(uuid, result);
        } else {
            demoAPI.getNewRoom(result);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    //region room
    private void joinRoom(String uuid, String token) {
        logRoomInfo("room uuid: " + uuid + "\nroom token: " + token);

        //存档一下，方便重连
        this.uuid = uuid;
        this.token = token;

        WhiteSdkConfiguration configuration = new WhiteSdkConfiguration(demoAPI.getAppId(), true);
        /*显示用户头像*/
        configuration.setUserCursor(true);
        //动态 ppt 需要的自定义字体，如果没有使用，无需调用
        configuration.setFonts(new MapBuilder<String, String>().put("宋体", "https://your-cdn.com/Songti.ttf").build());

        mWhiteSdk = new WhiteSdk(mWhiteboardView, this, configuration);

        //图片替换 API，需要在 whiteSDKConfig 中先行调用 setHasUrlInterrupterAPI，进行设置，否则不会被回调。
        mWhiteSdk.setCommonCallbacks(new CommonCallback() {
            @Override
            public String urlInterrupter(String sourceUrl) {
                return sourceUrl;
            }

            @Override
            public void onMessage(JSONObject message) {
                Log.d(TAG, message.toString());
            }

            @Override
            public void sdkSetupFail(SDKError error) {
                Log.e(TAG, "sdkSetupFail " + error.toString());
            }

            @Override
            public void throwError(Object args) {
                Log.e(TAG, "throwError " + args);
            }

            @Override
            public void onPPTMediaPlay() {
                logAction();
            }

            @Override
            public void onPPTMediaPause() {
                logAction();
            }

            @Override
            public void onLogger(JSONObject object) {
                logAction(object.toString());
            }
        });

        //如需支持用户头像，请在设置 WhiteSdkConfiguration 后，再调用 setUserPayload 方法，传入符合用户信息
        RoomParams roomParams = new RoomParams(uuid, token, DemoAPI.get().getUserId());
        roomParams.setDisableNewPencil(false);
        roomParams.setWritable(true);

        final Date joinDate = new Date();
        logRoomInfo("native join " + joinDate);
        mWhiteSdk.joinRoom(roomParams, new RoomCallbacks() {
            @Override
            public void onCanUndoStepsUpdate(long canUndoSteps) {
                mRoomCallbackHock.onCanUndoStepsUpdate(canUndoSteps);
                logRoomInfo("canUndoSteps: " + canUndoSteps);
            }

            @Override
            public void onCanRedoStepsUpdate(long canRedoSteps) {
                mRoomCallbackHock.onCanRedoStepsUpdate(canRedoSteps);
                logRoomInfo("onCanRedoStepsUpdate: " + canRedoSteps);
            }

            @Override
            public void onCatchErrorWhenAppendFrame(long userId, Exception error) {
                mRoomCallbackHock.onCatchErrorWhenAppendFrame(userId, error);
                logRoomInfo("onCatchErrorWhenAppendFrame: " + userId + " error " + error.getMessage());
            }

            @Override
            public void onPhaseChanged(RoomPhase phase) {
                mRoomCallbackHock.onPhaseChanged(phase);
                //在此处可以处理断连后的重连逻辑
                logRoomInfo("onPhaseChanged: " + phase.name());
                showToast(phase.name());
            }

            @Override
            public void onDisconnectWithError(Exception e) {
                mRoomCallbackHock.onDisconnectWithError(e);
                logRoomInfo("onDisconnectWithError: " + e.getMessage());
            }

            @Override
            public void onKickedWithReason(String reason) {
                mRoomCallbackHock.onKickedWithReason(reason);
                logRoomInfo("onKickedWithReason: " + reason);
            }

            @Override
            public void onRoomStateChanged(RoomState modifyState) {
                mRoomCallbackHock.onRoomStateChanged(modifyState);
                logRoomInfo("onRoomStateChanged:" + gson.toJson(modifyState));
            }
        }, new Promise<Room>() {
            @Override
            public void then(Room room) {
                //记录加入房间消耗的时长
                logRoomInfo("native join in room duration: " + (System.currentTimeMillis() - joinDate.getTime()) / 1000f + "s");
                mRoom = room;
                addCustomEventListener();

                testMarkIdling(true);
            }

            @Override
            public void catchEx(SDKError t) {
                logRoomInfo("native join fail: " + t.getMessage());
                showToast(t.getMessage());
            }
        });
    }

    //region private
    private void alert(final String title, final String detail) {
        runOnUiThread(() -> {
            AlertDialog alertDialog = new AlertDialog.Builder(PptOldActivity.this).create();
            alertDialog.setTitle(title);
            alertDialog.setMessage(detail);
            alertDialog.setButton(
                    AlertDialog.BUTTON_NEUTRAL,
                    "OK",
                    (dialog, which) -> {
                        dialog.dismiss();
                        finish();
                    });
            alertDialog.show();
        });
    }
    //endregion

    private void addCustomEventListener() {
        mRoom.addMagixEventListener(EVENT_NAME, event -> {
            logRoomInfo("customEvent payload: " + event.getPayload().toString());
            showToast(gson.toJson(event.getPayload()));
        });
    }

    public void scalePptToFit(MenuItem item) {
        mRoom.scalePptToFit(AnimationMode.Continuous);
    }

    public void reconnect(MenuItem item) {
        testMarkIdling(false);
        mRoom.disconnect(new Promise<Object>() {
            @Override
            public void then(Object b) {
                joinRoom(PptOldActivity.this.uuid, PptOldActivity.this.token);
            }

            @Override
            public void catchEx(SDKError t) {

            }
        });
    }

    public void setWritableFalse(MenuItem item) {
        mRoom.setWritable(false, new Promise<Boolean>() {
            @Override
            public void then(Boolean aBoolean) {
                logRoomInfo("room writable: " + aBoolean);
            }

            @Override
            public void catchEx(SDKError t) {

            }
        });
    }

    public void setWritableTrue(MenuItem item) {
        mRoom.setWritable(true, new Promise<Boolean>() {
            @Override
            public void then(Boolean aBoolean) {
                logRoomInfo("room writable: " + aBoolean);
            }

            @Override
            public void catchEx(SDKError t) {

            }
        });
    }

    void logRoomInfo(String str) {
        Log.i(ROOM_INFO, Thread.currentThread().getStackTrace()[3].getMethodName() + " " + str);
    }

    void logAction(String str) {
        Log.i(ROOM_ACTION, Thread.currentThread().getStackTrace()[3].getMethodName() + " " + str);
    }

    void logAction() {
        Log.i(ROOM_ACTION, Thread.currentThread().getStackTrace()[3].getMethodName());
    }

    static class CloudFile {
        public String fileURL;
        public String taskUUID;
        public String taskToken;
    }
}
