package com.herewhite.sdk.converter;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.herewhite.sdk.domain.ConvertErrorCode;
import com.herewhite.sdk.domain.ConvertException;
import com.herewhite.sdk.domain.ConverterStatus;
import com.herewhite.sdk.domain.Region;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Class for query convert task by Projector
 */
public class ProjectorQuery {
    private final static String QUERY_URL_FORMAT = "https://api.netless.link/v5/projector/tasks/%s";
    static ThreadPoolExecutor executorService;

    static {
        executorService = new ThreadPoolExecutor(4,
                4,
                10L,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(),
                new ProjectorThreadFactory());
        executorService.allowCoreThreadTimeOut(true);
    }

    private final Region region;
    private final String taskUuid;
    private final String taskToken;
    private final long interval;
    private final long timeout;
    private final Callback outCallback;
    private final Gson gson = new Gson();
    private final OkHttpClient client = new OkHttpClient();
    private ConverterStatus status;
    private long startTime;

    public ProjectorQuery(Region region,
                          String taskUuid,
                          String taskToken,
                          long interval,
                          long timeout,
                          Callback callback) {
        this.region = region;
        this.taskUuid = taskUuid;
        this.taskToken = taskToken;
        this.interval = interval;
        this.timeout = timeout;
        this.outCallback = callback;
    }

    public void startQuery() {
        if (startTime != 0 && isNotFinish()) {
            return;
        }
        startTime = System.currentTimeMillis();
        executorService.execute(this::startProgressLoop);
    }

    private boolean isNotFinish() {
        return !(status == ConverterStatus.Success || status == ConverterStatus.Fail);
    }

    private void startProgressLoop() {
        long timeLimit = startTime + timeout;
        try {
            status = ConverterStatus.Checking;
            while (System.currentTimeMillis() < timeLimit) {
                checkProgress();
                if (status != ConverterStatus.Checking) {
                    return;
                }
                Thread.sleep(interval);
            }
            onFailure(new ConvertException(ConvertErrorCode.CheckTimeout));
            status = ConverterStatus.Timeout;
        } catch (ConvertException e) {
            onFailure(e);
            status = ConverterStatus.Fail;
        } catch (InterruptedException ignored) {
            onFailure(new ConvertException(ConvertErrorCode.ConvertFail));
            status = ConverterStatus.Fail;
        }
    }

    private void checkProgress() throws ConvertException {
        Request request = new Request.Builder()
                .url(String.format(QUERY_URL_FORMAT, taskUuid))
                .header("token", taskToken)
                .header("region", convertRegion(region))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .build();
        Call call = client.newCall(request);

        try {
            Response response = call.execute();
            String body = response.body().string();
            System.out.println("response is " + body);
            if (response.code() == 200) {
                QueryResponse queryResponse = gson.fromJson(body, QueryResponse.class);
                ConversionStatus status = queryResponse.status;
                if (status == ConversionStatus.Fail) {
                    onFailure(new ConvertException(ConvertErrorCode.ConvertFail, queryResponse.errorMessage));
                    this.status = ConverterStatus.Fail;
                } else if (status == ConversionStatus.Finished) {
                    onFinish(queryResponse);
                    this.status = ConverterStatus.Success;
                } else {
                    onProgress(queryResponse.convertedPercentage, queryResponse);
                }
            } else {
                throw new ConvertException(ConvertErrorCode.ConvertFail, body);
            }
        } catch (IOException e) {
            throw new ConvertException(ConvertErrorCode.CheckFail, e);
        }
    }

    private String convertType(ConvertType type) {
        if (type == ConvertType.Dynamic) {
            return "dynamic";
        } else {
            return "static";
        }
    }

    private String convertRegion(Region region) {
        JsonElement regionElement = gson.toJsonTree(region);
        return regionElement.getAsString();
    }

    private void onFinish(QueryResponse response) {
        if (outCallback != null) {
            outCallback.onFinish(response);
        }
    }

    private void onFailure(ConvertException e) {
        if (outCallback != null) {
            outCallback.onFailure(e);
        }
    }

    private void onProgress(double convertedPercentage, QueryResponse response) {
        if (outCallback != null) {
            outCallback.onProgress(convertedPercentage, response);
        }
    }

    public enum ConversionStatus {
        Waiting,
        Converting,
        Finished,
        Fail,
        Abort,
    }

    public static class Image {
        public int width;
        public int height;
        public String url;
    }

    public interface Callback {
        void onProgress(double progress, QueryResponse convertInfo);

        void onFinish(QueryResponse response);

        void onFailure(ConvertException e);
    }

    private static final class ProjectorThreadFactory implements ThreadFactory {
        @Override
        public synchronized Thread newThread(Runnable runnable) {
            Thread result = new Thread(runnable, "white-sdk-converter");
            result.setPriority(Thread.MIN_PRIORITY);
            return result;
        }
    }

    public static class Builder {
        private Region region;
        private String taskUuid;
        private String taskToken;
        private long interval;
        private long timeout;
        private Callback callback;

        /**
         * @param region 数据中心 ID（不填则为 cn-hz）
         */
        public ProjectorQuery.Builder setRegion(Region region) {
            this.region = region;
            return this;
        }

        /**
         * @param taskUuid 任务唯一标识
         */
        public ProjectorQuery.Builder setTaskUuid(String taskUuid) {
            this.taskUuid = taskUuid;
            return this;
        }

        /**
         * @param taskToken 任务查询token
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

        public Builder setCallback(Callback callback) {
            this.callback = callback;
            return this;
        }

        public ProjectorQuery build() {
            if (taskUuid == null || taskToken == null) {
                throw new RuntimeException("taskUuid and taskToken should not be null");
            }

            if (region == null) {
                region = Region.cn;
            }

            if (timeout == 0) {
                timeout = 30 * 1000;
            }

            if (interval == 0) {
                interval = 2 * 1000;
            }

            return new ProjectorQuery(region, taskUuid, taskToken, interval, timeout, callback);
        }
    }

    public static class QueryResponse {
        private String uuid;
        private ConversionStatus status;
        private String errorCode;
        private String errorMessage;
        private double convertedPercentage;
        private String prefix;

        private String type;

        private Integer pageCount;

        private HashMap<String, Image> previews;

        private String note;

        private HashMap<String, Image> images;

        public String getUuid() {
            return uuid;
        }

        public void setUuid(String uuid) {
            this.uuid = uuid;
        }

        public ConversionStatus getStatus() {
            return status;
        }

        public void setStatus(ConversionStatus status) {
            this.status = status;
        }

        public String getErrorCode() {
            return errorCode;
        }

        public void setErrorCode(String errorCode) {
            this.errorCode = errorCode;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
        }

        public double getConvertedPercentage() {
            return convertedPercentage;
        }

        public void setConvertedPercentage(int convertedPercentage) {
            this.convertedPercentage = convertedPercentage;
        }

        public String getPrefix() {
            return prefix;
        }

        public void setPrefix(String prefix) {
            this.prefix = prefix;
        }

        public void setConvertedPercentage(double convertedPercentage) {
            this.convertedPercentage = convertedPercentage;
        }

        public Integer getPageCount() {
            return pageCount;
        }

        public void setPageCount(Integer pageCount) {
            this.pageCount = pageCount;
        }

        public String getNote() {
            return note;
        }

        public void setNote(String note) {
            this.note = note;
        }

        public HashMap<String, Image> getPreviews() {
            return previews;
        }

        public void setPreviews(HashMap<String, Image> previews) {
            this.previews = previews;
        }

        public HashMap<String, Image> getImages() {
            return images;
        }

        public void setImages(HashMap<String, Image> images) {
            this.images = images;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }
    }
}
