package com.herewhite.sdk.domain;

/**
 * Created by buhe on 2018/8/18.
 */

public class SDKError extends Exception {

    private final String jsStack;

    public SDKError(String message) {
        super(message);
        jsStack = "Native Error";
    }

    public SDKError(String message, String jsStack) {
        super(message);
        this.jsStack = jsStack;
    }

    @Override
    public String toString() {
        return super.toString() + " jsStack: " + getJsStack();
    }

    public String getJsStack() {
        return jsStack;
    }
}
