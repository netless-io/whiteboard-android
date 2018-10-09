package com.herewhite.demo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.herewhite.sdk.AbstractRoomCallbacks;
import com.herewhite.sdk.Environment;
import com.herewhite.sdk.Room;
import com.herewhite.sdk.RoomParams;
import com.herewhite.sdk.WhiteBroadView;
import com.herewhite.sdk.WhiteSdk;
import com.herewhite.sdk.WhiteSdkConfiguration;
import com.herewhite.sdk.domain.AkkoEvent;
import com.herewhite.sdk.domain.Appliance;
import com.herewhite.sdk.domain.DeviceType;
import com.herewhite.sdk.domain.EventEntry;
import com.herewhite.sdk.domain.EventListener;
import com.herewhite.sdk.domain.MemberState;
import com.herewhite.sdk.domain.SDKError;
import com.herewhite.sdk.domain.Promise;
import com.herewhite.sdk.domain.RoomPhase;
import com.herewhite.sdk.domain.RoomState;

import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static com.herewhite.demo.DemoAPI.TEST_ROOM_TOKEN;
import static com.herewhite.demo.DemoAPI.TEST_UUID;

public class MainActivity extends AppCompatActivity {

    WhiteBroadView whiteBroadView;
    Gson gson = new Gson();
    DemoAPI demoAPI = new DemoAPI();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.js);
        whiteBroadView = (WhiteBroadView) findViewById(R.id.white);
//        whiteBroadView.switchEnv(Environment.dev);
        demoAPI.createRoom("unknow", 100, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

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
        WhiteSdk whiteSdk = new WhiteSdk(
                whiteBroadView,
                MainActivity.this,
                new WhiteSdkConfiguration(DeviceType.touch, 10, 0.1));
        whiteSdk.addRoomCallbacks(new AbstractRoomCallbacks() {
            @Override
            public void onPhaseChanged(RoomPhase phase) {
                showToast(phase.name());
                // handle room phase
            }

            @Override
            public void onRoomStateChanged(RoomState modifyState) {
//                showToast(gson.toJson(modifyState));
            }
        });
        whiteSdk.joinRoom(new RoomParams(uuid, roomToken), new Promise<Room>() {
            @Override
            public void then(Room room) {


//                room.insertNewPage(1);
//                GlobalState globalState = new GlobalState();
//                globalState.setCurrentSceneIndex(0);
//                room.setGlobalState(globalState);
                MemberState memberState = new MemberState();
//                memberState.setStrokeColor(new int[]{99, 99, 99});
                memberState.setCurrentApplianceName(Appliance.ELLIPSE);
////                memberState.setStrokeWidth(10);
                room.setMemberState(memberState);

                room.addMagixEventListener("helloworld", new EventListener() {
                    @Override
                    public void onEvent(EventEntry eventEntry) {
                        showToast(gson.toJson(eventEntry));
                    }
                });

                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Map<String, String> payload = new HashMap<>();
                payload.put("test", "ddd");
                room.dispatchMagixEvent(new AkkoEvent("helloworld", payload));


//
//                room.insertNewPage(1);
//                room.removePage(1);
//
//                GlobalState globalState = new GlobalState();
//                globalState.setCurrentSceneIndex(1);
//                room.setGlobalState(globalState);
//
//                room.pushPptPages(new PptPage[]{
//                        new PptPage("https://white-pan.oss-cn-shanghai.aliyuncs.com/101/image/image.png", 600d, 600d),
//                });

//                room.setViewMode(ViewMode.broadcaster);
//
//                room.getBroadcastState(new Promise<BroadcastState>() {
//                    @Override
//                    public void then(BroadcastState broadcastState) {
//                        showToast(broadcastState.getMode());
//                    }
//
//                    @Override
//                    public void catchEx(Exception t) {
//
//                    }
//                });

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
