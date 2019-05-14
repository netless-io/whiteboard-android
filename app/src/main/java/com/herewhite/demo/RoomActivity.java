package com.herewhite.demo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.herewhite.sdk.AbstractPlayerEventListener;
import com.herewhite.sdk.AbstractRoomCallbacks;
import com.herewhite.sdk.Environment;
import com.herewhite.sdk.Logger;
import com.herewhite.sdk.domain.DeviceType;
import com.herewhite.sdk.domain.MemberInformation;
import com.herewhite.sdk.Player;
import com.herewhite.sdk.Room;
import com.herewhite.sdk.RoomParams;
import com.herewhite.sdk.WhiteBroadView;
import com.herewhite.sdk.WhiteSdk;
import com.herewhite.sdk.WhiteSdkConfiguration;
import com.herewhite.sdk.domain.PlayerConfiguration;
import com.herewhite.sdk.domain.PlayerPhase;
import com.herewhite.sdk.domain.PlayerState;
import com.herewhite.sdk.domain.Promise;
import com.herewhite.sdk.domain.RoomPhase;
import com.herewhite.sdk.domain.RoomState;
import com.herewhite.sdk.domain.SDKError;
import com.herewhite.sdk.domain.Scene;
import com.herewhite.sdk.domain.UpdateCursor;
import com.herewhite.sdk.domain.UrlInterrupter;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static com.herewhite.demo.DemoAPI.TEST_ROOM_TOKEN;
import static com.herewhite.demo.DemoAPI.TEST_UUID;

public class RoomActivity extends AppCompatActivity {

    WhiteBroadView whiteBroadView;
    Room room;
    Gson gson = new Gson();
    DemoAPI demoAPI = new DemoAPI();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.js);
        whiteBroadView = findViewById(R.id.white);
        try {
            createRoom();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createRoom() throws IOException {
        /*该请求，应该存放在业务服务器中，客户端从业务服务器，获取 roomToken。*/
        demoAPI.createRoom("unknow", 100, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("createRoom fail", e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                JsonObject room = gson.fromJson(response.body().string(), JsonObject.class);
                String uuid = room.getAsJsonObject("msg").getAsJsonObject("room").get("uuid").getAsString();
                String roomToken = room.getAsJsonObject("msg").get("roomToken").getAsString();
                Log.i("white", uuid + "|" + roomToken);
                if (whiteBroadView.getEnv() == Environment.dev) {
                    joinRoom(TEST_UUID, TEST_ROOM_TOKEN);
                } else {
                    joinRoom(uuid, roomToken);
                }
            }
        });
    }

    private void joinRoom(String uuid, String roomToken) {

        Log.i("room info:", uuid + "\n" + roomToken);

        WhiteSdkConfiguration sdkConfiguration = new WhiteSdkConfiguration(DeviceType.touch, 10, 0.1, true);
        /*显示用户头像*/
        sdkConfiguration.setUserCursor(true);
        /*接受用户头像信息回调，自己实现头像回调。会导致 UserCursor 设置失效。*/
        sdkConfiguration.setCustomCursor(true);

        WhiteSdk whiteSdk = new WhiteSdk(
                whiteBroadView,
                RoomActivity.this,
                sdkConfiguration,
                new UrlInterrupter() {
                    @Override
                    public String urlInterrupter(String sourceUrl) {
                        return sourceUrl;
                    }
                });

        /*插入图片*/
        MemberInformation info = new MemberInformation("313131");
        info.setAvatar("https://white-pan.oss-cn-shanghai.aliyuncs.com/40/image/mask.jpg");

        whiteSdk.joinRoom(new RoomParams(uuid, roomToken), new AbstractRoomCallbacks() {
            @Override
            public void onPhaseChanged(RoomPhase phase) {
                showToast(phase.name());
                // handle room phase
            }

            @Override
            public void onRoomStateChanged(RoomState modifyState) {
                showToast(gson.toJson(modifyState));
            }
        }, new Promise<Room>() {
            @Override
            public void then(Room wRoom) {
                room = wRoom;
            }

            @Override
            public void catchEx(SDKError t) {
                showToast(t.getMessage());
            }
        });
    }



    void showToast(Object o) {
        Toast.makeText(this, o.toString(), Toast.LENGTH_SHORT).show();
    }


}
