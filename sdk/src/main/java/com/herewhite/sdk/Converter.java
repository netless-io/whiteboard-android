package com.herewhite.sdk;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.herewhite.sdk.domain.ConvertErrorCode;
import com.herewhite.sdk.domain.ConvertException;
import com.herewhite.sdk.domain.ConvertedFiles;
import com.herewhite.sdk.domain.ConversionInfo;
import com.herewhite.sdk.domain.ConverterStatus;
import com.herewhite.sdk.domain.PptPage;
import com.herewhite.sdk.domain.Scene;

import java.io.IOException;
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

    public enum ConvertType {
        Unknown,
        Static,
        Dynamic,
    }

    public String getRoomToken() {
        return roomToken;
    }

    private String roomToken;
    private Gson gson;

    public String getTaskId() {
        return taskId;
    }

    private long interval;
    private long timeout;
    private String taskId;
    private boolean converting = true;

    public ConverterStatus getStatus() {
        return status;
    }

    private ConverterStatus status;
    public Converter(String roomToken) {
        new Converter(roomToken, 15 * 1000, 3 * 60 * 1000);
    }

    public Converter(String roomToken, long pollingInterval, long timeout) {
        this.roomToken = roomToken;
        gson = new Gson();
        status = ConverterStatus.Idle;
        this.interval = pollingInterval;
        this.timeout = timeout;
    }

    private static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");
    static ExecutorService poolExecutor = Executors.newSingleThreadExecutor();
    static final String PPT_ORIGIN = "https://cloudcapiv4.herewhite.com";
    static final String PPT_ASSETS_ORIGIN = "https://white-cn-doc-convert.oss-cn-hangzhou.aliyuncs.com/dynamicConvert";

    OkHttpClient client = new OkHttpClient();

    public void startConvertTask(final String url, final ConvertType type, final ConverterCallbacks callback) {

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

                that.polling(taskId, type, new ConvertingCallback() {
                    @Override
                    public void onProgress(Double progress, ConversionInfo info) {
                        callback.onProgress(progress, info);
                    }

                    @Override
                    public void onFinish(final ConversionInfo info) {
                        if (type == ConvertType.Dynamic) {
                            that.getDynamicPpt(taskId, new DynamicPptCallbacks() {
                                @Override
                                public void onSuccess(ConvertedFiles ppt) {
                                    callback.onFinish(ppt, info);
                                }

                                @Override
                                public void onFailure(Exception e) {
                                    ConvertException exception = new ConvertException(ConvertErrorCode.GetDynamicFail, e);
                                    callback.onFailure(exception);
                                }
                            });
                        } else {
                            callback.onFinish(that.getStaticPpt(info), info);
                        }
                    }

                    @Override
                    public void onFailure(ConvertException e) {
                        callback.onFailure(e);
                    }
                });
            }
        });
    }

    interface ConvertingCallback {
        void onProgress(Double progress, ConversionInfo info);
        void onFinish(ConversionInfo info);
        void onFailure(ConvertException e);
    }

    private void createConvertTask(String url, ConvertType type, final Callback callback) {
        String typeUrl = type.equals(ConvertType.Dynamic) ? "dynamic" : "static";

        Map<String, String> roomSpec = new HashMap<>();
        roomSpec.put("sourceUrl", url);
        RequestBody body = RequestBody.create(JSON, gson.toJson(roomSpec));

        Request request = new Request.Builder()
                .url(PPT_ORIGIN + "/services/" + typeUrl + "-conversion/tasks?roomToken=" + this.roomToken)
                .header("Content-Type", "application/json")
                .header("Accept","application/json")
                .post(body)
                .build();
        final Call call = client.newCall(request);
        call.enqueue(callback);
    }

    private void polling(String taskId, ConvertType type, final ConvertingCallback callbacks) {
        boolean canCheck = this.status == ConverterStatus.Timeout || this.status == ConverterStatus.CheckingFail || this.status == ConverterStatus.GetDynamicFail;
        if (this.status != ConverterStatus.Created && !canCheck) {
            return;
        }

        final Converter that = this;
        while (converting) {
            final CountDownLatch latch = new CountDownLatch(1);
            checkProgress(taskId, type, new ProgressCallback() {
                @Override
                public void onProgress(ConversionInfo info) {
                    ConversionInfo.ServerConversionStatus status = info.getConvertStatus();
                    if (status == ConversionInfo.ServerConversionStatus.Fail || status == ConversionInfo.ServerConversionStatus.NotFound) {
                        converting = false;
                        that.status = ConverterStatus.Fail;
                        ConvertErrorCode code = status == ConversionInfo.ServerConversionStatus.Fail ? ConvertErrorCode.ConvertFail : ConvertErrorCode.NotFound;
                        ConvertException e = new ConvertException(code, info.getReason());
                        callbacks.onFailure(e);
                    } else if (status == ConversionInfo.ServerConversionStatus.Finished) {
                        converting = false;
                        callbacks.onFinish(info);
                    } else {
                        callbacks.onProgress(info.getConvertedPercentage(), info);
                    }
                    try {
                        Thread.sleep(interval);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    latch.countDown();
                }

                @Override
                public void onFailure(Exception e) {
                    that.status = ConverterStatus.CheckingFail;
                    ConvertException exp = new ConvertException(ConvertErrorCode.CheckFail);
                    callbacks.onFailure(exp);
                    latch.countDown();
                }
            });
            try {
                latch.await();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private interface ProgressCallback {
        void onProgress(ConversionInfo info);
        void onFailure(Exception e);
    }

    private void checkProgress(String taskId, ConvertType type, final ProgressCallback progressCallback) {

        String typeUrl = type.equals(ConvertType.Dynamic) ? "dynamic" : "static";

        Request request = new Request.Builder()
                .url(PPT_ORIGIN + "/services/" + typeUrl + "-conversion/tasks/" + taskId +"/progress?roomToken=" + this.roomToken)
                .header("Content-Type", "application/json")
                .header("Accept","application/json")
                .build();
        Call call = client.newCall(request);

        final CountDownLatch latch = new CountDownLatch(1);
        final Converter that = this;

        this.status = ConverterStatus.Checking;
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                progressCallback.onFailure(e);
                that.status = ConverterStatus.CheckingFail;
                latch.countDown();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                JsonObject json = gson.fromJson(response.body().string(), JsonObject.class);
                if (response.code() == 200) {
                    JsonObject task = json.getAsJsonObject("msg").getAsJsonObject("task");
                    ConversionInfo info = gson.fromJson(gson.toJson(task), ConversionInfo.class);
                    progressCallback.onProgress(info);
                    that.status = ConverterStatus.WaitingForNextCheck;
                } else {
                    that.status = ConverterStatus.CheckingFail;
                    ConvertException e = new ConvertException(ConvertErrorCode.ConvertFail, gson.toJson(json));
                    progressCallback.onFailure(e);
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

    private interface DynamicPptCallbacks {
        void onSuccess(ConvertedFiles ppt);
        void onFailure(Exception e);
    }

    public void getDynamicPpt(String taskId, final DynamicPptCallbacks dynamicPptCallbacks) {
        final String prefix = PPT_ASSETS_ORIGIN + "/" + taskId;
        Request request = new Request.Builder()
                .url(prefix + "/info.json")
                .header("Content-Type", "application/json")
                .header("Accept","application/json")
                .build();
        Call call = client.newCall(request);

        final Converter that = this;

        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                dynamicPptCallbacks.onFailure(e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.code() == 200) {
                    ConvertedFiles ppt = that.getDynamicPpt(response);
                    dynamicPptCallbacks.onSuccess(ppt);
                } else {
                    ConvertException e = new ConvertException(ConvertErrorCode.GetDynamicFail);
                    dynamicPptCallbacks.onFailure(e);
                }

            }
        });
    }

    private ConvertedFiles getDynamicPpt(Response response) throws IOException {
        JsonObject json = gson.fromJson(response.body().string(), JsonObject.class);
        Integer count = json.get("totalPageSize").getAsInt();

        String[] sliderURLs = new String[count];
        Scene[] scenes = new Scene[count];

        ConvertedFiles files = new ConvertedFiles();
        files.setTaskId(this.taskId);
        files.setType(ConvertType.Dynamic);

        final String prefix = PPT_ASSETS_ORIGIN + "/" + taskId;

        for (int i = 0; i < count; i++) {
            PptPage pptPage = new PptPage(String.valueOf(i+1), json.get("width").getAsDouble(), json.get("height").getAsDouble());
            pptPage.setSrc(prefix + "/slide/slide" + (i+1) + ".xml");
            sliderURLs[i] = pptPage.getSrc();
            scenes[i] = new Scene(String.valueOf(i+1), pptPage);
        }

        files.setSlideURLs(sliderURLs);
        files.setScenes(scenes);
        return files;
    }

    private ConvertedFiles getStaticPpt(ConversionInfo info) {

        int fileLength = info.getStaticConversionFileList().length;
        String[] sliderURLs = new String[fileLength];
        Scene[] scenes = new Scene[fileLength];

        ConvertedFiles files = new ConvertedFiles();
        files.setTaskId(taskId);
        files.setType(ConvertType.Static);

        for (int i = 0; i < fileLength; i++) {
            PptPage pptPage = info.getStaticConversionFileList()[i];
            pptPage.setSrc(info.getPrefix() + pptPage.getSrc());
            sliderURLs[i] = pptPage.getSrc();
            scenes[i] = new Scene(String.valueOf(i+1), pptPage);
        }

        files.setSlideURLs(sliderURLs);
        files.setScenes(scenes);

        return files;
    }
}
