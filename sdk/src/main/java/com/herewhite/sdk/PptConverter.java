package com.herewhite.sdk;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.herewhite.sdk.domain.ConvertedPpt;
import com.herewhite.sdk.domain.PptConvertInfo;
import com.herewhite.sdk.domain.PptPage;
import com.herewhite.sdk.domain.Scene;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class PptConverter {

    public enum PptConverterStatus {
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

    public class PptConvertException extends Exception {

        private int code;

        PptConvertException(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }
    }

    public enum PptConvertErrorCode {

        CreatedFail(20001),
        ConvertFail(20002),
        NotFound(20003),
        CheckTimeout(20004);

        private int code;
        PptConvertErrorCode(int code) {
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

    public PptConverterStatus getStatus() {
        return status;
    }

    private PptConverterStatus status;
    public PptConverter(String roomToken) {
        roomToken = roomToken;
        gson = new Gson();
        status = PptConverterStatus.Idle;
    }

    static ExecutorService poolExecutor = Executors.newSingleThreadExecutor();
    OkHttpClient client = new OkHttpClient();

    static final String PPT_ORIGIN = "https://cloudcapiv4.herewhite.com";
    static final String PPT_ASSETS_ORIGIN = "https://white-cn-doc-convert.oss-cn-hangzhou.aliyuncs.com/dynamicConvert";

    public void createConvertTask(String url,final PptType type, long timeout, long interval,final PptConverterCallbacks callback) {

        this.timeout = timeout;
        this.interval = interval;
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
        final PptConverter that = this;

        poolExecutor.execute(new Runnable() {
            @Override
            public void run() {

                final CountDownLatch latch = new CountDownLatch(1);
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        that.status = PptConverterStatus.CreateFail;
                        //TODO:能在 exception 里加错误码之类额外的信息吗？
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
                                that.status = PptConverterStatus.Created;
                                that.taskId = room.getAsJsonObject("msg").get("taskUUID").getAsString();
                            } else {
                                that.status = PptConverterStatus.CreateFail;
                            }
                        } else {
                            //TODO:添加具体的错误信息
                            that.status = PptConverterStatus.CreateFail;
                        }
                        latch.countDown();
                    }
                });

                try {
                    latch.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                that.checkResult(taskId, type, new ResultCallback() {
                    @Override
                    public void onFinish(final PptConvertInfo info) {
                        if (type != PptType.Dynamic) {
                            that.getDynamicPpt(taskId, new DynamicPptCallbacks() {
                                @Override
                                public void onSuccess(ConvertedPpt ppt) {
                                    callback.onFinish(ppt, info);
                                }

                                @Override
                                public void onFail(Exception e) {
                                    callback.onFailure(e);
                                }
                            });
                        } else {
                            callback.onFinish(that.transformPptInfo(info), info);
                        }
                    }

                    @Override
                    public void onFail(Exception e) {
                        callback.onFailure(e);
                    }
                });
            }
        });
    }

    interface ResultCallback {
        void onFinish(PptConvertInfo info);
        void onFail(Exception e);
    }

    private void checkResult(String taskId, PptType type, final ResultCallback callbacks) {
        Boolean canCheck = this.status == PptConverterStatus.Timeout || this.status == PptConverterStatus.CheckingFail || this.status == PptConverterStatus.GetDynamicFail;
        if (this.status != PptConverterStatus.Created && !canCheck) {
            return;
        }

        final PptConverter that = this;

        while (converting) {
            final CountDownLatch latch = new CountDownLatch(1);
            checkProgress(taskId, type, new ProgressCallback() {
                @Override
                public void onProgress(PptConvertInfo info) {
                    PptConvertInfo.ServerConversionStatus status = info.getConvertStatus();
                    if (status == PptConvertInfo.ServerConversionStatus.Fail || status == PptConvertInfo.ServerConversionStatus.NotFound) {
                        that.status = PptConverterStatus.Fail;
                        converting = false;
                        PptConvertException e = new PptConvertException(PptConvertErrorCode.ConvertFail.getCode());
                        callbacks.onFail(e);
                    } else if (status == PptConvertInfo.ServerConversionStatus.Finished) {
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
                public void onFail(Exception e) {
                    that.status = PptConverterStatus.CheckingFail;
                    callbacks.onFail(e);
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
        void onProgress(PptConvertInfo info);
        void onFail(Exception e);
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
        final PptConverter that = this;

        this.status = PptConverterStatus.Checking;
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                progressCallback.onFail(e);
                that.status = PptConverterStatus.CheckingFail;
                latch.countDown();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.code() == 200) {
                    JsonObject json = gson.fromJson(response.body().string(), JsonObject.class);
                    PptConvertInfo info = gson.fromJson(json.getAsJsonObject("msg").getAsJsonObject("task").getAsString(), PptConvertInfo.class);
                    progressCallback.onProgress(info);
                } else {

                }
                that.status = PptConverterStatus.WaitingForNextCheck;
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
        void onSuccess(ConvertedPpt ppt);
        void onFail(Exception e);
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

        final PptConverter that = this;

        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                //TODO:封装异常，增加错误信息
                dynamicPptCallbacks.onFail(e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                JsonObject json = gson.fromJson(response.body().string(), JsonObject.class);

                Integer count = json.get("totalPageSize").getAsInt();

                String[] sliderURLs = new String[count];
                Scene[] scenes = new Scene[count];

                ConvertedPpt ppt = new ConvertedPpt();
                ppt.setTaskId(that.taskId);
                ppt.setType(PptType.Dynamic);

                for (int i = 0; i < count; i++) {
                    PptPage pptPage = new PptPage(String.valueOf(i+1), json.get("width").getAsDouble(), json.get("height").getAsDouble());
                    pptPage.setSrc(prefix + "/slide/slide" + (i+1) + ".xml");
                    sliderURLs[i] = pptPage.getSrc();
                    scenes[i] = new Scene(String.valueOf(i+1), pptPage);
                }

                ppt.setSlideURLs(sliderURLs);
                ppt.setScenes(scenes);
                dynamicPptCallbacks.onSuccess(ppt);
            }
        });
    }

    private ConvertedPpt transformPptInfo(PptConvertInfo info) {

        int fileLength = info.getStaticConversionFileList().length;
        String[] sliderURLs = new String[fileLength];
        Scene[] scenes = new Scene[fileLength];

        ConvertedPpt ppt = new ConvertedPpt();
        ppt.setTaskId(taskId);
        ppt.setType(PptType.Static);

        for (int i = 0; i < fileLength; i++) {
            PptPage pptPage = info.getStaticConversionFileList()[i];
            pptPage.setSrc(info.getPrefix() + pptPage.getSrc());
            sliderURLs[i] = pptPage.getSrc();
            scenes[i] = new Scene(String.valueOf(i+1), pptPage);
        }

        ppt.setSlideURLs(sliderURLs);
        ppt.setScenes(scenes);

        return ppt;
    }
}
