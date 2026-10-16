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
        boolean dropHandshake, holdSetup, holdLeave;
        Runnable completeLeave;
        int leaves;
        OnReturnValue<Object> handshake, setup;
        long setupGeneration;
        int setups;
        @SuppressWarnings("unchecked") public <T> void callHandler(String method,Object[] args,OnReturnValue<T> callback) {
            if (method.equals("sdk.getLifecycle")) {
                handshake=(OnReturnValue<Object>)callback;
                if (!dropHandshake) ready();
            } else if (callback != null) {
                try {
                    long generation=((JSONObject)args[0]).getJSONObject("__whiteScope").getLong("generation");
                    if (method.equals("sdk.newWhiteSdk")) {
                        setups++;setup=(OnReturnValue<Object>)callback;setupGeneration=generation;
                        if (!holdSetup) completeSetup();
                    } else if (method.equals("sdk.leaveRoom")) {
                        leaves++; completeLeave=()->callback.onValue((T)envelope(generation,"{}"));
                        if (!holdLeave) completeLeave.run();
                    } else callback.onValue((T)envelope(generation,"{}"));
                } catch(Exception e){throw new AssertionError(e);}
            }
        }
        void ready(){try { handshake.onValue(new JSONObject().put("value","{\"documentId\":\"unit-doc\"}")); } catch(Exception e){throw new AssertionError(e);} }
        void completeSetup(){setup.onValue(envelope(setupGeneration,"{}"));}
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
}
