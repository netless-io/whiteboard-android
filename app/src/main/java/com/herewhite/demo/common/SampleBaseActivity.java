package com.herewhite.demo.common;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.herewhite.demo.BaseActivity;
import com.herewhite.demo.R;
import com.herewhite.demo.StartActivity;
import com.herewhite.sdk.AbstractRoomCallbacks;
import com.herewhite.sdk.Room;
import com.herewhite.sdk.RoomCallbacks;
import com.herewhite.sdk.RoomParams;
import com.herewhite.sdk.WhiteSdk;
import com.herewhite.sdk.WhiteSdkConfiguration;
import com.herewhite.sdk.WhiteboardView;
import com.herewhite.sdk.domain.Promise;
import com.herewhite.sdk.domain.RoomPhase;
import com.herewhite.sdk.domain.RoomState;
import com.herewhite.sdk.domain.SDKError;

import java.util.Date;
import java.util.Objects;

abstract public class SampleBaseActivity extends BaseActivity {
    private static final String ROOM_INFO = "RoomInfo";
    private static final String ROOM_ACTION = "RoomAction";

    protected RoomCallbacks mRoomCallbackHock = new AbstractRoomCallbacks() {
    };

    protected DemoAPI demoAPI = DemoAPI.get();
    protected Gson gson = new Gson();
    protected TextView logView;
    protected WhiteboardView whiteboardView;
    protected WhiteSdk whiteSdk;
    protected Room room;

    protected String uuid;
    protected String token;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getContentView());
        Objects.requireNonNull(getSupportActionBar()).hide();

        logView = findViewById(R.id.logDisplay);
        whiteboardView = findViewById(R.id.white);
        initView();
        setupRoom();
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

        String uuid = getIntent().getStringExtra(StartActivity.EXTRA_ROOM_UUID);
        if (uuid != null) {
            demoAPI.getRoomToken(uuid, result);
        } else {
            demoAPI.getNewRoom(result);
        }
    }

    private void joinRoom(String uuid, String token) {
        logRoomInfo("room uuid: " + uuid + "\nroom token: " + token);

        this.uuid = uuid;
        this.token = token;

        WhiteSdkConfiguration configuration = generateSdkConfig();
        whiteSdk = new WhiteSdk(whiteboardView, this, configuration);

        RoomParams roomParams = generateRoomParams();
        final Date joinDate = new Date();
        whiteSdk.joinRoom(roomParams, new RoomCallbacks() {
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
                SampleBaseActivity.this.room = room;
                testMarkIdling(true);
                onJoinRoomSuccess();
            }

            @Override
            public void catchEx(SDKError t) {
                logRoomInfo("native join fail: " + t.getMessage());
                showToast(t.getMessage());
            }
        });
    }

    abstract protected View getContentView();

    abstract protected void initView();

    abstract protected void onJoinRoomSuccess();

    protected WhiteSdkConfiguration generateSdkConfig() {
        WhiteSdkConfiguration configuration = new WhiteSdkConfiguration(demoAPI.getAppId(), true);
        configuration.setUserCursor(true);
        return configuration;
    }

    protected RoomParams generateRoomParams() {
        RoomParams roomParams = new RoomParams(uuid, token, DemoAPI.DEFAULT_UID);
        roomParams.setDisableNewPencil(false);
        roomParams.setWritable(true);
        return roomParams;
    }

    protected void alert(final String title, final String detail) {
        runOnUiThread(() -> {
            AlertDialog alertDialog = new AlertDialog.Builder(this).create();
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

    protected void logRoomInfo(String str) {
        Log.i(ROOM_INFO, Thread.currentThread().getStackTrace()[3].getMethodName() + " " + str);
    }

    protected void logAction(String str) {
        Log.i(ROOM_ACTION, Thread.currentThread().getStackTrace()[3].getMethodName() + " " + str);
    }

    protected void logAction() {
        Log.i(ROOM_ACTION, Thread.currentThread().getStackTrace()[3].getMethodName());
    }

    protected void showLogDisplay(String message) {
        if (logView == null) return;
        runOnUiThread(() -> {
            String text = message + "\n\n" + logView.getText().toString();
            logView.setText(text);
        });
    }
}
