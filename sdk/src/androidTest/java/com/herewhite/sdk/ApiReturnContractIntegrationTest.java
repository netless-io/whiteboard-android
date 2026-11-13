package com.herewhite.sdk;

import androidx.test.platform.app.InstrumentationRegistry;
import com.herewhite.sdk.domain.Promise;
import com.herewhite.sdk.domain.SDKError;
import org.junit.Test;
import org.json.JSONTokener;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import static org.junit.Assert.*;

/** Real default bundled WebView with public APIs; manager failures are controlled injection. */
public class ApiReturnContractIntegrationTest extends WhiteSdkIntegrationTestHost {
    private <T> T call(Consumer<Promise<T>> action, String expectedError) throws Exception {
        CountDownLatch done=new CountDownLatch(1);AtomicReference<T> value=new AtomicReference<>();
        AtomicReference<SDKError> error=new AtomicReference<>();AtomicInteger count=new AtomicInteger();
        InstrumentationRegistry.getInstrumentation().runOnMainSync(()->action.accept(new Promise<T>() {
            public void then(T result){value.set(result);count.incrementAndGet();done.countDown();}
            public void catchEx(SDKError result){error.set(result);count.incrementAndGet();done.countDown();}
        }));
        assertTrue("API callback timeout",done.await(10,TimeUnit.SECONDS));assertEquals(1,count.get());
        if(expectedError==null)assertNull(error.get());
        else {assertNotNull(error.get());assertTrue(error.get().toString(),error.get().getMessage().contains(expectedError));}
        return value.get();
    }
    private void js(String source) throws Exception {
        CountDownLatch done=new CountDownLatch(1);AtomicReference<String> result=new AtomicReference<>();
        InstrumentationRegistry.getInstrumentation().runOnMainSync(()->whiteboardView.evaluateJavascript(source,r->{result.set(r);done.countDown();}));
        assertTrue(done.await(10,TimeUnit.SECONDS));assertEquals(Boolean.TRUE,new JSONTokener(result.get()).nextValue());
    }
    @Test public void singleWindowContract() throws Throwable {
        Room room=joinRoom(null,false);
        for(int i=0;i<10;i++) {
            assertNotNull(this.<com.herewhite.sdk.domain.RoomState>call(room::getRoomState,null));assertNotNull(call(room::getEntireScenes,null));
            this.<java.util.Map<String, com.herewhite.sdk.domain.WindowAppSyncAttrs>>call(p->room.queryAllApps(p),"window manager not existed");
            this.<com.herewhite.sdk.domain.WindowAppSyncAttrs>call(p->room.queryApp("missing",p),"window manager not existed");
            this.<Boolean>call(p->room.closeApp("missing",p),"window manager not existed");
        }
    }
    @Test public void multiWindowErrorsAndValues() throws Throwable {
        Room room=joinRoom(null,true);
        for(int i=0;i<10;i++) {
            assertNotNull(this.<com.herewhite.sdk.domain.RoomState>call(room::getRoomState,null));assertNotNull(call(room::getEntireScenes,null));
            assertNotNull(call(room::queryAllApps,null));
            js("window.__contractNext=window.manager.nextPage;window.manager.nextPage=function(){return Promise.resolve(false)};true");
            assertEquals(Boolean.FALSE,call(room::nextPage,null));
            js("window.manager.nextPage=function(){return Promise.reject(new Error('contract page failure'))};true");
            call(room::nextPage,"contract page failure");
            js("window.manager.nextPage=window.__contractNext;delete window.__contractNext;true");
            js("window.__contractClose=window.manager.closeApp;window.manager.closeApp=function(){return Promise.resolve()};true");
            assertEquals(Boolean.TRUE,this.<Boolean>call(p->room.closeApp("missing",p),null));
            js("window.manager.closeApp=function(){return Promise.reject(new Error('contract close failure'))};true");
            this.<Boolean>call(p->room.closeApp("missing",p),"contract close failure");
            js("window.manager.closeApp=window.__contractClose;delete window.__contractClose;true");
        }
    }
}
