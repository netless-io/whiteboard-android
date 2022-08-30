package com.herewhite.sdk;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.herewhite.sdk.domain.ConversionInfo;
import com.herewhite.sdk.domain.ConvertErrorCode;
import com.herewhite.sdk.domain.ConvertException;
import com.herewhite.sdk.domain.ConvertedFiles;
import com.herewhite.sdk.domain.ConverterStatus;
import com.herewhite.sdk.domain.PptPage;
import com.herewhite.sdk.domain.Scene;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Converter {

    static final String PPT_ORIGIN = "https://cloudcapiv4.herewhite.com";
    private static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");
    static ExecutorService poolExecutor = Executors.newSingleThreadExecutor();
    OkHttpClient client = new OkHttpClient();
    private String roomToken;
    private Gson gson;
    private long interval;
    private long timeout;
    private String taskId;
    private boolean converting = true;
    private Date beginDate;
    private ConverterStatus status;
    public Converter(String roomToken) {
        this(roomToken, 15 * 1000, 3 * 60 * 1000);
    }
    public Converter(String roomToken, long pollingInterval, long timeout) {
        this.roomToken = roomToken;
        gson = new Gson();
        status = ConverterStatus.Idle;
        this.interval = pollingInterval;
        this.timeout = timeout;
    }

    public String getRoomToken() {
        return roomToken;
    }

    public String getTaskId() {
        return taskId;
    }

    public long getInterval() {
        return interval;
    }

    public void setInterval(long interval) {
        this.interval = interval;
    }

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public ConverterStatus getStatus() {
        return status;
    }

    public void startConvertTask(final String url, final ConvertType type, final ConverterCallbacks callback) {

        beginDate = new Date();
        final Converter that = this;
        poolExecutor.execute(new Runnable() {
            @Override
            public void run() {

                final CountDownLatch latch = new CountDownLatch(1);
                that.createConvertTask(url, type, new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        that.status = ConverterStatus.CreateFail;
                        ConvertException convertE = new ConvertException(ConvertErrorCode.CreatedFail, e);
                        callback.onFailure(convertE);
                        latch.countDown();
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        JsonObject json = gson.fromJson(response.body().string(), JsonObject.class);
                        if (response.code() == 200) {
                            JsonObject msg = json.getAsJsonObject("msg");
                            boolean succeed = msg.getAsJsonPrimitive("succeed").getAsBoolean();
                            if (succeed) {
                                that.status = ConverterStatus.Created;
                                that.taskId = json.getAsJsonObject("msg").get("taskUUID").getAsString();
                            } else {
                                that.status = ConverterStatus.CreateFail;
                                ConvertException e = new ConvertException(ConvertErrorCode.CreatedFail, gson.toJson(json));
                                callback.onFailure(e);
                            }
                        } else {
                            that.status = ConverterStatus.CreateFail;
                            ConvertException e = new ConvertException(ConvertErrorCode.ConvertFail, gson.toJson(json));
                            callback.onFailure(e);
                        }
                        latch.countDown();
                    }
                });

                try {
                    latch.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if (that.status == ConverterStatus.CreateFail) {
                    return;
                }

                that.polling(taskId, type, new ConvertCallback() {
                    @Override
                    public void onConvertProgress(Double progress, ConversionInfo info) {
                        callback.onProgress(progress, info);
                    }

                    @Override
                    public void onConvertFinish(final ConversionInfo info) {
                        that.status = ConverterStatus.Success;
                        callback.onFinish(that.getPpt(info, type), info);
                    }

                    @Override
                    public void onConvertFailure(ConvertException e) {
                        // 错误时状态，各个上一级根据情况设置状态码
                        callback.onFailure(e);
                    }
                });
            }
        });
    }

    private void createConvertTask(String url, ConvertType type, final Callback callback) {
        String typeUrl = type.equals(ConvertType.Dynamic) ? "dynamic_conversion" : "static_conversion";

        Map<String, String> roomSpec = new HashMap<>();
        roomSpec.put("sourceUrl", url);
        roomSpec.put("serviceType", typeUrl);
        RequestBody body = RequestBody.create(JSON, gson.toJson(roomSpec));

        Request request = new Request.Builder()
                .url(PPT_ORIGIN + "/services/conversion/tasks?roomToken=" + this.roomToken)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .post(body)
                .build();
        final Call call = client.newCall(request);
        call.enqueue(callback);
    }

    private void polling(String taskId, ConvertType type, final ConvertCallback callbacks) {
        boolean canCheck = this.status == ConverterStatus.Timeout || this.status == ConverterStatus.CheckingFail || this.status == ConverterStatus.GetDynamicFail;
        if (this.status != ConverterStatus.Created && !canCheck) {
            return;
        }

        final Converter that = this;
        Date expireDate = new Date(this.beginDate.getTime() + this.timeout);
        while (converting && expireDate.after(new Date())) {
            final CountDownLatch latch = new CountDownLatch(1);
            that.status = ConverterStatus.Checking;
            checkProgress(taskId, type, new CheckCallback() {
                @Override
                public void onCheckResponse(ConversionInfo info) {
                    ConversionInfo.ServerConversionStatus status = info.getConvertStatus();
                    if (status == ConversionInfo.ServerConversionStatus.Fail || status == ConversionInfo.ServerConversionStatus.NotFound) {
                        converting = false;
                        that.status = ConverterStatus.Fail;
                        ConvertErrorCode code = status == ConversionInfo.ServerConversionStatus.Fail ? ConvertErrorCode.ConvertFail : ConvertErrorCode.NotFound;
                        ConvertException e = new ConvertException(code, info.getReason());
                        callbacks.onConvertFailure(e);
                    } else if (status == ConversionInfo.ServerConversionStatus.Finished) {
                        //成功时，可能还要额外获取动态 ppt 内容，不直接设置状态
                        converting = false;
                        callbacks.onConvertFinish(info);
                    } else {
                        that.status = ConverterStatus.WaitingForNextCheck;
                        callbacks.onConvertProgress(info.getConvertedPercentage(), info);
                    }
                    try {
                        Thread.sleep(interval);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    latch.countDown();
                }

                @Override
                public void onCheckFailure(Exception e) {
                    ConvertException exp = new ConvertException(ConvertErrorCode.CheckFail);
                    callbacks.onConvertFailure(exp);
                    converting = false;
                    latch.countDown();
                }
            });
            try {
                latch.await();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (this.status == ConverterStatus.WaitingForNextCheck) {
            this.status = ConverterStatus.Timeout;
            ConvertException exp = new ConvertException(ConvertErrorCode.CheckTimeout);
            callbacks.onConvertFailure(exp);
        }
    }

    private void checkProgress(String taskId, ConvertType type, final CheckCallback checkCallback) {

        String typeUrl = type.equals(ConvertType.Dynamic) ? "dynamic_conversion" : "static_conversion";

        Request request = new Request.Builder()
                .url(PPT_ORIGIN + "/services/conversion/tasks/" + taskId + "/progress?roomToken=" + this.roomToken + "&serviceType=" + typeUrl)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .build();
        Call call = client.newCall(request);

        final CountDownLatch latch = new CountDownLatch(1);
        final Converter that = this;

        this.status = ConverterStatus.Checking;
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                checkCallback.onCheckFailure(e);
                that.status = ConverterStatus.CheckingFail;
                latch.countDown();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                JsonObject json = gson.fromJson(response.body().string(), JsonObject.class);
                if (response.code() == 200) {
                    JsonObject task = json.getAsJsonObject("msg").getAsJsonObject("task");
                    ConversionInfo info = gson.fromJson(gson.toJson(task), ConversionInfo.class);
                    checkCallback.onCheckResponse(info);
                } else {
                    ConvertException e = new ConvertException(ConvertErrorCode.ConvertFail, gson.toJson(json));
                    checkCallback.onCheckFailure(e);
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

    private ConvertedFiles getPpt(ConversionInfo info, ConvertType type) {
        int fileLength = info.getConvertedFileList().length;
        String[] sliderURLs = new String[fileLength];
        Scene[] scenes = new Scene[fileLength];

        ConvertedFiles files = new ConvertedFiles();
        files.setTaskId(taskId);
        files.setType(convertType(type));

        for (int i = 0; i < fileLength; i++) {
            PptPage pptPage = info.getConvertedFileList()[i];
            pptPage.setSrc(info.getPrefix() + pptPage.getSrc());
            sliderURLs[i] = pptPage.getSrc();
            scenes[i] = new Scene(String.valueOf(i + 1), pptPage);
        }

        files.setSlideURLs(sliderURLs);
        files.setScenes(scenes);

        return files;
    }

    private com.herewhite.sdk.converter.ConvertType convertType(ConvertType convertType) {
        if (convertType == Converter.ConvertType.Static) {
            return com.herewhite.sdk.converter.ConvertType.Static;
        } else {
            return com.herewhite.sdk.converter.ConvertType.Dynamic;
        }
    }

    public enum ConvertType {
        Unknown,
        Static,
        Dynamic,
    }

    private interface ConvertCallback {
        void onConvertProgress(Double progress, ConversionInfo info);

        void onConvertFinish(ConversionInfo info);

        void onConvertFailure(ConvertException e);
    }

    private interface CheckCallback {
        void onCheckResponse(ConversionInfo info);

        void onCheckFailure(Exception e);
    }
}
