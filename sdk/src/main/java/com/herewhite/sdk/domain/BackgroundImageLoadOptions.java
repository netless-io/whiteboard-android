package com.herewhite.sdk.domain;

public class BackgroundImageLoadOptions extends WhiteObject {
    private int maxRetries = 3;
    private int timeoutMs = 15000;
    private int retryIntervalMs = 1000;

    public int getMaxRetries() { return maxRetries; }
    public void setMaxRetries(int value) {
        if (value < -1 || value > 10) throw new IllegalArgumentException("maxRetries must be -1 or from 0 to 10");
        maxRetries = value;
    }
    public int getTimeoutMs() { return timeoutMs; }
    public void setTimeoutMs(int value) {
        if (value < 1000 || value > 120000) throw new IllegalArgumentException("timeoutMs must be from 1000 to 120000");
        timeoutMs = value;
    }
    public int getRetryIntervalMs() { return retryIntervalMs; }
    public void setRetryIntervalMs(int value) {
        if (value < 0 || value > 30000) throw new IllegalArgumentException("retryIntervalMs must be from 0 to 30000");
        retryIntervalMs = value;
    }
}
