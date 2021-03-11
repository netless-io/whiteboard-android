package com.herewhite.sdk.internal;

// TODO 是否处理批量的异常模版
public class JsCallWrapper {
    private final Runnable runnable;
    private final String message;

    public JsCallWrapper(Runnable runnable, String message) {
        this.runnable = runnable;
        this.message = message;
    }

    public void run() {
        try {
            runnable.run();
        } catch (AssertionError a) {
            throw a;
        } catch (Throwable e) {
            Logger.error(message, e);
        }
    }
}
