package com.herewhite.demo.common;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.herewhite.demo.R;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by buhe on 2018/8/16.
 */

public class DemoAPI {
    private static final String TAG = DemoAPI.class.getSimpleName();
    public static final String DEFAULT_UID = "5e62a5c0-8c15-4b00-a9fc-0e309e91da30";

    private static DemoAPI instance;
    private String sdkToken;
    private String appId;
    private String roomUUID;
    private String roomToken;

    public synchronized static DemoAPI get() {
        if (instance == null) {
            instance = new DemoAPI();
        }

        return instance;
    }

    public void init(Context context) {
        appId = context.getString(R.string.sdk_app_id);
        sdkToken = context.getString(R.string.sdk_app_token);

        roomUUID = context.getString(R.string.room_uuid);
        roomToken = context.getString(R.string.room_token);
    }

    public String getAppId() {
        return appId;
    }

    public String getRoomUUID() {
        return roomUUID;
    }

    public String getRoomToken() {
        return roomToken;
    }

    public String getSdkToken() {
        return sdkToken;
    }

    public boolean hasDemoInfo() {
        return roomUUID.length() > 0 && roomToken.length() > 0;
    }

    public boolean invalidToken() {
        return !hasDemoInfo() && sdkToken.length() <= 50;
    }

    public interface Result {
        void success(String uuid, String token);

        void fail(String message);
    }

    public void getNewRoom(final Result result) {
        if (hasDemoInfo()) {
            result.success(roomUUID, roomToken);
            return;
        }

        ApiService.createRoom(sdkToken, 100, "cn-hz", new ApiCallback<CreateRoomResult>() {
            @Override
            public void onSuccess(CreateRoomResult data) {
                roomUUID = data.uuid;
                getRoomToken(data.uuid, result);
            }

            @Override
            public void onFailure(String message) {
                result.fail(message);
            }
        });
    }

    public void getRoomToken(final String uuid, final Result result) {
        if (uuid.equals(roomUUID) && hasDemoInfo()) {
            result.success(roomUUID, roomToken);
            return;
        }

        ApiService.createRoomToken(sdkToken, uuid, "cn-hz", new ApiCallback<String>() {
            @Override
            public void onSuccess(String token) {
                roomToken = token;
                result.success(uuid, token);
            }

            @Override
            public void onFailure(String message) {
                result.fail(message);
            }
        });
    }

    private final OkHttpClient client = new OkHttpClient();

    public void downloadZip(String zipUrl, String des) {
        Request request = new Request.Builder().url(zipUrl).build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "download error: " + e.toString());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    throw new IOException(("下载失败: " + response));
                }
                String path = des + "/convertcdn.netless.link/dynamicConvert";
                File file = new File(path);
                if (!file.exists()) {
                    boolean success = file.mkdirs();
                    Log.i("LocalFile", "success: " + success + " path: " + path);
                } else {
                    Log.i("LocalFile", path + " is exist");
                }

                FileOutputStream fos = new FileOutputStream(path + "/1.zip", false);
                fos.write(response.body().bytes());
                fos.close();
                unzip(new File(path + "/1.zip"), new File(path));
                Log.i("LocalFile", "unzip");
            }
        });
    }

    private static void unzip(File zipFile, File targetDirectory) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(new BufferedInputStream(new FileInputStream(zipFile)))) {
            ZipEntry ze;
            while ((ze = zis.getNextEntry()) != null) {
                File file = new File(targetDirectory, ze.getName());
                File dir = ze.isDirectory() ? file : file.getParentFile();
                if (!dir.isDirectory() && !dir.mkdirs())
                    throw new FileNotFoundException("Failed to ensure directory: " + dir.getAbsolutePath());
                if (ze.isDirectory())
                    continue;
                try (FileOutputStream fos = new FileOutputStream(file)) {
                    int count;
                    byte[] buffer = new byte[8192];
                    while ((count = zis.read(buffer)) != -1)
                        fos.write(buffer, 0, count);
                }
            }
        }
    }
}
