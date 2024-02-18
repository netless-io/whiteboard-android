package com.herewhite.demo.test.window;

import android.view.View;

import com.herewhite.demo.R;
import com.herewhite.demo.common.SampleBaseActivity;
import com.herewhite.demo.databinding.ActivityWindowAppsBinding;
import com.herewhite.sdk.WhiteSdkConfiguration;
import com.herewhite.sdk.domain.Promise;
import com.herewhite.sdk.domain.SDKError;
import com.herewhite.sdk.domain.Scene;
import com.herewhite.sdk.domain.WindowAppParam;
import com.herewhite.sdk.domain.WindowAppSyncAttrs;

import java.util.Map;

public class WindowAppsActivity extends SampleBaseActivity {

    private ActivityWindowAppsBinding binding;
    private String[] appIds;

    private Promise<String> insertPromise = new Promise<String>() {
        @Override
        public void then(String appId) {
            showLogDisplay("insert app success, appId: " + appId);
        }

        @Override
        public void catchEx(SDKError t) {

        }
    };

    @Override
    protected View getContentView() {
        binding = ActivityWindowAppsBinding.inflate(getLayoutInflater());
        return binding.getRoot();
    }

    @Override
    protected void initView() {
        // 插入播放器
        binding.insertPlayer.setOnClickListener(v -> {
            WindowAppParam appParam = WindowAppParam.createMediaPlayerApp("https://white-pan.oss-cn-shanghai.aliyuncs.com/101/oceans.mp4", "player");
            room.addApp(appParam, insertPromise);
        });

        // 插入静态场景
        binding.insertStatic.setOnClickListener(v -> {
            // 由转换后信息序列化
            String ppts = "[{\"name\":\"1\",\"ppt\":{\"height\":1010.0,\"src\":\"https://convertcdn.netless.link/staticConvert/0764816000c411ecbfbbb9230f6dd80f/1.png\",\"width\":714.0}},{\"name\":\"2\",\"ppt\":{\"height\":1010.0,\"src\":\"https://convertcdn.netless.link/staticConvert/0764816000c411ecbfbbb9230f6dd80f/2.png\",\"width\":714.0}},{\"name\":\"3\",\"ppt\":{\"height\":1010.0,\"src\":\"https://convertcdn.netless.link/staticConvert/0764816000c411ecbfbbb9230f6dd80f/3.png\",\"width\":714.0}},{\"name\":\"4\",\"ppt\":{\"height\":1010.0,\"src\":\"https://convertcdn.netless.link/staticConvert/0764816000c411ecbfbbb9230f6dd80f/4.png\",\"width\":714.0}}]";
            Scene[] scenes = gson.fromJson(ppts, Scene[].class);

            WindowAppParam param = WindowAppParam.createDocsViewerApp("/static", scenes, "static");
            room.addApp(param, insertPromise);
        });

        // 插入动态场景
        binding.insertDynamic.setOnClickListener(v -> {
            // 由转换后信息序列化
            String ppts = " [{\"name\":\"1\",\"ppt\":{\"src\":\"pptx://convertcdn.netless.link/dynamicConvert/369ac28037d011ec99f08bddeae74404/1.slide\",\"width\":1280,\"height\":720,\"previewURL\":\"https://convertcdn.netless.link/dynamicConvert/369ac28037d011ec99f08bddeae74404/preview/1.png\"}},{\"name\":\"2\",\"ppt\":{\"src\":\"pptx://convertcdn.netless.link/dynamicConvert/369ac28037d011ec99f08bddeae74404/2.slide\",\"width\":1280,\"height\":720,\"previewURL\":\"https://convertcdn.netless.link/dynamicConvert/369ac28037d011ec99f08bddeae74404/preview/2.png\"}},{\"name\":\"3\",\"ppt\":{\"src\":\"pptx://convertcdn.netless.link/dynamicConvert/369ac28037d011ec99f08bddeae74404/3.slide\",\"width\":1280,\"height\":720,\"previewURL\":\"https://convertcdn.netless.link/dynamicConvert/369ac28037d011ec99f08bddeae74404/preview/3.png\"}},{\"name\":\"4\",\"ppt\":{\"src\":\"pptx://convertcdn.netless.link/dynamicConvert/369ac28037d011ec99f08bddeae74404/4.slide\",\"width\":1280,\"height\":720,\"previewURL\":\"https://convertcdn.netless.link/dynamicConvert/369ac28037d011ec99f08bddeae74404/preview/4.png\"}},{\"name\":\"5\",\"ppt\":{\"src\":\"pptx://convertcdn.netless.link/dynamicConvert/369ac28037d011ec99f08bddeae74404/5.slide\",\"width\":1280,\"height\":720,\"previewURL\":\"https://convertcdn.netless.link/dynamicConvert/369ac28037d011ec99f08bddeae74404/preview/5.png\"}},{\"name\":\"6\",\"ppt\":{\"src\":\"pptx://convertcdn.netless.link/dynamicConvert/369ac28037d011ec99f08bddeae74404/6.slide\",\"width\":1280,\"height\":720,\"previewURL\":\"https://convertcdn.netless.link/dynamicConvert/369ac28037d011ec99f08bddeae74404/preview/6.png\"}},{\"name\":\"7\",\"ppt\":{\"src\":\"pptx://convertcdn.netless.link/dynamicConvert/369ac28037d011ec99f08bddeae74404/7.slide\",\"width\":1280,\"height\":720,\"previewURL\":\"https://convertcdn.netless.link/dynamicConvert/369ac28037d011ec99f08bddeae74404/preview/7.png\"}},{\"name\":\"8\",\"ppt\":{\"src\":\"pptx://convertcdn.netless.link/dynamicConvert/369ac28037d011ec99f08bddeae74404/8.slide\",\"width\":1280,\"height\":720,\"previewURL\":\"https://convertcdn.netless.link/dynamicConvert/369ac28037d011ec99f08bddeae74404/preview/8.png\"}},{\"name\":\"9\",\"ppt\":{\"src\":\"pptx://convertcdn.netless.link/dynamicConvert/369ac28037d011ec99f08bddeae74404/9.slide\",\"width\":1280,\"height\":720,\"previewURL\":\"https://convertcdn.netless.link/dynamicConvert/369ac28037d011ec99f08bddeae74404/preview/9.png\"}},{\"name\":\"10\",\"ppt\":{\"src\":\"pptx://convertcdn.netless.link/dynamicConvert/369ac28037d011ec99f08bddeae74404/10.slide\",\"width\":1280,\"height\":720,\"previewURL\":\"https://convertcdn.netless.link/dynamicConvert/369ac28037d011ec99f08bddeae74404/preview/10.png\"}},{\"name\":\"11\",\"ppt\":{\"src\":\"pptx://convertcdn.netless.link/dynamicConvert/369ac28037d011ec99f08bddeae74404/11.slide\",\"width\":1280,\"height\":720,\"previewURL\":\"https://convertcdn.netless.link/dynamicConvert/369ac28037d011ec99f08bddeae74404/preview/11.png\"}},{\"name\":\"12\",\"ppt\":{\"src\":\"pptx://convertcdn.netless.link/dynamicConvert/369ac28037d011ec99f08bddeae74404/12.slide\",\"width\":1280,\"height\":720,\"previewURL\":\"https://convertcdn.netless.link/dynamicConvert/369ac28037d011ec99f08bddeae74404/preview/12.png\"}},{\"name\":\"13\",\"ppt\":{\"src\":\"pptx://convertcdn.netless.link/dynamicConvert/369ac28037d011ec99f08bddeae74404/13.slide\",\"width\":1280,\"height\":720,\"previewURL\":\"https://convertcdn.netless.link/dynamicConvert/369ac28037d011ec99f08bddeae74404/preview/13.png\"}},{\"name\":\"14\",\"ppt\":{\"src\":\"pptx://convertcdn.netless.link/dynamicConvert/369ac28037d011ec99f08bddeae74404/14.slide\",\"width\":1280,\"height\":720,\"previewURL\":\"https://convertcdn.netless.link/dynamicConvert/369ac28037d011ec99f08bddeae74404/preview/14.png\"}}]";
            Scene[] scenes = gson.fromJson(ppts, Scene[].class);

            WindowAppParam param = WindowAppParam.createSlideApp("/dynamic003", scenes, "dynamic");
            room.addApp(param, insertPromise);
        });

        // 插入新的动态PPT
        binding.insertNewDynamic.setOnClickListener(v -> {
            // prefixUrl
            String prefixUrl = "https://conversion-demo-cn.oss-cn-hangzhou.aliyuncs.com/demo/dynamicConvert";
            String taskUuid = "3e3a2b8845194f998e6e05adab70e1a1";
            WindowAppParam param = WindowAppParam.createSlideApp(taskUuid, prefixUrl, "Projector App");
            room.addApp(param, insertPromise);
        });

        binding.queryApps.setOnClickListener(v -> {
            room.queryAllApps(new Promise<Map<String, WindowAppSyncAttrs>>() {
                @Override
                public void then(Map<String, WindowAppSyncAttrs> apps) {
                    appIds = apps.keySet().toArray(new String[0]);
                    showLogDisplay("queryApps " + String.join(",", appIds));
                }

                @Override
                public void catchEx(SDKError t) {
                }
            });
        });

        binding.closeApp.setOnClickListener(v -> {
            room.closeApp(appIds[0], new Promise<Boolean>() {
                @Override
                public void then(Boolean aBoolean) {

                }

                @Override
                public void catchEx(SDKError t) {

                }
            });
        });

        binding.focusApp.setOnClickListener(v -> {
            room.focusApp(appIds[0]);
        });

        findViewById(R.id.queryApp).setOnClickListener(v -> {
            room.queryApp(appIds[0], new Promise<WindowAppSyncAttrs>() {
                @Override
                public void then(WindowAppSyncAttrs attrs) {
                    showLogDisplay("queryApp " + attrs);
                }

                @Override
                public void catchEx(SDKError t) {

                }
            });

            // 查询不存在的 appId 触发 catchEx
            room.queryApp("not_exited_appId", new Promise<WindowAppSyncAttrs>() {
                @Override
                public void then(WindowAppSyncAttrs attrs) {
                }

                @Override
                public void catchEx(SDKError t) {
                    showLogDisplay("query not_exited_appId result" + t.getMessage());
                }
            });
        });
    }

    protected WhiteSdkConfiguration generateSdkConfig() {
        WhiteSdkConfiguration configuration = new WhiteSdkConfiguration(demoAPI.getAppId(), true);
        // 开启多窗口支持
        configuration.setUseMultiViews(true);
        return configuration;
    }

    @Override
    protected void onJoinRoomSuccess() {

    }
}
