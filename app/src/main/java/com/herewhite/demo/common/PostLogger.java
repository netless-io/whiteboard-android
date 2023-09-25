package com.herewhite.demo.common;

import android.util.Log;

import java.io.IOException;
import java.net.URLEncoder;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class PostLogger {
    static OkHttpClient client = new OkHttpClient();
    static String url = "http://10.6.0.73:8080";

    public static void log(String message) {
        Log.e("PostLogger", message);

        Request request = new Request.Builder()
                .url(url + "?message=" + URLEncoder.encode(message))
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                System.out.println("log failed");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                System.out.println("log success");
            }
        });
    }
}
