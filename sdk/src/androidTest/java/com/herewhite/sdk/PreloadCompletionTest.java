package com.herewhite.sdk;

import androidx.test.platform.app.InstrumentationRegistry;
import com.herewhite.sdk.domain.WhitePreloadResult;
import org.junit.Test;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.Assert.*;

/** Actual WhiteSdk and Handler dispatch, with synthetic Bridge completion payloads. */
public class PreloadCompletionTest {
    void main(Runnable work) { InstrumentationRegistry.getInstrumentation().runOnMainSync(work); }
    private WhiteSdkConfiguration enabled() {
        WhiteSdkConfiguration config = new WhiteSdkConfiguration("test-app");
        config.setEnableAppliancePlugin(true);
        config.setEnablePreLoadAppliancePlugin(true);
        return config;
    }
    @Test public void ordinaryConfigurationDoesNotObserveOrPostPreload() throws Exception {
        LifecycleHostTest.Raw raw = new LifecycleHostTest.Raw();
        main(() -> new WhiteSdk(raw, InstrumentationRegistry.getInstrumentation().getTargetContext(), new WhiteSdkConfiguration("test-app")));
        main(() -> assertEquals(0, raw.observations));
    }
    @Test public void buffersForLateListenerDeliversOnceAndUsesUiThread() throws Exception {
        LifecycleHostTest.Raw raw = new LifecycleHostTest.Raw();raw.holdPreload=true;
        WhiteSdk[] sdk = new WhiteSdk[1];AtomicInteger calls=new AtomicInteger();CountDownLatch delivered=new CountDownLatch(1);
        main(()->sdk[0]=new WhiteSdk(raw,InstrumentationRegistry.getInstrumentation().getTargetContext(),enabled()));
        main(()->{assertEquals(1,raw.observations);raw.completePreload("{\"state\":\"warn\",\"message\":\"Slide failed\"}");});
        main(()->sdk[0].setCommonCallbacks(new CommonCallback() {
            public void onPreloadStateChange(WhitePreloadResult result) {
                assertEquals(android.os.Looper.getMainLooper(),android.os.Looper.myLooper());
                assertEquals("warn",result.getState());assertEquals("Slide failed",result.getMessage());calls.incrementAndGet();delivered.countDown();
            }
        }));
        assertTrue(delivered.await(2,TimeUnit.SECONDS));
        main(()->sdk[0].setCommonCallbacks(new CommonCallback(){public void onPreloadStateChange(WhitePreloadResult result){calls.incrementAndGet();}}));
        main(()->assertEquals(1,calls.get()));
    }
    @Test public void cancellationDropsLateOptionalResultWithoutCancelledEvent() throws Exception {
        LifecycleHostTest.Raw raw=new LifecycleHostTest.Raw();raw.holdPreload=true;
        WhiteSdk[] sdk=new WhiteSdk[1];AtomicInteger calls=new AtomicInteger();
        main(()->sdk[0]=new WhiteSdk(raw,InstrumentationRegistry.getInstrumentation().getTargetContext(),enabled(),new CommonCallback(){public void onPreloadStateChange(WhitePreloadResult result){calls.incrementAndGet();}}));
        main(()->sdk[0].cancelPreparation());
        main(()->raw.completePreload("{\"state\":\"success\",\"message\":\"\"}"));
        main(()->assertEquals(0,calls.get()));
    }
}
