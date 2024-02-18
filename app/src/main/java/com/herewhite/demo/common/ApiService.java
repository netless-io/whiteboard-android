package com.herewhite.demo.common;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Api for create new room and create new room token
 */
public class ApiService {
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static final String HOST = "https://api.netless.link/v5";
    private static final Gson GSON = new Gson();
    private static final OkHttpClient CLIENT = new OkHttpClient();

    public static void createRoom(String sdkToken, int limit, String region, ApiCallback<RoomCreationResult> outCallback) {
        Map<String, Object> roomSpec = new HashMap<>();
        roomSpec.put("limit", limit);
        roomSpec.put("isRecord", true);

        RequestBody body = RequestBody.create(JSON, GSON.toJson(roomSpec));
        Request request = new Request.Builder()
                .url(HOST + "/rooms")
                .addHeader("token", sdkToken)
                .addHeader("region", region)
                .post(body)
                .build();

        CLIENT.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                outCallback.onFailure("网络请求错误：" + e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) {
                handleResponse(response, RoomCreationResult.class, outCallback);
            }
        });
    }

    public static void createRoomToken(String sdkToken, String uuid, String region, ApiCallback<String> outCallback) {
        Map<String, Object> roomSpec = new HashMap<>();
        roomSpec.put("lifespan", 0);
        roomSpec.put("role", "admin");

        RequestBody body = RequestBody.create(JSON, GSON.toJson(roomSpec));
        Request request = new Request.Builder()
                .url(HOST + "/tokens/rooms/" + uuid)
                .addHeader("token", sdkToken)
                .addHeader("region", region)
                .post(body)
                .build();

        CLIENT.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                outCallback.onFailure("网络请求错误：" + e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) {
                handleResponse(response, String.class, outCallback);
            }
        });
    }

    private static <T> void handleResponse(Response response, Class<T> clazz, ApiCallback<T> outCallback) {
        try {
            if (response.isSuccessful()) {
                T result = GSON.fromJson(response.body().string(), clazz);
                outCallback.onSuccess(result);
            } else {
                outCallback.onFailure("请求失败：" + response.code() + " - " + response.body().string());
            }
        } catch (Throwable e) {
            outCallback.onFailure("处理响应时出错：" + e.toString());
        }
    }
}