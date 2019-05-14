package com.herewhite.demo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.herewhite.sdk.*;
import com.herewhite.sdk.domain.*;

import java.io.IOException;
import java.util.HashMap;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static com.herewhite.demo.DemoAPI.TEST_ROOM_TOKEN;
import static com.herewhite.demo.DemoAPI.TEST_UUID;

public class RoomActivity extends AppCompatActivity {

    /*和 iOS 名字一致*/
    final String EVENT_NAME = "WhiteCommandCustomEvent";

    final String SCENE_DIR = "/dir";


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

    public void broadcaster() {
        Log.i("action", "set broadcaster");
        room.setViewMode(ViewMode.Broadcaster);
    }

    public void getBroadcastState() {
        Log.i("action", "get broadcastState");
        room.getBroadcastState(new Promise<BroadcastState>() {
            @Override
            public void then(BroadcastState broadcastState) {
                showToast(broadcastState.getMode());
                Log.i("room info", broadcastState.toString());
            }

            @Override
            public void catchEx(SDKError t) {

            }
        });
    }

    public void dispatchCustomEvent() {

        Log.i("action", "dispatchCustomEvent");

        HashMap payload = new HashMap<>();
        payload.put("device", "android");

        room.dispatchMagixEvent(new AkkoEvent(EVENT_NAME, payload));
    }

    private void addCustomEventListener() {
        room.addMagixEventListener(EVENT_NAME, new EventListener() {
            @Override
            public void onEvent(EventEntry eventEntry) {
                Log.i("action", "customEvent");
                showToast(gson.toJson(eventEntry.getPayload()));
            }
        });
    }

    public void cleanScene() {
        Log.i("action", "cleanScene");
        room.cleanScene(true);
    }

    public void insertNewScene() {
        Log.i("action", "insertNewScene");
        room.putScenes(SCENE_DIR, new Scene[]{
                new Scene("page1")}, 0);
        room.setScenePath(SCENE_DIR + "/page1");
    }

    public void insertPPT() {
        Log.i("action", "insertPpt");
        room.putScenes(SCENE_DIR, new Scene[]{
            new Scene("page2", new PptPage("https://white-pan.oss-cn-shanghai.aliyuncs.com/101/image/alin-rusu-1239275-unsplash_opt.jpg", 600d, 600d))
        }, 0);
        room.setScenePath(SCENE_DIR + "/page2");
    }

    public void inserImage() {
        room.insertImage(new ImageInformationWithUrl(0d, 0d, 100d, 200d, "https://white-pan.oss-cn-shanghai.aliyuncs.com/40/image/mask.jpg"));
    }

    public void getScene() {
        room.getScenes(new Promise<Scene[]>() {
            @Override
            public void then(Scene[] scenes) {
                //TODO:do any thing you want
            }

            @Override
            public void catchEx(SDKError t) {

            }
        });
    }

    public void getRoomPhase() {
        //TODO:增加获取的 API
    }

    public void disconnect() {

        //如果需要房间断开连接后回调
        room.disconnect(new Promise<Object>() {
            @Override
            public void then(Object o) {
                Log.i("action", "room disconnect success");
            }

            @Override
            public void catchEx(SDKError t) {

            }
        });

        //如果不需要，则直接断开连接即可
        //room.disconnect();
    }

    public void readonly() {
        room.disableOperations(true);
    }

    public void pencil() {
        MemberState mberState = new MemberState();
        mberState.setStrokeColor(new int[]{99, 99, 99});
        mberState.setCurrentApplianceName(Appliance.PENCIL);
        mberState.setStrokeWidth(10);
        mberState.setTextSize(10);
        room.setMemberState(mberState);
    }

    public void rectangle() {
        MemberState mberState = new MemberState();
        mberState.setStrokeColor(new int[]{99, 99, 99});
        mberState.setCurrentApplianceName(Appliance.RECTANGLE);
        mberState.setStrokeWidth(10);
        mberState.setTextSize(10);
        room.setMemberState(mberState);
    }

    public void color() {
        MemberState mberState = new MemberState();
        mberState.setStrokeColor(new int[]{200, 200, 200});
        mberState.setCurrentApplianceName(Appliance.PENCIL);
        mberState.setStrokeWidth(4);
        mberState.setTextSize(10);
        room.setMemberState(mberState);
    }

    public void convertPoint() {
        //获取特定点，在白板内部的坐标点
        room.convertToPointInWorld(0, 0, new Promise<Point>() {
            @Override
            public void then(Point point) {
                Logger.info(point.toString());
            }

            @Override
            public void catchEx(SDKError t) {
                Logger.error("convertToPointInWorld  error", t);
            }
        });
    }

    public void externalEvent() {
        room.disableOperations(true);
        room.externalDeviceEventDown(new RoomMouseEvent(100, 300));
        room.externalDeviceEventMove(new RoomMouseEvent(100, 400));
        room.externalDeviceEventMove(new RoomMouseEvent(100, 500));
        room.externalDeviceEventMove(new RoomMouseEvent(100, 600));
        room.externalDeviceEventMove(new RoomMouseEvent(100, 700));
        room.externalDeviceEventUp(new RoomMouseEvent(100, 700));
        room.disableOperations(false);
    }

    public void zoomChange() {
        room.zoomChange(10);
    }

    void showToast(Object o) {
        Toast.makeText(this, o.toString(), Toast.LENGTH_SHORT).show();
    }
}
