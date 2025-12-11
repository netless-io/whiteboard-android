package com.herewhite.rtc.demo.rtc3;

import static com.herewhite.rtc.demo.Utils.*;

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

import io.agora.rtc.Constants;
import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.RtcEngine;
import io.agora.rtc.video.VideoCanvas;
import io.agora.rtc.video.VideoEncoderConfiguration;

/**
 * RTC 3.x 版本的代理实现
 */
public class Rtc3ActivityDelegate implements RtcActivityDelegate {
    private static final String TAG = "RtcDelegate3x";

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
    private AudioMixerBridgeRtc3Impl audioMixerBridge;
    private AudioEffectBridgeRtc3Impl audioEffectBridge;

    private boolean calling = false;

    private final IRtcEngineEventHandler mRtcEventHandler = new IRtcEngineEventHandler() {
        @Override
        public void onJoinChannelSuccess(String channel, final int uid, int elapsed) {
            activity.runOnUiThread(() -> Log.i("agora", "Join channel success, uid: " + (uid & 0xFFFFFFFFL)));
        }

        @Override
        public void onFirstRemoteVideoDecoded(final int uid, int width, int height, int elapsed) {
            activity.runOnUiThread(() -> {
                Log.i("agora", "First remote video decoded, uid: " + (uid & 0xFFFFFFFFL));
                setupRemoteVideo(uid);
            });
        }

        @Override
        public void onUserJoined(int uid, int elapsed) {
            Log.i("agora", "User joined, uid: " + (uid & 0xFFFFFFFFL));
            super.onUserJoined(uid, elapsed);
        }

        @Override
        public void onUserOffline(final int uid, int reason) {
            Log.i("agora", "User offline, uid: " + (uid & 0xFFFFFFFFL));
        }

        @Override
        public void onAudioMixingStateChanged(int state, int reason) {
            Log.d(AudioMixerBridgeRtc3Impl.TAG, "rtcMix[RTC] onAudioMixingStateChanged " + readableState(state) + ":" + readableReason(reason));
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
        int code1 = rtcEngine.getAudioEffectManager().playEffect(1, "https://white-pan.oss-cn-shanghai.aliyuncs.com/101/oceans.mp4", 1, 1, 0.0, 100, false, 0);
        int code2 = rtcEngine.getAudioEffectManager().playEffect(2, "https://canvas-conversion-demo-dev.oss-cn-hangzhou.aliyuncs.com/assets/Jay%20demo%201.mp3", 0, 1, 0.0, 100, false, 0);
        int code3 = rtcEngine.getAudioEffectManager().playEffect(3, "https://canvas-conversion-demo-dev.oss-cn-hangzhou.aliyuncs.com/assets/Jay%20demo%202.mp3", 0, 1, 0.0, 100, false, 0);
        Log.d("AudioEffect", "code1: " + code1 + " code2: " + code2 + " code3: " + code3);
    }

    private void preloadEffect() {
        int code0 = rtcEngine.getAudioEffectManager().preloadEffect(1, "https://white-pan.oss-cn-shanghai.aliyuncs.com/101/oceans.mp4");
        int code1 = rtcEngine.getAudioEffectManager().preloadEffect(2, "https://canvas-conversion-demo-dev.oss-cn-hangzhou.aliyuncs.com/assets/Jay%20demo%201.mp3");
        int code2 = rtcEngine.getAudioEffectManager().preloadEffect(3, "https://canvas-conversion-demo-dev.oss-cn-hangzhou.aliyuncs.com/assets/Jay%20demo%202.mp3");
        Log.e("AudioEffect", "preloadEffect code0: " + code0 + " code1: " + code1 + " code2: " + code2);
        rtcEngine.getAudioEffectManager().setEffectsVolume(50.0);
    }

    private void pauseEffect() {
        Log.d("AudioEffect", "pauseAllEffects code: " + audioEffectBridge.pauseAllEffects());
    }

    private void resumeEffect() {
        Log.d("AudioEffect", "resumeAllEffects code: " + audioEffectBridge.resumeAllEffects());
    }

    private void getEffectCurrentPosition() {
        Log.d("AudioEffect", "getEffectCurrentPosition :" + audioEffectBridge.getEffectCurrentPosition(1));
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
            Log.e(TAG, Log.getStackTraceString(e));
            throw new RuntimeException("NEED TO check rtc sdk init fatal error\n" + Log.getStackTraceString(e));
        }
    }

    private void setupVideoConfig() {
        rtcEngine.enableVideo();
        rtcEngine.enableAudio();
        rtcEngine.setVideoEncoderConfiguration(new VideoEncoderConfiguration(
                VideoEncoderConfiguration.VD_640x360,
                VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_15,
                VideoEncoderConfiguration.STANDARD_BITRATE,
                VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_FIXED_PORTRAIT));
    }

    private void joinRoom(String uuid, String token) {
        WhiteSdkConfiguration configuration = new WhiteSdkConfiguration(BOARD_APP_ID, true);
        configuration.setUserCursor(true);
        configuration.setUseMultiViews(true);

        audioMixerBridge = new AudioMixerBridgeRtc3Impl(rtcEngine, (state, code) -> {
            if (whiteSdk.getAudioMixerImplement() != null) {
                whiteSdk.getAudioMixerImplement().setMediaState(state, code);
            }
        });
        audioEffectBridge = new AudioEffectBridgeRtc3Impl(rtcEngine);
        whiteSdk = new WhiteSdk.Builder(whiteboardView, configuration)
                .setAudioEffectBridge(audioEffectBridge)
                .setCommonCallback(new CommonCallback() {
                    @Override
                    public void onLogger(JSONObject object) {
                        Log.i("ROOM_LOGGER", object.toString());
                    }
                })
                .build();

        RoomParams roomParams = new RoomParams(uuid, token, Utils.getUserId(activity));
        roomParams.setWritable(true);

        whiteSdk.joinRoom(roomParams, new AbstractRoomCallbacks() {
            @Override
            public void onPhaseChanged(RoomPhase phase) {
                showToast(phase.name());
            }
        }, new Promise<Room>() {
            @Override
            public void then(Room room) {
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
        rtcEngine.setupLocalVideo(new VideoCanvas(localVideoView, VideoCanvas.RENDER_MODE_HIDDEN, getRtcUid()));
    }

    private void startCall() {
        setupLocalVideo();
        joinChannel();
        joinRoom(BOARD_ROOM_UUID, BOARD_ROOM_TOKEN);
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
        rtcEngine.joinChannel(null, "demoChannel1", "Extra Optional Data", getRtcUid());
    }

    private void leaveChannel() {
        rtcEngine.leaveChannel();
    }

    private void showToast(Object o) {
        Toast.makeText(activity, o.toString(), Toast.LENGTH_SHORT).show();
    }

    private int getRtcUid() {
        return Utils.getFallbackRtcId(activity);
    }

    public String readableState(int state) {
        switch (state) {
            case Constants.MEDIA_ENGINE_AUDIO_EVENT_MIXING_PLAY:
                return "PLAYING";
            case Constants.MEDIA_ENGINE_AUDIO_EVENT_MIXING_PAUSED:
                return "PAUSED";
            case Constants.MEDIA_ENGINE_AUDIO_EVENT_MIXING_STOPPED:
                return "STOPPED";
            case Constants.MEDIA_ENGINE_AUDIO_EVENT_MIXING_ERROR:
                return "FAILED";
            default:
                return state + "";
        }
    }

    public String readableReason(int reason) {
        switch (reason) {
            case Constants.ERR_OK:
                return "OK";
            case Constants.AUDIO_MIXING_REASON_CAN_NOT_OPEN:
                return "CAN_NOT_OPEN";
            case Constants.AUDIO_MIXING_REASON_TOO_FREQUENT_CALL:
                return "TOO_FREQUENT_CALL";
            case Constants.AUDIO_MIXING_REASON_INTERRUPTED_EOF:
                return "INTERRUPTED_EOF";
            case Constants.AUDIO_MIXING_REASON_ONE_LOOP_COMPLETED:
                return "ONE_LOOP_COMPLETED";
            case Constants.AUDIO_MIXING_REASON_ALL_LOOPS_COMPLETED:
                return "ALL_LOOPS_COMPLETED";
            case Constants.AUDIO_MIXING_REASON_STOPPED_BY_USER:
                return "STOPPED_BY_USER";
            default:
                return reason + "";
        }
    }
}
