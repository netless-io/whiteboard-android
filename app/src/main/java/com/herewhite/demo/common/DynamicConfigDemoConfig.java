package com.herewhite.demo.common;

import java.util.LinkedHashMap;
import java.util.Map;

/** Platform-specific OSS addresses for the Android dynamic configuration example. */
public final class DynamicConfigDemoConfig {
    private DynamicConfigDemoConfig() {
    }

    public static Map<String, String> getRegionUrls() {
        String path = "/whiteboard-config-service/fastboard-demo/android/latest.json";
        String chinaUrl = "https://fastboard-demo-config.oss-cn-hangzhou.aliyuncs.com" + path;
        String singaporeUrl = "https://fastboard-demo-config-sg.oss-ap-southeast-1.aliyuncs.com" + path;
        Map<String, String> urls = new LinkedHashMap<>();
        urls.put("cn-hz", chinaUrl);
        urls.put("sg", singaporeUrl);
        urls.put("us-sv", singaporeUrl);
        urls.put("eu", singaporeUrl);
        urls.put("in-mum", singaporeUrl);
        return urls;
    }
}
