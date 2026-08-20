package com.herewhite.demo.common;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.herewhite.demo.BaseActivity;
import com.herewhite.demo.R;
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
import java.util.concurrent.atomic.AtomicBoolean;

abstract public class SampleBaseActivity extends BaseActivity {
    public static final String EXTRA_ROOM_UUID = "roomUuid";
    public static final String EXTRA_ROOM_TOKEN = "roomToken";
    private static final String ROOM_INFO = "RoomInfo";
    private static final String ROOM_ACTION = "RoomAction";
    private static final long ROOM_DISCONNECT_TIMEOUT_MS = 3_000L;

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

    private final Handler roomHandler = new Handler(Looper.getMainLooper());
    private RoomConnectionGuard connectionGuard;
    private int roomGeneration;
    private boolean destroyed;
    private final Runnable reconnectRunnable = this::recreateRoom;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getContentView());
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        logView = findViewById(R.id.logDisplay);
        whiteboardView = findViewById(R.id.white);
        connectionGuard = new RoomConnectionGuard(new RoomConnectionGuard.Listener() {
            @Override
            public void onLoadingChanged(boolean visible) {
                runOnUiThread(() -> {
                    View loading = findViewById(R.id.roomConnectionLoading);
                    if (loading != null) {
                        loading.setVisibility(visible ? View.VISIBLE : View.GONE);
                    }
                });
            }

            @Override
            public void onReconnectRequested(long delayMs) {
                roomHandler.removeCallbacks(reconnectRunnable);
                roomHandler.postDelayed(reconnectRunnable, delayMs);
            }

            @Override
            public void onReconnectCancelled() {
                roomHandler.removeCallbacks(reconnectRunnable);
            }
        });
        initView();
        setupRoom();
    }

    @Override
    protected void onDestroy() {
        destroyed = true;
        roomGeneration++;
        roomHandler.removeCallbacksAndMessages(null);
        if (connectionGuard != null) {
            connectionGuard.stop();
        }
        if (whiteSdk != null) {
            whiteSdk.releaseRoom();
        }
        room = null;
        whiteboardView.destroy();
        super.onDestroy();
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

        String intentUuid = getIntent().getStringExtra(EXTRA_ROOM_UUID);
        String intentToken = getIntent().getStringExtra(EXTRA_ROOM_TOKEN);
        if (intentUuid != null && !intentUuid.isEmpty()
                && intentToken != null && !intentToken.isEmpty()) {
            joinRoom(intentUuid, intentToken);
            return;
        }

        String uuid = demoAPI.getRoomUUID();
        if (uuid != null) {
            demoAPI.getRoomToken(uuid, result);
        } else {
            demoAPI.getNewRoom(result);
        }
    }

    private void joinRoom(String uuid, String token) {
        if (destroyed || !connectionGuard.onJoinStarted()) {
            return;
        }
        logRoomInfo("room uuid: " + uuid + "\nroom token: " + token);

        this.uuid = uuid;
        this.token = token;

        if (whiteSdk == null) {
            WhiteSdkConfiguration configuration = generateSdkConfig();
            whiteSdk = new WhiteSdk(whiteboardView, this, configuration);
        }

        RoomParams roomParams = generateRoomParams();
        final int generation = ++roomGeneration;
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
                if (!isCurrentRoom(generation)) {
                    return;
                }
                mRoomCallbackHock.onPhaseChanged(phase);
                connectionGuard.onRoomPhaseChanged(phase);
                logRoomInfo("onPhaseChanged: " + phase.name());
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
                if (!isCurrentRoom(generation)) {
                    return;
                }
                //记录加入房间消耗的时长
                logRoomInfo("native join in room duration: " + (System.currentTimeMillis() - joinDate.getTime()) / 1000f + "s");
                SampleBaseActivity.this.room = room;
                connectionGuard.onJoinSucceeded();
                connectionGuard.onRoomPhaseChanged(room.getRoomPhase());
                testMarkIdling(true);
                onJoinRoomSuccess();
            }

            @Override
            public void catchEx(SDKError t) {
                if (!isCurrentRoom(generation)) {
                    return;
                }
                logRoomInfo("native join fail: " + t.getMessage());
                connectionGuard.onJoinFailed();
            }
        });
    }

    private boolean isCurrentRoom(int generation) {
        return !destroyed && generation == roomGeneration;
    }

    private void recreateRoom() {
        if (destroyed || !connectionGuard.onReconnectTimerFired()) {
            return;
        }

        final int teardownGeneration = ++roomGeneration;
        final Room oldRoom = room;
        room = null;
        AtomicBoolean completed = new AtomicBoolean();
        Runnable continueJoin = () -> completeRoomRecreation(teardownGeneration, completed);

        if (oldRoom == null || oldRoom.getRoomPhase() == RoomPhase.disconnected) {
            continueJoin.run();
            return;
        }

        roomHandler.postDelayed(continueJoin, ROOM_DISCONNECT_TIMEOUT_MS);
        oldRoom.disconnect(new Promise<Object>() {
            @Override
            public void then(Object result) {
                runOnUiThread(continueJoin);
            }

            @Override
            public void catchEx(SDKError error) {
                logRoomInfo("disconnect before reconnect failed: " + error.getMessage());
                runOnUiThread(continueJoin);
            }
        });
    }

    private void completeRoomRecreation(int generation, AtomicBoolean completed) {
        if (!completed.compareAndSet(false, true)
                || destroyed
                || generation != roomGeneration) {
            return;
        }
        whiteSdk.releaseRoom();
        joinRoom(uuid, token);
    }

    protected void leaveRoomAndFinish() {
        connectionGuard.leaveRoom();
        roomHandler.removeCallbacksAndMessages(null);
        roomGeneration++;

        Room oldRoom = room;
        room = null;
        if (oldRoom == null) {
            if (whiteSdk != null) {
                whiteSdk.releaseRoom();
            }
            finish();
            return;
        }

        AtomicBoolean completed = new AtomicBoolean();
        Runnable completeLeave = () -> {
            if (!completed.compareAndSet(false, true)) {
                return;
            }
            if (whiteSdk != null) {
                whiteSdk.releaseRoom();
            }
            finish();
        };
        roomHandler.postDelayed(completeLeave, ROOM_DISCONNECT_TIMEOUT_MS);
        oldRoom.disconnect(new Promise<Object>() {
            @Override
            public void then(Object result) {
                runOnUiThread(completeLeave);
            }

            @Override
            public void catchEx(SDKError error) {
                logRoomInfo("leave room disconnect failed: " + error.getMessage());
                runOnUiThread(completeLeave);
            }
        });
    }

    @Override
    public void onBackPressed() {
        leaveRoomAndFinish();
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
        RoomParams roomParams = new RoomParams(uuid, token, DemoAPI.get().getUserId());
        roomParams.setDisableNewPencil(false);
        roomParams.setWritable(true);
        roomParams.setUndoCacheScenesCount(32);
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
