package com.herewhite.demo;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.herewhite.demo.common.DemoAPI;
import com.herewhite.demo.common.PostLogger;
import com.herewhite.sdk.WhiteboardView;
import com.tencent.smtt.sdk.QbSdk;

public class MainApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        DemoAPI.get().init(getApplicationContext());
        WhiteboardView.setWebContentsDebuggingEnabled(true);

        QbSdk.setDownloadWithoutWifi(true);
        QbSdk.initX5Environment(getApplicationContext(), new QbSdk.PreInitCallback() {
            @Override
            public void onCoreInitFinished() {
                // 内核初始化完成，可能为系统内核，也可能为系统内核
            }

            /**
             * 预初始化结束
             * 由于X5内核体积较大，需要依赖网络动态下发，所以当内核不存在的时候，默认会回调false，此时将会使用系统内核代替
             * @param isX5 是否使用X5内核
             */
            @Override
            public void onViewInitFinished(boolean isX5) {

            }
        });

        this.registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
                PostLogger.log("onActivityCreated: " + activity.getClass().getSimpleName());
            }

            @Override
            public void onActivityStarted(@NonNull Activity activity) {
                PostLogger.log("onActivityStarted: " + activity.getClass().getSimpleName());
            }

            @Override
            public void onActivityResumed(@NonNull Activity activity) {
                PostLogger.log("onActivityResumed: " + activity.getClass().getSimpleName());
            }

            @Override
            public void onActivityPaused(@NonNull Activity activity) {
                PostLogger.log("onActivityPaused: " + activity.getClass().getSimpleName());
            }

            @Override
            public void onActivityStopped(@NonNull Activity activity) {
                PostLogger.log("onActivityStopped: " + activity.getClass().getSimpleName());
            }

            @Override
            public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {
                PostLogger.log("onActivitySaveInstanceState: " + activity.getClass().getSimpleName());
            }

            @Override
            public void onActivityDestroyed(@NonNull Activity activity) {
                PostLogger.log("onActivityDestroyed: " + activity.getClass().getSimpleName());
            }
        });
    }
}
