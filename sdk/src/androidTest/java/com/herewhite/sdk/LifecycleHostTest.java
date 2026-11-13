package com.herewhite.sdk;

import androidx.test.platform.app.InstrumentationRegistry;
import com.herewhite.sdk.domain.SDKError;
import org.json.JSONObject;
import org.junit.Test;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import wendu.dsbridge.special.OnReturnValue;
import static org.junit.Assert.*;

/** Deterministic Native owner tests; no SDK/server or room credentials involved. */
public class LifecycleHostTest {
    static JSONObject envelope(long generation, Object value) {
        try { return new JSONObject().put("scope",new JSONObject().put("documentId","unit-doc").put("generation",generation)).put("value",value); }
        catch(Exception e){ throw new AssertionError(e); }
    }
    static class Raw implements JsBridgeInterface {
        boolean dropHandshake, holdSetup, holdLeave, holdPreload;
        Runnable completeLeave;
        int leaves, recoveries;
        boolean throwLeave;
        String leaveError;
        String document = "unit-doc";
        OnReturnValue<Object> handshake, setup;
        long setupGeneration;
        int setups;
        int observations;
        OnReturnValue<Object> preload;
        @SuppressWarnings("unchecked") public <T> void callHandler(String method,Object[] args,OnReturnValue<T> callback) {
            if (method.equals("sdk.getLifecycle")) {
                handshake=(OnReturnValue<Object>)callback;
                if (!dropHandshake) ready();
            } else if (method.equals("sdk.continueAfterLeaveFailure")) { recoveries++;
            } else if (callback != null) {
                try {
                    long generation=((JSONObject)args[0]).getJSONObject("__whiteScope").getLong("generation");
                    if (method.equals("sdk.newWhiteSdk")) {
                        setups++;setup=(OnReturnValue<Object>)callback;setupGeneration=generation;
                        if (!holdSetup) completeSetup();
                    } else if (method.equals("sdk.leaveRoom")) {
                        leaves++; if(throwLeave) throw new IllegalStateException("synthetic transport failure");
                        final String source = document;
                        completeLeave=()->callback.onValue((T)wrapped(source,generation,leaveError == null ? "{}" : leaveError));
                        if (!holdLeave) completeLeave.run();
                    } else if (method.equals("sdk.observePreload")) {
                        observations++; preload=(OnReturnValue<Object>)callback;
                        if (!holdPreload) completePreload("{\"state\":\"success\",\"message\":\"\"}");
                    } else callback.onValue((T)wrapped(document,generation,"{}"));
                } catch(RuntimeException e){throw e;} catch(Exception e){throw new AssertionError(e);}
            }
        }
        static JSONObject wrapped(String document,long generation,Object value){JSONObject e=envelope(generation,value);try{e.getJSONObject("scope").put("documentId",document);return e;}catch(Exception x){throw new AssertionError(x);}}
        void ready(){try { handshake.onValue(new JSONObject().put("value",new JSONObject().put("documentId",document).toString())); } catch(Exception e){throw new AssertionError(e);} }
        void completeSetup(){setup.onValue(wrapped(document,setupGeneration,"{}"));}
        void completePreload(String result){preload.onValue(envelope(0,result));}
        public <T> void callHandler(String m,OnReturnValue<T> c){callHandler(m,null,c);}
        public void callHandler(String m,Object[] a){callHandler(m,a,null);}
        public void addJavascriptObject(Object o,String n){}
        public void evaluateJavascript(String s){}
    }
    void main(Runnable work){InstrumentationRegistry.getInstrumentation().runOnMainSync(work);}
    void activate(LifecycleHost host, String key) throws Exception {
        CountDownLatch done=new CountDownLatch(1);
        main(()->host.replace(key,new Object[]{},5000,()->{},g->{assertTrue(host.continueJoin(g,e->fail(e.getMessage())));assertTrue(host.finish(g,true));done.countDown();},e->fail(e.getMessage())));
        assertTrue(done.await(2,TimeUnit.SECONDS));
    }
    @Test public void failedLeavePermitsSameAndCrossHostReplacement() throws Exception {
        for(boolean cross:new boolean[]{false,true}) {
            Raw raw=new Raw();LifecycleHost a=new LifecycleHost(raw),b=cross?new LifecycleHost(new Raw()):a;
            String key="failed-replace-"+System.nanoTime();main(()->{a.claim();if(cross)b.claim();});activate(a,key);
            raw.leaveError="{\"__error\":{\"message\":\"synthetic close failure\"}}";activate(b,key);
            main(()->{assertEquals(1,raw.recoveries);assertTrue(b.current(cross?1:3));raw.completeLeave.run();assertTrue(b.current(cross?1:3));a.destroy();b.destroy();});
        }
    }
    @Test public void explicitFailedLeaveRemainsObservableAndNextJoinRecoversCachedFailure() throws Exception {
        Raw raw=new Raw();LifecycleHost host=new LifecycleHost(raw);main(host::claim);String key="cached-failure-"+System.nanoTime();activate(host,key);
        raw.leaveError="{\"__error\":{\"message\":\"synthetic close failure\"}}";
        main(()->host.leave(1,value->assertEquals("synthetic close failure",SDKError.promiseError(value).getMessage())));
        activate(host,key+"-other-identity");main(()->{assertEquals(1,raw.leaves);assertEquals(1,raw.recoveries);assertTrue(host.current(3));host.destroy();});
    }
    @Test public void timeoutReplacementFencesLateOldReply() throws Exception {
        Raw raw=new Raw();LifecycleHost host=new LifecycleHost(raw);main(host::claim);String key="leave-timeout-"+System.nanoTime();activate(host,key);raw.holdLeave=true;
        CountDownLatch joined=new CountDownLatch(1);
        main(()->host.replace(key,new Object[]{},5000,()->{},g->{assertTrue(host.continueJoin(g,e->fail(e.getMessage())));assertTrue(host.finish(g,true));joined.countDown();},e->fail(e.getMessage())));
        main(()->{
            assertEquals(1,raw.leaves);
            try { java.lang.reflect.Method finish=LifecycleHost.class.getDeclaredMethod("finishLeave",long.class,String.class);finish.setAccessible(true);finish.invoke(host,1L,"{\"__error\":{\"message\":\"LeaveTimeout\"}}"); }
            catch(Exception e){throw new AssertionError(e);}
        });
        assertTrue(joined.await(2,TimeUnit.SECONDS));main(()->{raw.completeLeave.run();assertEquals(1,raw.recoveries);assertTrue(host.current(3));host.destroy();});
    }
    @Test public void synchronousLeaveExceptionDoesNotBlockReplacement() throws Exception {
        Raw raw=new Raw();LifecycleHost host=new LifecycleHost(raw);main(host::claim);String key="throw-leave-"+System.nanoTime();activate(host,key);raw.throwLeave=true;
        activate(host,key);main(()->{assertEquals(1,raw.recoveries);assertTrue(host.current(3));host.destroy();});
    }
    @Test public void activeNavigationRecoversSameHostAndFencesOldLeave() throws Exception {
        Raw raw=new Raw();LifecycleHost host=new LifecycleHost(raw);main(host::claim);String key="navigate-"+System.nanoTime();activate(host,key);
        main(()->{raw.holdLeave=true;host.prepareReload();assertFalse(host.current(1));host.setDocumentRecovery(()->{raw.document="new-doc";host.pageFinished();});});
        activate(host,key);main(()->{raw.completeLeave.run();assertTrue(host.current(3));host.destroy();});
    }
    @Test public void navigationCancelsOldSetupButNewJoinStillStarts() throws Exception {
        Raw raw=new Raw();raw.holdSetup=true;LifecycleHost host=new LifecycleHost(raw);AtomicInteger errors=new AtomicInteger();
        main(()->{host.claim();host.replace("old-nav-"+System.nanoTime(),new Object[]{},5000,()->{},g->fail("old join"),e->errors.incrementAndGet());});
        main(()->{host.prepareReload();assertEquals(1,errors.get());raw.holdSetup=false;host.setDocumentRecovery(()->{raw.document="new-doc";host.pageFinished();});});
        activate(host,"new-nav-"+System.nanoTime());main(host::destroy);
    }
    @Test public void validRedirectedSdkDocumentCompletesReloadHandshake() throws Exception {
        Raw raw=new Raw();LifecycleHost host=new LifecycleHost(raw);main(host::claim);
        main(()->{
            host.prepareReload();raw.document="redirected-sdk-doc";
            host.pageFinished(host.documentEpoch(),true);
        });
        activate(host,"redirected-sdk-"+System.nanoTime());main(host::destroy);
    }
    @Test public void latePageProbeCannotInvalidateReplacementDocument() throws Exception {
        Raw raw=new Raw();LifecycleHost host=new LifecycleHost(raw);main(host::claim);
        main(()->{
            long retired=host.documentEpoch();host.prepareReload();raw.document="replacement-sdk-doc";
            host.pageFinished(retired,false);
            host.pageFinished(host.documentEpoch(),true);
        });
        activate(host,"late-page-probe-"+System.nanoTime());main(host::destroy);
    }
    @Test public void unavailablePageStillRecoversOnNextExplicitJoin() throws Exception {
        Raw raw=new Raw();LifecycleHost host=new LifecycleHost(raw);main(host::claim);
        main(()->{
            host.pageFinished(host.documentEpoch(),false);
            host.setDocumentRecovery(()->{raw.document="restored-sdk-doc";host.pageFinished(host.documentEpoch(),true);});
        });
        activate(host,"unavailable-page-"+System.nanoTime());main(host::destroy);
    }
    @Test public void foreignIdleDocumentRecoversButDestroyedHostCannotRevive() throws Exception {
        Raw raw=new Raw();LifecycleHost host=new LifecycleHost(raw);main(()->{host.claim();host.documentUnavailable();host.setDocumentRecovery(()->{raw.document="new-doc";host.pageFinished();});});
        activate(host,"foreign-"+System.nanoTime());main(()->{host.prepareReload();host.destroy();host.replace("destroyed-"+System.nanoTime(),new Object[]{},5000,()->{},g->fail("destroyed join"),e->assertEquals("HostInvalid",e.getMessage()));});
    }
    @Test public void destroyedOwnerAllowsSameIdentityWithoutExplicitLeave() throws Exception {
        Raw oldRaw=new Raw(),newRaw=new Raw();LifecycleHost old=new LifecycleHost(oldRaw),fresh=new LifecycleHost(newRaw);
        String key="destroy-rejoin-"+System.nanoTime();main(()->{old.claim();fresh.claim();});
        activate(old,key);main(old::destroy);activate(fresh,key);
        main(()->{assertEquals(0,oldRaw.leaves);assertTrue(fresh.current(1));fresh.destroy();});
    }
    @Test public void destroyingOldOwnerUnblocksAnAlreadyWaitingReplacement() throws Exception {
        Raw oldRaw=new Raw(),newRaw=new Raw();LifecycleHost old=new LifecycleHost(oldRaw),fresh=new LifecycleHost(newRaw);
        String key="destroy-during-leave-"+System.nanoTime();main(()->{old.claim();fresh.claim();});activate(old,key);oldRaw.holdLeave=true;
        CountDownLatch joined=new CountDownLatch(1);
        main(()->fresh.replace(key,new Object[]{},5000,()->{},g->{assertTrue(fresh.continueJoin(g,e->fail(e.getMessage())));assertTrue(fresh.finish(g,true));joined.countDown();},e->fail(e.getMessage())));
        main(()->{assertEquals(1,oldRaw.leaves);assertFalse(fresh.current(1));old.destroy();});
        assertTrue(joined.await(2,TimeUnit.SECONDS));
        main(()->{oldRaw.completeLeave.run();old.destroy();assertTrue(fresh.current(1));fresh.destroy();});
    }
    @Test public void repeatedDestroyRejoinDoesNotAccumulateOwners() throws Exception {
        java.lang.reflect.Field field=LifecycleHost.class.getDeclaredField("owners");field.setAccessible(true);
        int initial=((java.util.Map<?,?>)field.get(null)).size();
        for(int i=0;i<50;i++) {
            Raw raw=new Raw();LifecycleHost host=new LifecycleHost(raw);main(host::claim);
            activate(host,"destroy-capacity-"+i+"-"+System.nanoTime());main(host::destroy);
        }
        assertTrue(((java.util.Map<?,?>)field.get(null)).size() <= initial);
    }
    @Test public void realWebViewDestroyRetiresItsLifecycle() {
        main(()->{
            WhiteboardView view=new WhiteboardView(androidx.test.core.app.ApplicationProvider.getApplicationContext());
            LifecycleHost host=view.lifecycle;long generation=host.admit(e->{});assertTrue(host.finish(generation,true));
            view.destroy();
            host.leave(generation,result->assertNull(SDKError.promiseError(result)));
            assertFalse(host.current(generation));
        });
    }
    @Test public void replacementDoesNotCloseOwnerThatChangedIdentityDuringLocalLeave() throws Exception {
        Raw rawA=new Raw(),rawB=new Raw();LifecycleHost a=new LifecycleHost(rawA),b=new LifecycleHost(rawB);
        String key="owner-race-"+System.nanoTime();main(()->{a.claim();b.claim();});
        try {
            activate(a,key+"1");activate(b,key+"2");rawB.holdLeave=true;
            CountDownLatch joined=new CountDownLatch(1);
            main(()->b.replace(key+"1",new Object[]{},5000,()->{},g->{assertTrue(b.continueJoin(g,e->fail(e.getMessage())));assertTrue(b.finish(g,true));joined.countDown();},e->fail(e.getMessage())));
            main(()->assertEquals(1,rawB.leaves));
            activate(a,key+"3");main(()->rawB.completeLeave.run());
            assertTrue(joined.await(2,TimeUnit.SECONDS));
            main(()->{assertEquals(1,rawA.leaves);assertTrue(a.current(2));assertTrue(b.current(2));});
        } finally {main(()->{rawB.holdLeave=false;a.leave(2,null);b.leave(2,null);a.invalidate();b.invalidate();});}
    }
    void timeout(boolean handshakeMissing) throws Exception {
        Raw raw=new Raw();raw.dropHandshake=handshakeMissing;raw.holdSetup=true;
        AtomicInteger calls=new AtomicInteger(), starts=new AtomicInteger();AtomicReference<String> error=new AtomicReference<>();
        CountDownLatch done=new CountDownLatch(1);LifecycleHost host=new LifecycleHost(raw);
        main(()->{host.claim();host.replace("timeout-"+handshakeMissing+System.nanoTime(),new Object[]{},150,()->{},g->starts.incrementAndGet(),e->{calls.incrementAndGet();error.set(e.getMessage());done.countDown();});});
        assertTrue(done.await(3,TimeUnit.SECONDS));assertEquals("JoinTimeout",error.get());
        main(()->{if(handshakeMissing)raw.ready();else raw.completeSetup();});
        Thread.sleep(100);assertEquals(1,calls.get());assertEquals(0,starts.get());
        if(handshakeMissing)assertEquals(0,raw.setups);
        main(host::invalidate);
    }
    @Test public void missingHandshakeUsesConfiguredDeadlineAndLateReplyCannotStart() throws Exception {timeout(true);}
    @Test public void delayedSetupUsesConfiguredDeadlineAndCompletesOnce() throws Exception {timeout(false);}
    @Test public void playerCannotStealPendingReplacementSlot() throws Exception {
        Raw raw=new Raw();raw.holdSetup=true;LifecycleHost host=new LifecycleHost(raw);CountDownLatch joined=new CountDownLatch(1);
        main(()->{host.claim();host.replace("player-"+System.nanoTime(),new Object[]{},2000,()->{},g->{assertTrue(host.continueJoin(g,e->fail(e.getMessage())));assertTrue(host.finish(g,true));joined.countDown();},e->fail(e.getMessage()));});
        main(()->{assertEquals(-1,host.admit(e->{}));raw.completeSetup();});
        assertTrue(joined.await(1,TimeUnit.SECONDS));main(host::invalidate);
    }
    @Test public void failedCloseCannotBeOverriddenByLateIdle() {
        Raw raw=new Raw();LifecycleHost host=new LifecycleHost(raw);
        main(()->{
            host.claim();long g=host.admit(e->{});host.finish(g,true);
            // Cache the actual close failure through the normal reply path.
            raw.holdSetup=false;
            try {
                java.lang.reflect.Method finish=LifecycleHost.class.getDeclaredMethod("finishLeave",long.class,String.class);finish.setAccessible(true);
                finish.invoke(host,g,"{\"__error\":{\"message\":\"LeaveTimeout\"}}");
            }catch(Exception e){throw new AssertionError(e);}
            try {host.stateEvent(envelope(g,new JSONObject().put("state","idle")));}catch(Exception e){throw new AssertionError(e);}
            assertEquals(-1,host.admit(e->{}));host.invalidate();
        });
    }

    @Test public void prepareWaitsForSetupAndSharesConstructorWork() throws Exception {
        Raw raw=new Raw();raw.holdSetup=true;LifecycleHost host=new LifecycleHost(raw);
        CountDownLatch done=new CountDownLatch(1);AtomicInteger completed=new AtomicInteger();
        main(()->{host.claim();host.configure(new Object[]{"config"},()->{});host.prepare(new Object[]{"config"},()->{},2000,new com.herewhite.sdk.domain.Promise<Void>() {
            public void then(Void value){completed.incrementAndGet();done.countDown();}
            public void catchEx(SDKError error){fail(error.getMessage());}
        });});
        main(()->{assertEquals(1,raw.setups);assertEquals(0,completed.get());raw.completeSetup();});
        assertTrue(done.await(2,TimeUnit.SECONDS));assertEquals(1,completed.get());main(host::invalidate);
    }
    @Test public void prepareMissingBridgeTimesOutOnceWithoutStartingRoom() throws Exception {
        Raw raw=new Raw();raw.dropHandshake=true;LifecycleHost host=new LifecycleHost(raw);
        CountDownLatch done=new CountDownLatch(1);AtomicInteger calls=new AtomicInteger();
        main(()->{host.claim();host.configure(new Object[]{"config"},()->{});host.prepare(new Object[]{"config"},()->{},100,new com.herewhite.sdk.domain.Promise<Void>() {
            public void then(Void value){fail("missing Bridge cannot prepare");}
            public void catchEx(SDKError error){assertEquals("PrepareTimeout",error.getMessage());calls.incrementAndGet();done.countDown();}
        });});
        assertTrue(done.await(2,TimeUnit.SECONDS));main(raw::ready);main(()->assertEquals(1,calls.get()));main(host::invalidate);
    }
    @Test public void prepareReportsConstructorFailureWithoutWaitingForDeadline() throws Exception {
        Raw raw=new Raw();raw.holdSetup=true;LifecycleHost host=new LifecycleHost(raw);CountDownLatch done=new CountDownLatch(1);
        main(()->{host.claim();host.configure(new Object[]{"bad-config"},()->{});host.prepare(new Object[]{"bad-config"},()->{},2000,new com.herewhite.sdk.domain.Promise<Void>() {
            public void then(Void value){fail("expected constructor failure");}
            public void catchEx(SDKError error){assertEquals("SdkSetupFailed",error.getMessage());done.countDown();}
        });});
        main(()->raw.setup.onValue(envelope(0,"{\"__error\":{\"message\":\"SdkSetupFailed\"}}")));
        assertTrue(done.await(1,TimeUnit.SECONDS));main(host::invalidate);
    }
}
