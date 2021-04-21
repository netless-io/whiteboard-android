package com.herewhite.demo.test;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.herewhite.demo.R;
import com.herewhite.sdk.ConverterCallbacks;
import com.herewhite.sdk.converter.ConvertType;
import com.herewhite.sdk.converter.ConverterV5;
import com.herewhite.sdk.domain.ConversionInfo;
import com.herewhite.sdk.domain.ConvertException;
import com.herewhite.sdk.domain.ConvertedFiles;

import java.util.Random;
import java.util.concurrent.CountDownLatch;

public class ConvertV5Activity extends AppCompatActivity {
    private TextView display;
    private Button start;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_convert_v5);

        display = findViewById(R.id.textDisplay);
        start = findViewById(R.id.startButton);

        start.setOnClickListener(v -> {
            new Thread(() -> {
                int testCount = 10;
                String[] resources = new String[]{
                        "https://white-cn-edge-doc-convert.oss-cn-hangzhou.aliyuncs.com/-1/1.pptx",
                        "https://beings.oss-cn-hangzhou.aliyuncs.com/test/cb839dc0a17811ebb08929f76725f043.pptx"
                };
                TestCallback[] callbacks = new TestCallback[testCount];
                CountDownLatch latch = new CountDownLatch(testCount);
                for (int i = 0; i < testCount; i++) {
                    callbacks[i] = new TestCallback(latch);
                    ConverterV5.Builder builder = new ConverterV5.Builder();
                    ConverterV5 converter = builder
                            .setResource(resources[i % resources.length])
                            .setType(randomInt(1) == 1 ? ConvertType.Static : ConvertType.Dynamic)
                            .setPreview(randomInt(1) == 1)
                            .setOutputFormat(randomInt(1) == 1 ? ConverterV5.OutputFormat.PNG : ConverterV5.OutputFormat.JPEG)
                            .setTimeout(60_000L)
                            .setPoolInterval(2000)
                            .setSdkToken("WHITEcGFydG5lcl9pZD1OZ3pwQWNBdlhiemJERW9NY0E0Z0V3RTUwbVZxM0NIbDJYV0Ymc2lnPWNiZWExOTMwNzc1NmQyNmU3N2U3M2Q0NWZjNTZiOGIwMWE2ZjU4NDI6YWRtaW5JZD0yMTYmcm9sZT1hZG1pbiZleHBpcmVfdGltZT0xNTg5ODMzNTQxJmFrPU5nenBBY0F2WGJ6YkRFb01jQTRnRXdFNTBtVnEzQ0hsMlhXRiZjcmVhdGVfdGltZT0xNTU4Mjc2NTg5Jm5vbmNlPTE1NTgyNzY1ODg4NDQwMA")
                            .setCallback(callbacks[i])
                            .build();
                    converter.startConvertTask();
                }
                try {
                    latch.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                boolean allSuccess = true;
                for (TestCallback callback : callbacks) {
                    allSuccess &= (callback.countFailure + callback.countFinish == 1);
                }
                boolean finalAllSuccess = allSuccess;
                runOnUiThread(() -> display.append("\nTest Result" + finalAllSuccess));
            }).start();
        });
    }

    ConverterCallbacks callbacks = new ConverterCallbacks() {
        @Override
        public void onFailure(ConvertException e) {
            logAction(e.getMessage());
        }

        @Override
        public void onFinish(ConvertedFiles ppt, ConversionInfo convertInfo) {
            logAction(convertInfo.toString());
        }

        @Override
        public void onProgress(Double progress, ConversionInfo convertInfo) {
            logAction(String.valueOf(progress));
        }
    };

    void logAction(String str) {
        Log.i("ConvertV5Activity", Thread.currentThread().getStackTrace()[3].getMethodName() + " " + str);
    }

    class TestCallback implements ConverterCallbacks {
        private final CountDownLatch latch;
        public int countFinish;
        public int countFailure;

        public TestCallback(CountDownLatch latch) {
            this.latch = latch;
        }

        @Override
        public void onProgress(Double progress, ConversionInfo convertInfo) {
            System.out.println(progress);
        }

        @Override
        public void onFinish(ConvertedFiles ppt, ConversionInfo convertInfo) {
            System.out.println("onFinish");
            latch.countDown();
            countFinish++;
        }

        @Override
        public void onFailure(ConvertException e) {
            System.out.println("onFailure" + e.getMessage());
            latch.countDown();
            countFailure++;
        }
    }

    Random r = new Random();

    private int randomInt(int max) {
        return r.nextInt(max);
    }
}