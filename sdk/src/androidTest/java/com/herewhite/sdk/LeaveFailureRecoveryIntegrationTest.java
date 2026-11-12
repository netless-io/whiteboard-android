package com.herewhite.sdk;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.platform.app.InstrumentationRegistry;
import com.herewhite.sdk.domain.Promise;
import com.herewhite.sdk.domain.SDKError;
import com.herewhite.sdk.domain.WindowParams;
import org.junit.Test;
import org.json.JSONTokener;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import static org.junit.Assert.*;

/** Actual default WebView and server; close failures are injected after real transport cleanup. */
public class LeaveFailureRecoveryIntegrationTest extends WhiteSdkIntegrationTestHost {
    private TestCredentialProvider.RoomCredentials credentials;
    private String uid;
    private boolean multi;
    private void main(Runnable task){InstrumentationRegistry.getInstrumentation().runOnMainSync(task);}
    private WhiteSdkConfiguration configuration(){
        WhiteSdkConfiguration c=new WhiteSdkConfiguration(credentials.appIdentifier,true);c.setUseMultiViews(multi);
        String hosts=InstrumentationRegistry.getArguments().getString("whiteboard.test.apiHosts");
        if(hosts!=null&&!hosts.isEmpty())c.setApiHosts(java.util.Arrays.asList(hosts.split(",")));return c;
    }
    private void initialize(){main(()->{
        whiteboardView=new WhiteboardView(ApplicationProvider.getApplicationContext());
        whiteboardView.getSettings().setAllowUniversalAccessFromFileURLs(true);
        sdk=new WhiteSdk(whiteboardView,ApplicationProvider.getApplicationContext(),configuration());
    });}
    private <T> T call(Consumer<Promise<T>> action,String expected) throws Exception {
        CountDownLatch done=new CountDownLatch(1);AtomicReference<T> value=new AtomicReference<>();AtomicReference<SDKError> error=new AtomicReference<>();
        main(()->action.accept(new Promise<T>(){public void then(T v){value.set(v);done.countDown();}public void catchEx(SDKError e){error.set(e);done.countDown();}}));
        assertTrue("callback timeout",done.await(65,TimeUnit.SECONDS));
        if(expected==null)assertNull(error.get());else {assertNotNull(error.get());assertTrue(error.get().getMessage(),error.get().getMessage().contains(expected));}
        return value.get();
    }
    private void join() throws Exception {
        RoomParams p=new RoomParams(credentials.roomUuid,credentials.roomToken,uid);p.setWindowParams(new WindowParams());
        room=this.<Room>call(reply->sdk.joinRoom(p,null,reply),null);assertNotNull(room);
        js("window.room.phase==='connected'");
    }
    private void js(String code) throws Exception {
        CountDownLatch done=new CountDownLatch(1);AtomicReference<String> value=new AtomicReference<>();
        main(()->whiteboardView.evaluateJavascript(code,r->{value.set(r);done.countDown();}));
        assertTrue(done.await(15,TimeUnit.SECONDS));assertEquals(Boolean.TRUE,new JSONTokener(value.get()).nextValue());
    }
    private void inject() throws Exception {
        js("window.__leaveErrors=[];var r=window.room,d=r.disconnect.bind(r),e=r.logger.error.bind(r.logger);r.logger=Object.create(r.logger,{error:{value:function(m){window.__leaveErrors.push(m);return e(m)}}});r.disconnect=function(){return d().then(function(){throw new Error('device old leave failure')})};true");
    }
    private void verifyLog() throws Exception {js("window.__leaveErrors.filter(function(m){return typeof m==='string'&&m.indexOf('[Bridge] Previous room leave failed: device old leave failure')>=0}).length===1");}
    private void exercise(boolean multiple) throws Exception {
        multi=multiple;credentials=TestCredentialProvider.requireRoomCredentials();uid="android-leave-recovery-"+System.currentTimeMillis();initialize();
        for(int i=0;i<10;i++){
            // A prepared/reused SDK and ordinary initialization use the same public admission path.
            if(i%2==1)this.<Void>call(reply->sdk.prepareCompletion(reply),null);
            join();inject();this.<Object>call(reply->sdk.leaveRoom(reply),"device old leave failure");verifyLog();join();
            inject();Room old=room;this.<Object>call(reply->old.disconnect(reply),"device old leave failure");verifyLog();join();
            inject();join();verifyLog();
            // releaseRoom only drops Native references: the JS Room stays connected until replacement.
            main(()->sdk.releaseRoom());js("window.room.phase==='connected'");
            main(()->sdk=new WhiteSdk(whiteboardView,ApplicationProvider.getApplicationContext(),configuration()));join();
            inject();WhiteboardView oldView=whiteboardView;initialize();join();
            WhiteboardView fresh=whiteboardView;whiteboardView=oldView;verifyLog();js("window.room===undefined");whiteboardView=fresh;
            main(oldView::destroy);join();
            this.<Object>call(reply->sdk.leaveRoom(reply),null);
        }
    }
    @Test public void deadlineAllowsNextJoinAndIgnoresLateClose() throws Exception {
        multi=true;credentials=TestCredentialProvider.requireRoomCredentials();uid="android-leave-timeout-"+System.currentTimeMillis();initialize();join();inject();
        js("(function(r,disconnect){r.disconnect=function(){return disconnect.call(r).catch(function(){return new Promise(function(resolve){window.__finishOldLeave=resolve})})}})(window.room,window.room.disconnect);true");
        join();js("window.__leaveErrors.some(function(m){return m.indexOf('LeaveTimeout')>=0})");
        js("window.__finishOldLeave();true");Thread.sleep(300);js("window.room.phase==='connected'");
        this.<Object>call(reply->sdk.leaveRoom(reply),null);
    }
    @Test public void singleWindowFailureRecovery() throws Exception {exercise(false);}
    @Test public void multiWindowFailureRecovery() throws Exception {exercise(true);}
}
