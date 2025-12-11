package com.herewhite.rtc.demo.rtc4;

import static io.agora.rtc2.Constants.AUDIO_MIXING_REASON_ALL_LOOPS_COMPLETED;
import static io.agora.rtc2.Constants.AUDIO_MIXING_REASON_CAN_NOT_OPEN;
import static io.agora.rtc2.Constants.AUDIO_MIXING_REASON_INTERRUPTED_EOF;
import static io.agora.rtc2.Constants.AUDIO_MIXING_REASON_OK;
import static io.agora.rtc2.Constants.AUDIO_MIXING_REASON_ONE_LOOP_COMPLETED;
import static io.agora.rtc2.Constants.AUDIO_MIXING_REASON_STOPPED_BY_USER;
import static io.agora.rtc2.Constants.AUDIO_MIXING_REASON_TOO_FREQUENT_CALL;
import static io.agora.rtc2.Constants.AUDIO_MIXING_STATE_FAILED;
import static io.agora.rtc2.Constants.AUDIO_MIXING_STATE_PAUSED;
import static io.agora.rtc2.Constants.AUDIO_MIXING_STATE_PLAYING;
import static io.agora.rtc2.Constants.AUDIO_MIXING_STATE_STOPPED;
import static io.agora.rtc2.Constants.AUDIO_SCENARIO_CHATROOM;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.webkit.WebView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.herewhite.rtc.demo.R;
import com.herewhite.rtc.demo.RtcActivityDelegate;
import com.herewhite.rtc.demo.Utils;
import com.herewhite.rtc.demo.databinding.ActivityMainRtcBinding;
import com.herewhite.sdk.AbstractRoomCallbacks;
import com.herewhite.sdk.CommonCallback;
import com.herewhite.sdk.Room;
import com.herewhite.sdk.RoomParams;
import com.herewhite.sdk.WhiteSdk;
import com.herewhite.sdk.WhiteSdkConfiguration;
import com.herewhite.sdk.WhiteboardView;
import com.herewhite.sdk.domain.Appliance;
import com.herewhite.sdk.domain.MemberState;
import com.herewhite.sdk.domain.Promise;
import com.herewhite.sdk.domain.RoomPhase;
import com.herewhite.sdk.domain.SDKError;

import org.json.JSONObject;

import io.agora.rtc2.ChannelMediaOptions;
import io.agora.rtc2.Constants;
import io.agora.rtc2.IRtcEngineEventHandler;
import io.agora.rtc2.RtcEngine;
import io.agora.rtc2.video.VideoCanvas;
import io.agora.rtc2.video.VideoEncoderConfiguration;

/**
 * RTC 4.x 版本的代理实现
 */
public class Rtc4ActivityDelegate implements RtcActivityDelegate {
    private static final String TAG = "RtcDelegate4x";

    private String RTC_CHANNEL_ID = "demoChannel1";
    private String RTC_CHANNEL_TOKEN = "007eJxTYFhRsT5iX6KMcuDT+wq3pXwmJDWwc0+QPq1r/OZGTuhvpysKDIaploapSQaGKabJhibmSYmJaclJickmBuZplslJpilG2cE+qQ2BjAyNbxoYGKEQxOdhSEnNzXfOSMzLS80xZGAAADy1Img=";

    private static final int UID_LOCAL = 0x12356;

    private static final String DEFAULT_UID = "1233124";

    private static final int PERMISSION_REQ_ID = 22;
    private static final String[] REQUESTED_PERMISSIONS = {
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA,
    };

    private AppCompatActivity activity;
    private ActivityMainRtcBinding binding;

    private SurfaceView localVideoView;
    private SurfaceView remoteVideoView;

    private RtcEngine rtcEngine;

    private WhiteboardView whiteboardView;
    private WhiteSdk whiteSdk;
    private Room room;

    private AudioMixerBridgeRtc4Impl audioMixerBridge;
    private AudioEffectBridgeRtc4Impl audioEffectBridge;

    private boolean calling = false;

    private final IRtcEngineEventHandler mRtcEventHandler = new IRtcEngineEventHandler() {
        @Override
        public void onJoinChannelSuccess(String channel, final int uid, int elapsed) {
            activity.runOnUiThread(() -> Log.i("agora", "Join channel success, uid: " + (uid & 0xFFFFFFFFL)));
        }

        @Override
        public void onUserJoined(int uid, int elapsed) {
            activity.runOnUiThread(() -> {
                Log.i("agora", "First remote video decoded, uid: " + (uid & 0xFFFFFFFFL));
                setupRemoteVideo(uid);
            });
        }

        @Override
        public void onUserOffline(final int uid, int reason) {
            Log.i("agora", "User offline, uid: " + (uid & 0xFFFFFFFFL));
        }

        @Override
        public void onAudioMixingStateChanged(int state, int reason) {
            Log.d(AudioMixerBridgeRtc4Impl.TAG, "rtcMix[RTC] onAudioMixingStateChanged " + readableState(state) + ":" + readableReason(reason));
            if (whiteSdk != null) {
                whiteSdk.getAudioMixerImplement().setMediaState(state, reason);
            }
        }

        @Override
        public void onAudioEffectFinished(int soundId) {
            Log.d("AudioEffect", "rtcMix[RTC] onAudioEffectFinished " + soundId);
            if (whiteSdk != null) {
                whiteSdk.getAudioEffectImplement().setEffectFinished(soundId);
            }
        }
    };

    @Override
    public void onCreate(AppCompatActivity activity, ActivityMainRtcBinding binding, Bundle savedInstanceState) {
        this.activity = activity;
        this.binding = binding;

        WebView.setWebContentsDebuggingEnabled(true);

        whiteboardView = binding.whiteboardView;

        // 设置按钮点击事件
        setupButtonListeners();

        checkAndInitRtcEngine();
    }

    private void setupButtonListeners() {
        binding.btnCall.setOnClickListener(v -> {
            calling = !calling;
            setCalling(calling);
        });

        binding.playEffect.setOnClickListener(v -> playEffect());
        binding.preloadEffect.setOnClickListener(v -> preloadEffect());
        binding.pauseEffect.setOnClickListener(v -> pauseEffect());
        binding.resumeEffect.setOnClickListener(v -> resumeEffect());
        binding.getEffectCurrentPosition.setOnClickListener(v -> getEffectCurrentPosition());
        binding.startAudioMixing.setOnClickListener(v -> startAudioMixing());
    }

    @Override
    public void onDestroy() {
        leaveChannel();
        RtcEngine.destroy();
        whiteboardView.removeAllViews();
        whiteboardView.destroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        checkAndInitRtcEngine();
    }

    private void setCalling(boolean calling) {
        if (calling) {
            startCall();
        } else {
            endCall();
        }
        this.calling = calling;
        binding.btnCall.setSelected(calling);
    }

    private void playEffect() {
        int code = rtcEngine.playEffect(1, "https://convertcdn.netless.group/test/dynamicConvert/d507bd99a85b4f14861edbce85ef30e0/jsonOutput/401f2fd866b025ba71f959eef4930819.mp3", 1, 1, 0.0, 100, false, 0);
        int code2 = rtcEngine.playEffect(2, "https://canvas-conversion-demo-dev.oss-cn-hangzhou.aliyuncs.com/assets/Jay%20demo%201.mp3", 0, 1, 0.0, 100, false, 150000);
        int code3 = rtcEngine.playEffect(3, "https://canvas-conversion-demo-dev.oss-cn-hangzhou.aliyuncs.com/assets/Jay%20demo%202.mp3", 0, 1, 0.0, 100, false, 160000);
        Log.d("AudioEffect", "code1: " + code + " code2: " + code2 + " code3: " + code3);
    }

    private void preloadEffect() {
        // 4.x 版本预留
    }

    private void pauseEffect() {
        int code = audioEffectBridge.pauseAllEffects();
        Log.d("AudioEffect", "pauseAllEffects code: " + code);
    }

    private void resumeEffect() {
        int code = audioEffectBridge.resumeAllEffects();
        Log.d("AudioEffect", "resumeAllEffects code: " + code);
    }

    private void getEffectCurrentPosition() {
        int position = audioEffectBridge.getEffectCurrentPosition(1);
        Log.d("AudioEffect", "getEffectCurrentPosition :" + position);
    }

    private void startAudioMixing() {
        audioMixerBridge.startAudioMixing("https://white-pan.oss-cn-shanghai.aliyuncs.com/101/oceans.mp4", false, false, 1);
    }

    private void checkAndInitRtcEngine() {
        if (checkSelfPermission(REQUESTED_PERMISSIONS[0], PERMISSION_REQ_ID) && checkSelfPermission(REQUESTED_PERMISSIONS[1], PERMISSION_REQ_ID)) {
            initializeEngine();
            setupVideoConfig();
            setCalling(true);
        }
    }

    private void initializeEngine() {
        try {
            rtcEngine = RtcEngine.create(activity.getBaseContext(), activity.getString(R.string.rtc_app_id), mRtcEventHandler);
        } catch (Exception e) {
            throw new RuntimeException("NEED TO check rtc sdk init fatal error\n" + Log.getStackTraceString(e));
        }
    }

    private void setupVideoConfig() {
        rtcEngine.enableVideo();
        rtcEngine.startPreview();
        rtcEngine.setVideoEncoderConfiguration(new VideoEncoderConfiguration(
                VideoEncoderConfiguration.VD_640x360,
                VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_15,
                VideoEncoderConfiguration.STANDARD_BITRATE,
                VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_FIXED_PORTRAIT));
    }

    private void joinRoom(String uuid, String token) {
        WhiteSdkConfiguration configuration = new WhiteSdkConfiguration(Utils.BOARD_APP_ID, true);
        configuration.setUserCursor(true);
        configuration.setUseMultiViews(true);

        audioMixerBridge = new AudioMixerBridgeRtc4Impl(rtcEngine, (state, code) -> {
            if (whiteSdk.getAudioMixerImplement() != null) {
                whiteSdk.getAudioMixerImplement().setMediaState(state, code);
            }
        });
        audioEffectBridge = new AudioEffectBridgeRtc4Impl(rtcEngine);

        whiteSdk = new WhiteSdk.Builder(whiteboardView, configuration)
                .setAudioMixerBridge(audioMixerBridge)
                .setCommonCallback(new CommonCallback() {
                    @Override
                    public void onLogger(JSONObject object) {
                        Log.i("ROOM_LOGGER", object.toString());
                    }
                })
                .build();

        RoomParams roomParams = new RoomParams(uuid, token, DEFAULT_UID);
        roomParams.setWritable(true);

        whiteSdk.joinRoom(roomParams, new AbstractRoomCallbacks() {
            @Override
            public void onPhaseChanged(RoomPhase phase) {
                showToast(phase.name());
            }
        }, new Promise<Room>() {
            @Override
            public void then(Room room) {
                Rtc4ActivityDelegate.this.room = room;
                MemberState memberState = new MemberState();
                memberState.setCurrentApplianceName(Appliance.CLICKER);
                room.setMemberState(memberState);
            }

            @Override
            public void catchEx(SDKError sdkError) {
            }
        });
    }

    private boolean checkSelfPermission(String permission, int requestCode) {
        if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, REQUESTED_PERMISSIONS, requestCode);
            return false;
        }
        return true;
    }

    private void setupRemoteVideo(int uid) {
        remoteVideoView = RtcEngine.CreateRendererView(activity.getBaseContext());
        binding.remoteVideoViewContainer.addView(remoteVideoView);
        rtcEngine.setupRemoteVideo(new VideoCanvas(remoteVideoView, VideoCanvas.RENDER_MODE_HIDDEN, uid));
    }

    private void setupLocalVideo() {
        localVideoView = RtcEngine.CreateRendererView(activity.getBaseContext());
        localVideoView.setZOrderMediaOverlay(true);
        binding.localVideoViewContainer.addView(localVideoView);
        rtcEngine.setupLocalVideo(new VideoCanvas(localVideoView, VideoCanvas.RENDER_MODE_HIDDEN, UID_LOCAL));
    }

    private void startCall() {
        setupLocalVideo();
        joinChannel();
        joinRoom(Utils.BOARD_ROOM_UUID, Utils.BOARD_ROOM_TOKEN);
    }

    private void endCall() {
        removeLocalVideo();
        removeRemoteVideo();
        leaveChannel();
    }

    private void removeLocalVideo() {
        if (localVideoView != null) {
            binding.localVideoViewContainer.removeView(localVideoView);
            localVideoView = null;
        }
    }

    private void removeRemoteVideo() {
        if (remoteVideoView != null) {
            binding.remoteVideoViewContainer.removeView(remoteVideoView);
            remoteVideoView = null;
        }
    }

    private void joinChannel() {
        ChannelMediaOptions options = new ChannelMediaOptions();
        options.clientRoleType = Constants.CLIENT_ROLE_BROADCASTER;
        rtcEngine.joinChannel(RTC_CHANNEL_TOKEN, RTC_CHANNEL_ID, UID_LOCAL, options);
        rtcEngine.setAudioScenario(AUDIO_SCENARIO_CHATROOM);
    }

    private void leaveChannel() {
        rtcEngine.leaveChannel();
    }

    private void showToast(Object o) {
        Toast.makeText(activity, o.toString(), Toast.LENGTH_SHORT).show();
    }

    public String readableState(int state) {
        switch (state) {
            case AUDIO_MIXING_STATE_PLAYING:
                return "PLAYING";
            case AUDIO_MIXING_STATE_PAUSED:
                return "PAUSED";
            case AUDIO_MIXING_STATE_STOPPED:
                return "STOPPED";
            case AUDIO_MIXING_STATE_FAILED:
                return "FAILED";
            default:
                return state + "";
        }
    }

    public String readableReason(int reason) {
        switch (reason) {
            case AUDIO_MIXING_REASON_OK:
                return "OK";
            case AUDIO_MIXING_REASON_CAN_NOT_OPEN:
                return "CAN_NOT_OPEN";
            case AUDIO_MIXING_REASON_TOO_FREQUENT_CALL:
                return "TOO_FREQUENT_CALL";
            case AUDIO_MIXING_REASON_INTERRUPTED_EOF:
                return "INTERRUPTED_EOF";
            case AUDIO_MIXING_REASON_ONE_LOOP_COMPLETED:
                return "ONE_LOOP_COMPLETED";
            case AUDIO_MIXING_REASON_ALL_LOOPS_COMPLETED:
                return "ALL_LOOPS_COMPLETED";
            case AUDIO_MIXING_REASON_STOPPED_BY_USER:
                return "STOPPED_BY_USER";
            default:
                return reason + "";
        }
    }
}
