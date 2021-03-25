package com.herewhite.sdk.domain;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.junit.Test;

import static com.herewhite.sdk.CommonTestTools.compareJson;
import static org.junit.Assert.assertTrue;

public class SceneTest {
    @Test
    public void serialization() {
        Gson gson = new GsonBuilder()
                .serializeNulls()
                .create();


        String old = "[{\"componentsCount\":null,\"ppt\":{\"height\":500,\"width\":500,\"src\":\"https://white-pan.oss-cn-shanghai.aliyuncs.com/101/image/alin-rusu-1239275-unsplash_opt.jpg\"},\"name\":\"page1\"},{\"ppt\":{\"height\":600,\"width\":600,\"src\":\"https://white-pan.oss-cn-shanghai.aliyuncs.com/101/image/alin-rusu-1239275-unsplash_opt.jpg\"},\"componentsCount\":null,\"name\":\"page2\"}]";
        Scene[] s = new Scene[]{
                new Scene("page1", new PptPage("https://white-pan.oss-cn-shanghai.aliyuncs.com/101/image/alin-rusu-1239275-unsplash_opt.jpg", 500d, 500d)),
                new Scene("page2", new PptPage("https://white-pan.oss-cn-shanghai.aliyuncs.com/101/image/alin-rusu-1239275-unsplash_opt.jpg", 600d, 600d))
        };

        assertTrue(compareJson(old, gson.toJson(s)));
    }
}