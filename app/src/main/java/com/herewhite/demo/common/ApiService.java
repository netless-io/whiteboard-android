package com.herewhite.demo.common;

import androidx.annotation.NonNull;

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
    private static final String host = "https://api.netless.link/v5";
    private static final Gson gson = new Gson();
    private static final OkHttpClient client = new OkHttpClient();

    public static void createRoom(String sdkToken,
                                  int limit,
                                  String region,
                                  ApiCallback<CreateRoomResult> outCallback) {
        Map<String, Object> roomSpec = new HashMap<>();
        roomSpec.put("limit", limit);
        roomSpec.put("isRecord", true);

        RequestBody body = RequestBody.create(JSON, gson.toJson(roomSpec));
        Request request = new Request.Builder()
                .url(host + "/rooms")
                .addHeader("token", sdkToken)
                .addHeader("region", region)
                .post(body)
                .build();

        Call call = client.newCall(request);
        Callback callback = new okhttp3.Callback() {
            @Override
            public void onFailure(@NonNull Call call, IOException e) {
                outCallback.onFailure("网络请求错误：" + e.toString());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                try {
                    if (response.code() >= 200 && response.code() < 300) {
                        CreateRoomResult result = gson.fromJson(response.body().string(), CreateRoomResult.class);
                        outCallback.onSuccess(result);
                    } else {
                        outCallback.onFailure("创建房间失败：" + response.body().string());
                    }
                } catch (Throwable e) {
                    outCallback.onFailure(("网络请求错误：" + e.toString()));
                }
            }
        };
        call.enqueue(callback);
    }

    /**
     * see https://developer.netless.link/server-zh/home/server-token
     *
     * @param sdkToken
     * @param uuid
     * @param region
     * @param outCallback
     */
    public static void createRoomToken(String sdkToken,
                                       String uuid,
                                       String region,
                                       ApiCallback<String> outCallback) {
        Map<String, Object> roomSpec = new HashMap<>();
        // unit ms, one day: 3600 * 24 * 1000
        roomSpec.put("lifespan", 0);
        roomSpec.put("role", "admin");
        RequestBody body = RequestBody.create(JSON, gson.toJson(roomSpec));
        Request request = new Request.Builder()
                .url(host + "/tokens/rooms/" + uuid)
                .addHeader("token", sdkToken)
                .addHeader("region", region)
                .post(body)
                .build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                outCallback.onFailure("网络请求错误：" + e.toString());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                try {
                    if (response.code() >= 200 && response.code() < 300) {
                        String roomToken = gson.fromJson(response.body().string(), String.class);
                        outCallback.onSuccess(roomToken);
                    } else {
                        outCallback.onFailure(("获取房间 token 失败：" + response.body().string()));
                    }
                } catch (Throwable e) {
                    outCallback.onFailure(("网络请求错误：" + e.toString()));
                }
            }
        });
    }

    public static String convertUrl(String sourceUrl) {
        Map<String, Object> roomSpec = new HashMap<>();
        roomSpec.put("src", sourceUrl);
        RequestBody body = RequestBody.create(
                MediaType.parse("application/json; charset=utf-8"),
                gson.toJson(roomSpec)
        );
        Request request = new Request.Builder()
                .url("https://abacus-api-us.netless.group/aws/s3/presigned")
                .addHeader("region", "us-sv")
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build();

        String signedUrl = sourceUrl;
        try {
            Call call = client.newCall(request);
            Response response = call.execute();
            if (response.code() >= 200 && response.code() < 300) {
                SignedResult result = gson.fromJson(response.body().string(), SignedResult.class);
                signedUrl = result.signedUrl;
            }
        } catch (Throwable ignored) {
        }
        return signedUrl;
    }
}
