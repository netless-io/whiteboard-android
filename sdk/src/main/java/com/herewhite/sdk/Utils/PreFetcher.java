package com.herewhite.sdk.Utils;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.herewhite.sdk.WhiteSdk;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class PreFetcher {


    public interface ResultCallback {
        void fetchOriginConfigFail(Exception exception);
        void fetchOriginConfigSuccess(JsonObject jsonObject);
        void finishPrefetch(JsonObject jsonObject);
    }

    static final String API_Origin = "https://cloudcapiv4.herewhite.com";
    static final String ORIGINS = "origins";

    static final String PING_INFO_ORIGIN = "origin";
    static final String PING_INFO_PING = "ping";
    static final String PING_INFO_VALID = "valid";

    private static final String TAG = "prefetch";

    static ExecutorService poolExecutor = Executors.newSingleThreadExecutor();

    OkHttpClient client = new OkHttpClient().newBuilder().connectTimeout(1, TimeUnit.SECONDS).build();
    Gson gson = new Gson();

    HashMap<String, Number> responseSpeedMap = new HashMap<>();
    HashSet<String> domains;
    JsonObject sdkStructConfig;
    JsonObject sdkConfig;

    public void setResultCallback(ResultCallback resultCallback) {
        this.resultCallback = resultCallback;
    }

    ResultCallback resultCallback;

    public void fetchOriginConfigs() {
        Request request = new Request.Builder()
                .url(API_Origin + "/configs/origin")
                .header("Content-Type", "application/json")
                .header("Accept","application/json")
                .header("platform","android")
                .header("version", WhiteSdk.Version())
                .build();
        final Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (resultCallback != null) {
                    resultCallback.fetchOriginConfigFail(e);
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                JsonObject json = gson.fromJson(response.body().string(), JsonObject.class);
                JsonObject serverConfig = json.getAsJsonObject("msg");
                if (response.code() == 200) {
                    sdkStructConfig = sdkStructConfig(serverConfig);
                    domains = extractDomains(serverConfig);
                    if (resultCallback != null) {
                        resultCallback.fetchOriginConfigSuccess(serverConfig);
                    }
                    prefetchOrigins();
                } else if (resultCallback != null) {
                    Exception e = new Exception(serverConfig.toString());
                    resultCallback.fetchOriginConfigFail(e);
                }
            }
        });
    }

    public void prefetchOrigins() {

        poolExecutor.execute(new Runnable() {
            @Override
            public void run() {

                Iterator<String> it = domains.iterator();
                while (it.hasNext()) {
                    final String origin = it.next();
                    final CountDownLatch latch = new CountDownLatch(1);

                    final Date beginDate = new Date();
//                    Log.i(TAG, "begin fetch");
                    pingHost(origin, new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
//                            Log.i(TAG, "ping " + origin + " fail");
                            latch.countDown();
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            double duration = new Date().getTime() - beginDate.getTime();
                            if (response.code() == 200) {
//                                Log.i(TAG, origin + " response time: " + duration / 1000.0f + "ms");
                                responseSpeedMap.put(origin, duration / 1000.0f);
                            } else {
//                                Log.i(TAG, "ping " + origin + " fail");
                            }
                            latch.countDown();
                        }
                    });

                    try {
                        latch.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                sdkConfig = generateSdkConfig(sdkStructConfig);
                if (resultCallback != null) {
                    resultCallback.finishPrefetch(sdkConfig);
                }
            }
        });
    }

    public JsonObject sdkStructConfig(JsonObject jsonObject) {
        JsonObject structJson = new JsonObject();
        for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
            JsonElement value = entry.getValue();
            if (value.isJsonObject()) {
                JsonObject subStructJson = new JsonObject();
                JsonObject originConfig = new JsonObject();

                JsonObject subJson = (JsonObject)value;
                for (Map.Entry<String, JsonElement> subEntry : subJson.entrySet()) {
                    JsonElement subValue = subEntry.getValue();
                    if (subValue.isJsonPrimitive()) {
                        subStructJson.add(subEntry.getKey(), subValue);
                    } else {
                        originConfig.add(subEntry.getKey(), subValue);
                    }
                }
                subStructJson.add(ORIGINS, originConfig);
                structJson.add(entry.getKey(), subStructJson);
            } else {
                structJson.add(entry.getKey(), value);
            }
        }
        return structJson;
    }

    public HashSet<String> extractDomains(JsonObject jsonObject) {
        HashSet<String> set = new HashSet<>();
        for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
            JsonElement value = entry.getValue();
            if (value.isJsonPrimitive()) {
                JsonPrimitive primitive = (JsonPrimitive)value;
                if (primitive.isString()) {
                    String host = primitive.getAsString();
                    if (isDomain(host)) {
                        set.add(primitive.getAsString());
                    }
                }
            } else if (value.isJsonObject()) {
                JsonObject subJson = (JsonObject)value;
                set.addAll(extractDomains(subJson));
            } else if (value.isJsonArray()) {
                JsonArray array = (JsonArray)value;
                for (int i = 0; i < array.size(); i++) {
                    JsonElement object = array.get(i);
                    if (object.isJsonPrimitive()) {
                        JsonPrimitive primitive = (JsonPrimitive)object;
                        if (primitive.isString()) {
                            String host = primitive.getAsString();
                            if (isDomain(host)) {
                                set.add(primitive.getAsString());
                            }
                        }
                    }
                }
            }
        }
        return set;
    }

    private boolean isDomain(String url) {
        //TODO:更严谨的判断
        return url.contains("://");
    }

    public JsonObject generateSdkConfig(JsonObject structObject) {
        JsonObject config = new JsonObject();

        for (Map.Entry<String, JsonElement> entry : structObject.entrySet()) {
            JsonElement value = entry.getValue();

            if (value.isJsonObject()) {
                JsonObject subStructJson = (JsonObject) value.deepCopy();

                JsonObject oldOrigins = value.getAsJsonObject().get(ORIGINS).getAsJsonObject();
                JsonObject newOrigins = new JsonObject();

                for (Map.Entry<String, JsonElement> subEntry : oldOrigins.entrySet()) {
                    JsonElement subValue = subEntry.getValue();
                    String subKey = subEntry.getKey();

                    if (subValue.isJsonArray()) {
                        newOrigins.add(subKey, pingInfoHosts(subValue.getAsJsonArray()));
                    } else {
                        newOrigins.add(subKey, subValue);
                    }
                }
                subStructJson.add(ORIGINS, newOrigins);

                config.add(entry.getKey(), subStructJson);
            } else {
                config.add(entry.getKey(), value);
            }
        }
        return config;
    }

    public JsonArray pingInfoHosts(JsonArray hosts) {
        JsonArray jsonArray = new JsonArray();
        for (JsonElement element: hosts) {
            if (element.isJsonPrimitive()) {
                jsonArray.add(pingInfoHost(element.getAsString()));
            }
        }
        return jsonArray;
    }

    public JsonObject pingInfoHost(String host) {
        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty(PING_INFO_ORIGIN, host);
        boolean contain = responseSpeedMap.containsKey(host);
        if (contain) {
            jsonObject.addProperty(PING_INFO_PING, responseSpeedMap.get(host));
            jsonObject.addProperty(PING_INFO_VALID, true);
        } else {
            jsonObject.addProperty(PING_INFO_PING, 10000d);
            jsonObject.addProperty(PING_INFO_VALID, false);
        }
        return jsonObject;
    }

    public void pingHost(final String origin, Callback callback) {
        Request request = new Request.Builder()
                .url(origin + "/ping")
                .header("platform","android")
                .header("version", WhiteSdk.Version())
                .build();
        client.newCall(request).enqueue(callback);
    }


}
