package com.herewhite.demo;

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
 * Created by buhe on 2018/8/16.
 */

public class DemoAPI {

    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");

    OkHttpClient client = new OkHttpClient();
    Gson gson = new Gson();

    public void createRoom(String name, int limit, Callback callback) {
        Map<String, Object> roomSpec = new HashMap<>();
        roomSpec.put("name", name);
        roomSpec.put("limit", limit);
        roomSpec.put("mode", "persistent");
        RequestBody body = RequestBody.create(JSON, gson.toJson(roomSpec));
        Request request = new Request.Builder()
                .url("https://edgecapiv3.herewhite.com/room?token=WHITEcGFydG5lcl9pZD1QNnR4cXJEQlZrZmJNZWRUdGVLenBURXRnZzhjbGZ6ZnZteUQmc2lnPWYzZjlkOTdhYTBmZmVhZTUxYzAxYTk0N2QwMWZmMzQ5ZGRhYjhmMmQ6YWRtaW5JZD0xJnJvbGU9YWRtaW4mZXhwaXJlX3RpbWU9MTU0OTYyNzcyMyZhaz1QNnR4cXJEQlZrZmJNZWRUdGVLenBURXRnZzhjbGZ6ZnZteUQmY3JlYXRlX3RpbWU9MTUxODA3MDc3MSZub25jZT0xNTE4MDcwNzcxMjg3MDA")
                .post(body)
                .build();
        Call call = client.newCall(request);
        call.enqueue(callback);
    }

    static String TEST_UUID = "test";
    static String TEST_ROOM_TOKEN = "test";
}
