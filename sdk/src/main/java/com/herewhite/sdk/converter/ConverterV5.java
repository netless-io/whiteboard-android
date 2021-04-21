package com.herewhite.sdk.converter;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.herewhite.sdk.ConverterCallbacks;
import com.herewhite.sdk.domain.ConversionInfo;
import com.herewhite.sdk.domain.ConvertErrorCode;
import com.herewhite.sdk.domain.ConvertException;
import com.herewhite.sdk.domain.ConvertedFiles;
import com.herewhite.sdk.domain.ConverterStatus;
import com.herewhite.sdk.domain.PptPage;
import com.herewhite.sdk.domain.Region;
import com.herewhite.sdk.domain.Scene;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * 此类提供一个PPT转换思路，用户可依据{@see <a href="https://developer.netless.link/server-zh/home/server-conversion">ppt转换</a>}自行实现转换
 */
public class ConverterV5 {
    private static final String TAG = ConverterV5.class.getSimpleName();

    public enum OutputFormat {
        PNG,
        JPG,
        JPEG,
        WEBP,
        ;

        static Map<OutputFormat, String> map;

        static {
            map = new HashMap<>();
            map.put(PNG, "png");
            map.put(JPG, "jpg");
            map.put(JPEG, "jpeg");
            map.put(WEBP, "webp");
        }

        String getRequestValue() {
            return map.get(this);
        }
    }

    private String resource;
    private ConvertType type;
    private boolean preview;
    private double scale;
    private OutputFormat outputFormat;
    private boolean pack;
    private Region region;
    private String sdkToken;
    private String taskUuid;
    private String taskToken;
    private long interval;
    private long timeout;

    private long startTime;
    private ConverterCallbacks outCallbacks;
    private volatile ConverterStatus status = ConverterStatus.Created;

    private ConverterV5(String resource,
                        ConvertType type,
                        boolean preview,
                        double scale,
                        OutputFormat outputFormat,
                        boolean pack,
                        Region region,
                        String sdkToken,
                        String taskUuid,
                        String taskToken,
                        long interval,
                        long timeout,
                        ConverterCallbacks callbacks) {
        this.resource = resource;
        this.type = type;
        this.preview = preview;
        this.scale = scale;
        this.outputFormat = outputFormat;
        this.pack = pack;
        this.region = region;
        this.sdkToken = sdkToken;
        this.taskUuid = taskUuid;
        this.taskToken = taskToken;
        this.interval = interval;
        this.timeout = timeout;
        this.outCallbacks = callbacks;
    }

    static ThreadPoolExecutor executorService;

    static {
        executorService =
                new ThreadPoolExecutor(4, 4, 10L, TimeUnit.SECONDS, new LinkedBlockingQueue<>(),
                        new ConverterThreadFactory());
        executorService.allowCoreThreadTimeOut(true);
    }

    private static final class ConverterThreadFactory implements ThreadFactory {
        @Override
        public synchronized Thread newThread(Runnable runnable) {
            Thread result = new Thread(runnable, "white-sdk-converter");
            result.setPriority(Thread.MIN_PRIORITY);
            return result;
        }
    }

    private static MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static String PPT_BASE_URL = "https://api.netless.link/v5/services/conversion/tasks";
    private static String PROGRESS_URL_FORMAT = PPT_BASE_URL + "/%s?type=%s";

    private final Gson gson = new Gson();
    private final OkHttpClient client = new OkHttpClient();

    public void startConvertTask() {
        if (startTime != 0 && (status != ConverterStatus.Success || status != ConverterStatus.Fail)) {
            return;
        }
        startTime = System.currentTimeMillis();
        executorService.execute(() -> {
            if (taskUuid == null) {
                requestConvert();
            } else {
                status = ConverterStatus.Created;
            }
            if (status == ConverterStatus.Created) {
                startProgressLoop(taskToken != null ? taskToken : sdkToken);
            }
        });
    }

    /**
     * @return 转换状态
     */
    public ConverterStatus getStatus() {
        return status;
    }

    /**
     * @return 转换任务唯一标识
     */
    public String getTaskUuid() {
        return taskUuid;
    }

    /**
     * @return 转换任务查询token
     */
    public String getTaskToken() {
        return taskToken != null ? taskToken : sdkToken;
    }

    // Step 1: 发起文档转换
    private void requestConvert() {
        Map<String, Object> roomSpec = new HashMap<>();
        roomSpec.put("resource", resource);
        roomSpec.put("type", getRequestValue(type));
        roomSpec.put("preview", preview);
        if (type == ConvertType.Static) {
            roomSpec.put("scale", scale);
            roomSpec.put("outputFormat", outputFormat.getRequestValue());
            roomSpec.put("pack", pack);
        }
        RequestBody body = RequestBody.create(JSON, gson.toJson(roomSpec));

        Request request = new Request.Builder()
                .url(PPT_BASE_URL)
                .header("token", sdkToken)
                .header("region", getRequestValue(region))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .post(body)
                .build();
        final Call call = client.newCall(request);
        try {
            Response response = call.execute();
            if (response.code() >= 200 && response.code() <= 204) {
                JsonObject jsonObject = (JsonObject) JsonParser.parseString(response.body().string());
                taskUuid = jsonObject.get("uuid").getAsString();
                type = parseConvertType((jsonObject.get("type").getAsString()));
                status = ConverterStatus.Created;
            } else {
                onFailure(new ConvertException(ConvertErrorCode.ConvertFail, response.body().string()));
                status = ConverterStatus.CreateFail;
            }
        } catch (IOException e) {
            onFailure(new ConvertException(ConvertErrorCode.CreatedFail, e));
            status = ConverterStatus.CreateFail;
        }
    }

    private ConvertType parseConvertType(String type) {
        if ("static".equals(type)) {
            return ConvertType.Static;
        } else {
            return ConvertType.Dynamic;
        }
    }

    private String getRequestValue(ConvertType type) {
        if (type == ConvertType.Dynamic) {
            return "dynamic";
        } else {
            return "static";
        }
    }

    private String getRequestValue(Region region) {
        JsonElement regionElement = gson.toJsonTree(region);
        return regionElement.getAsString();
    }

    private void startProgressLoop(String token) {
        long timeLimit = startTime + timeout;
        try {
            status = ConverterStatus.Checking;
            while (System.currentTimeMillis() < timeLimit) {
                if (status != ConverterStatus.Checking) {
                    return;
                }
                checkProgress(token);
                try {
                    Thread.sleep(interval);
                } catch (InterruptedException e) {
                }
            }
        } catch (ConvertException e) {
            onFailure(e);
            status = ConverterStatus.Fail;
        }
        onFailure(new ConvertException(ConvertErrorCode.CheckTimeout));
        status = ConverterStatus.Timeout;
    }

    // Step 3: 轮询查询
    private void checkProgress(String token) throws ConvertException {
        Request request = new Request.Builder()
                .url(String.format(PROGRESS_URL_FORMAT, taskUuid, getRequestValue(type)))
                .header("token", token)
                .header("region", getRequestValue(region))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .build();
        Call call = client.newCall(request);

        try {
            Response response = call.execute();
            String body = response.body().string();

            System.out.println(body);
            if (response.code() == 200) {
                QueryInfo queryInfo = gson.fromJson(body, QueryInfo.class);
                ConversionInfo info = queryInfo.progress;
                ConversionInfo.ServerConversionStatus status = queryInfo.status;
                if (status == ConversionInfo.ServerConversionStatus.Fail || status == ConversionInfo.ServerConversionStatus.NotFound) {
                    ConvertErrorCode code = status == ConversionInfo.ServerConversionStatus.Fail ? ConvertErrorCode.ConvertFail : ConvertErrorCode.NotFound;
                    onFailure(new ConvertException(code, info.getReason()));
                    this.status = ConverterStatus.Fail;
                } else if (status == ConversionInfo.ServerConversionStatus.Finished) {
                    onFinish(getPpt(info, type), info);
                    this.status = ConverterStatus.Success;
                } else {
                    onProgress(info.getConvertedPercentage(), info);
                }
            } else {
                throw new ConvertException(ConvertErrorCode.ConvertFail, body);
            }
        } catch (IOException e) {
            throw new ConvertException(ConvertErrorCode.CheckFail, e);
        }
    }

    private void onFinish(ConvertedFiles convertedFiles, ConversionInfo info) {
        if (outCallbacks != null) {
            outCallbacks.onFinish(convertedFiles, info);
        }
    }

    private void onFailure(ConvertException e) {
        if (outCallbacks != null) {
            outCallbacks.onFailure(e);
        }
    }

    private void onProgress(Double convertedPercentage, ConversionInfo info) {
        if (outCallbacks != null) {
            outCallbacks.onProgress(convertedPercentage, info);
        }
    }

    private ConvertedFiles getPpt(ConversionInfo info, ConvertType type) {
        int length = info.getConvertedFileList().length;
        String[] sliderURLs = new String[length];
        Scene[] scenes = new Scene[length];
        for (int i = 0; i < length; i++) {
            PptPage pptPage = info.getConvertedFileList()[i];
            sliderURLs[i] = pptPage.getSrc();
            scenes[i] = new Scene(String.valueOf(i + 1), pptPage);
        }

        ConvertedFiles files = new ConvertedFiles();
        files.setTaskId(taskUuid);
        files.setType(type);
        files.setSlideURLs(sliderURLs);
        files.setScenes(scenes);

        return files;
    }

    public static class Builder {
        private String resource;
        private ConvertType type;
        private boolean preview = false;
        private double scale = 1.2;
        private OutputFormat outputFormat;
        private boolean pack = false;
        private Region region;
        private String sdkToken;
        private String taskUuid;
        private String taskToken;
        private long interval;
        private long timeout;
        private ConverterCallbacks callback;

        /**
         * @param resource 转换任务源文件 url
         * @return
         */
        public Builder setResource(String resource) {
            this.resource = resource;
            return this;
        }

        /**
         * @param type 转换任务类型，枚举：dynamic, static
         * @return
         */
        public Builder setType(ConvertType type) {
            this.type = type;
            return this;
        }

        /**
         * 只有动态文档转换支持预览图功能，同时生成预览图需要消耗较长时间，请根据业务需要选择
         *
         * @param preview 是否需要生成预览图，默认为 false
         * @return
         */
        public Builder setPreview(boolean preview) {
            this.preview = preview;
            return this;
        }

        /**
         * 只有静态文档转换支持缩放功能
         *
         * @param scale 图片缩放比例，取值 0.1 到 3 之间的范围，默认为 1.2
         * @return
         */
        public Builder setScale(double scale) {
            this.scale = scale;
            return this;
        }

        /**
         * 只有静态文档转换支持自定义输出格式
         * @param outputFormat 输出图片格式，默认为 png，可选参数为 png/jpg/jpeg/webp
         * @return
         */
        public Builder setOutputFormat(OutputFormat outputFormat) {
            this.outputFormat = outputFormat;
            return this;
        }

        /**
         * 只有静态文档转换支持 pack 参数
         * @param pack 是否要生成资源包
         * @return
         */
        public Builder setPack(boolean pack) {
            this.pack = pack;
            return this;
        }

        /**
         * @param region 数据中心 ID（不填则为 cn-hz）
         * @return
         */
        public Builder setRegion(Region region) {
            this.region = region;
            return this;
        }

        /**
         * @note 由于 sdktoken 的权限过大，我们不建议将 sdktoken 暴露到前端，建议使用 sdktoken 签出 tasktoken，将 tasktoken 传到前端使用，只有拥有 tasktoken 的用户才能查询对应的任务进度。
         * @param sdkToken 用于发起文档转换任务并得到 taskuuid
         * @return
         */
        public Builder setSdkToken(String sdkToken) {
            this.sdkToken = sdkToken;
            return this;
        }

        /**
         * @param taskUuid 任务唯一标识
         * @return
         */
        public Builder setTaskUuid(String taskUuid) {
            this.taskUuid = taskUuid;
            return this;
        }

        /**
         * @param taskToken 任务查询token
         * @return
         */
        public Builder setTaskToken(String taskToken) {
            this.taskToken = taskToken;
            return this;
        }

        public Builder setPoolInterval(long interval) {
            this.interval = interval;
            return this;
        }

        public Builder setTimeout(long timeout) {
            this.timeout = timeout;
            return this;
        }

        public Builder setCallback(ConverterCallbacks callback) {
            this.callback = callback;
            return this;
        }

        public ConverterV5 build() {
            if (resource == null) {
                throw new RuntimeException("resource should not be null");
            }
            if (type == null) {
                throw new RuntimeException("type should not be null");
            }

            if (region == null) {
                region = Region.cn;
            }

            if (sdkToken == null) {
                if (taskToken == null || taskUuid == null) {
                    throw new RuntimeException("taskToken and taskUuid should not be null");
                }
            }

            if (outputFormat == null) {
                outputFormat = OutputFormat.PNG;
            }

            if (timeout == 0) {
                timeout = 3 * 60 * 1000;
            }

            if (interval == 0) {
                interval = 15 * 1000;
            }
            return new ConverterV5(resource, type, preview, scale, outputFormat, pack, region, sdkToken, taskUuid, taskToken, interval, timeout, callback);
        }
    }

    class QueryInfo {
        String uuid;
        ConvertType type;
        ConversionInfo.ServerConversionStatus status;
        String failedReason;
        ConversionInfo progress;
    }
}
