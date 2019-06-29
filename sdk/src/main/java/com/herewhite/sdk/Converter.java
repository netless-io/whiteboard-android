package com.herewhite.sdk;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.herewhite.sdk.domain.ConvertedFiles;
import com.herewhite.sdk.domain.ConversionInfo;
import com.herewhite.sdk.domain.PptPage;
import com.herewhite.sdk.domain.Scene;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Converter {

    public enum ConverterStatus {
        Idle,
        Created,
        CreateFail,
        Checking,
        WaitingForNextCheck,
        Timeout,
        CheckingFail,
        GetDynamicFail,
        Success,
        Fail,
    }

    public enum PptType {
        Unknown,
        Static,
        Dynamic,
    }

    public class ConvertException extends Exception {

        private int code;

        ConvertException(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }
    }

    public enum ConvertErrorCode {

        CreatedFail(20001),
        ConvertFail(20002),
        NotFound(20003),
        CheckTimeout(20004);

        private int code;
        ConvertErrorCode(int code) {
            this.code=code;
        }

        public int getCode() {
            return code;
        }
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
    private Boolean converting = false;

    public ConverterStatus getStatus() {
        return status;
    }

    private ConverterStatus status;
    public Converter(String roomToken) {
        this.roomToken = roomToken;
        gson = new Gson();
        status = ConverterStatus.Idle;
        this.interval = 15000;
        this.timeout = 3 * 60 * 1000;
    }

    public Converter(String roomToken, long pollingInterval, long timeout) {
        this.roomToken = roomToken;
        gson = new Gson();
        status = ConverterStatus.Idle;
        this.interval = pollingInterval;
        this.timeout = timeout;
    }

    static ExecutorService poolExecutor = Executors.newSingleThreadExecutor();
    OkHttpClient client = new OkHttpClient();

    static final String PPT_ORIGIN = "https://cloudcapiv4.herewhite.com";
    static final String PPT_ASSETS_ORIGIN = "https://white-cn-doc-convert.oss-cn-hangzhou.aliyuncs.com/dynamicConvert";

    public void startConvertTask(String url, final PptType type, final ConverterCallbacks callback) {
        String typeUrl = type.equals(PptType.Dynamic) ? "dynamic" : "static";

        FormBody formBody = new FormBody.Builder()
                .add("sourceUrl", url)
                .build();

        Request request = new Request.Builder()
                .url(PPT_ORIGIN + "/services/" + typeUrl + "-conversion/tasks?roomToken=" + this.roomToken)
                .header("Content-Type", "application/json")
                .header("Accept","application/json")
                .post(formBody)
                .build();

        final Call call = client.newCall(request);
        final Converter that = this;

        poolExecutor.execute(new Runnable() {
            @Override
            public void run() {

                final CountDownLatch latch = new CountDownLatch(1);
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        that.status = ConverterStatus.CreateFail;
                        try {
                            callback.onFailure(e);
                        } catch (Exception exception) {
                            Logger.error("ppt converter", exception);
                        }
                        latch.countDown();
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (response.code() == 200) {
                            JsonObject room = gson.fromJson(response.body().string(), JsonObject.class);
                            Boolean succeed = room.getAsJsonObject("msg").getAsJsonObject("succeed").getAsBoolean();
                            if (succeed) {
                                that.status = ConverterStatus.Created;
                                that.taskId = room.getAsJsonObject("msg").get("taskUUID").getAsString();
                            } else {
                                that.status = ConverterStatus.CreateFail;
                            }
                        } else {
                            that.status = ConverterStatus.CreateFail;
                        }
                        latch.countDown();
                    }
                });

                try {
                    latch.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if (that.status == ConverterStatus.CreateFail) {
                    return;
                }

                that.checkResult(taskId, type, new ResultCallback() {
                    @Override
                    public void onFinish(final ConversionInfo info) {
                        if (type != PptType.Dynamic) {
                            that.getDynamicPpt(taskId, new DynamicPptCallbacks() {
                                @Override
                                public void onSuccess(ConvertedFiles ppt) {
                                    callback.onFinish(ppt, info);
                                }

                                @Override
                                public void onFailure(Exception e) {
                                    callback.onFailure(e);
                                }
                            });
                        } else {
                            callback.onFinish(that.getStaticPpt(info), info);
                        }
                    }

                    @Override
                    public void onFailure(Exception e) {
                        callback.onFailure(e);
                    }
                });
            }
        });
    }

    interface ResultCallback {
        void onFinish(ConversionInfo info);
        void onFailure(Exception e);
    }

    private void checkResult(String taskId, PptType type, final ResultCallback callbacks) {
        Boolean canCheck = this.status == ConverterStatus.Timeout || this.status == ConverterStatus.CheckingFail || this.status == ConverterStatus.GetDynamicFail;
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
                        that.status = ConverterStatus.Fail;
                        converting = false;
                        ConvertException e = new ConvertException(ConvertErrorCode.ConvertFail.getCode());
                        callbacks.onFailure(e);
                    } else if (status == ConversionInfo.ServerConversionStatus.Finished) {
                        converting = false;
                        callbacks.onFinish(info);
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
                    callbacks.onFailure(e);
                    latch.countDown();
                }
            });
            try {
                latch.wait();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private interface ProgressCallback {
        void onProgress(ConversionInfo info);
        void onFailure(Exception e);
    }

    private void checkProgress(String taskId, PptType type, final ProgressCallback progressCallback) {

        String typeUrl = type.equals(PptType.Dynamic) ? "dynamic" : "static";

        Request request = new Request.Builder()
                .url(PPT_ASSETS_ORIGIN + "/services/" + typeUrl + "-conversion/tasks/" + taskId +"/progress?roomToken=" + this.roomToken)
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
                if (response.code() == 200) {
                    JsonObject json = gson.fromJson(response.body().string(), JsonObject.class);
                    ConversionInfo info = gson.fromJson(json.getAsJsonObject("msg").getAsJsonObject("task").getAsString(), ConversionInfo.class);
                    progressCallback.onProgress(info);
                    that.status = ConverterStatus.WaitingForNextCheck;
                } else {
                    that.status = ConverterStatus.CheckingFail;
                    ConvertException e = new ConvertException(ConvertErrorCode.ConvertFail.getCode());
                    progressCallback.onFailure(e);
                }
                latch.countDown();
            }
        });

        try {
            latch.wait();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private interface DynamicPptCallbacks {
        void onSuccess(ConvertedFiles ppt);
        void onFailure(Exception e);
    }

    public void getDynamicPpt(String taskId, final DynamicPptCallbacks dynamicPptCallbacks) {
        FormBody formBody = new FormBody.Builder()
                .build();

        final String prefix = PPT_ASSETS_ORIGIN + "/" + taskId;
        Request request = new Request.Builder()
                .url(prefix + "/info.json")
                .header("Content-Type", "application/json")
                .header("Accept","application/json")
                .post(formBody)
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
                    ConvertException e = new ConvertException(ConvertErrorCode.ConvertFail.getCode());
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
        files.setType(PptType.Dynamic);

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
        files.setType(PptType.Static);

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
