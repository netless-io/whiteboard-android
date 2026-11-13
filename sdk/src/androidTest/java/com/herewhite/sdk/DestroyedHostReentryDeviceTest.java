package com.herewhite.sdk;

import android.os.SystemClock;
import android.util.Log;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.platform.app.InstrumentationRegistry;
import com.herewhite.sdk.domain.Promise;
import com.herewhite.sdk.domain.SDKError;
import com.herewhite.sdk.domain.WindowParams;
import org.junit.Test;
import org.json.JSONObject;
import org.json.JSONTokener;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.Assert.*;

/** Ordinary public join/exit API matrix, with real WebViews and a real room. */
public class DestroyedHostReentryDeviceTest {
    public static class HostActivity extends android.app.Activity {
        static volatile HostActivity current;
        android.widget.FrameLayout boards;
        @Override public void onCreate(android.os.Bundle state){super.onCreate(state);current=this;boards=new android.widget.FrameLayout(this);setContentView(boards);}
        @Override public void onDestroy(){if(current==this)current=null;super.onDestroy();}
    }
    HostActivity activity;
    boolean preloaded;
    final class Host {
        WhiteboardView view; WhiteSdk sdk; Room room;
        CountDownLatch joined = new CountDownLatch(1);
        AtomicInteger callbacks = new AtomicInteger();
        SDKError failure;
        final CountDownLatch preloadedDone=new CountDownLatch(1);
        final AtomicInteger preloadCallbacks=new AtomicInteger();
        com.herewhite.sdk.domain.WhitePreloadResult preloadResult;
    }
    TestCredentialProvider.RoomCredentials credentials;
    String probe;
    void main(Runnable r) { InstrumentationRegistry.getInstrumentation().runOnMainSync(r); }
    String js(Host host, String code) throws Exception {
        CountDownLatch done=new CountDownLatch(1);String[] result={null};
        main(()->host.view.evaluateJavascript(code,v->{result[0]=v;done.countDown();}));
        assertTrue("JS completion",done.await(15,TimeUnit.SECONDS));return result[0];
    }
    Host create() throws Exception {
        Host host=new Host();
        main(()->{
            host.view=new WhiteboardView(activity);
            activity.boards.addView(host.view,new android.widget.FrameLayout.LayoutParams(-1,-1));
            host.view.getSettings().setAllowUniversalAccessFromFileURLs(true);
            newSdk(host);
        });
        long deadline=SystemClock.elapsedRealtime()+40000;
        while(!"true".equals(js(host,probe))) {
            assertTrue("ordinary SDK construction",SystemClock.elapsedRealtime()<deadline);SystemClock.sleep(50);
        }
        if(preloaded){
            Exit prepared=new Exit();
            main(()->host.sdk.prepareCompletion(45000,new Promise<Void>(){
                public void then(Void value){prepared.callbacks.incrementAndGet();prepared.done.countDown();}
                public void catchEx(SDKError e){prepared.failure=e;prepared.callbacks.incrementAndGet();prepared.done.countDown();}
            }));
            exited(prepared);assertTrue("optional preload completion",host.preloadedDone.await(55,TimeUnit.SECONDS));
            assertEquals("success",host.preloadResult.getState());assertEquals(1,host.preloadCallbacks.get());
            assertEquals(0,snapshot(host).getInt("joinStarts"));assertEquals(0,open(snapshot(host)));
        }
        return host;
    }
    // Construct a new Native wrapper without constructing or reloading the View.
    void newSdk(Host host) {
            WhiteSdkConfiguration config=new WhiteSdkConfiguration(credentials.appIdentifier,true);
            config.setUseMultiViews(true);
            if(preloaded){config.setEnableModelPreload(true);config.setPreloadBuiltinApps(java.util.Arrays.asList("Slide","Presentation"));}
            String hosts=InstrumentationRegistry.getArguments().getString("matrixApiHosts","");
            if(!hosts.isEmpty())config.setApiHosts(java.util.Arrays.asList(hosts.split(",")));
            host.sdk=new WhiteSdk(host.view,ApplicationProvider.getApplicationContext(),config,new CommonCallback(){
                @Override public void onPreloadStateChange(com.herewhite.sdk.domain.WhitePreloadResult result){host.preloadResult=result;host.preloadCallbacks.incrementAndGet();host.preloadedDone.countDown();}
            },null);
    }
    WeakReference<WhiteSdk> releaseSdk(Host host) {
        WeakReference<WhiteSdk> old=new WeakReference<>(host.sdk);
        main(()->host.sdk=null);
        return old;
    }
    boolean collected(WeakReference<WhiteSdk> old) {
        for(int i=0;i<3;i++){System.gc();System.runFinalization();SystemClock.sleep(50);}
        return old.get()==null;
    }
    void join(Host host,String uid) {
        join(host,uid,false);
    }
    void join(Host host,String uid,boolean differentRoom) {
        main(()->{
            String uuid=differentRoom?InstrumentationRegistry.getArguments().getString("matrixOtherUuid"):credentials.roomUuid;
            String token=differentRoom?InstrumentationRegistry.getArguments().getString("matrixOtherToken"):credentials.roomToken;
            RoomParams p=new RoomParams(uuid,token,uid);p.setWritable(false);p.setWindowParams(new WindowParams());
            host.sdk.joinRoom(p,null,new Promise<Room>() {
                public void then(Room room){host.room=room;host.callbacks.incrementAndGet();host.joined.countDown();}
                public void catchEx(SDKError e){host.failure=e;host.callbacks.incrementAndGet();host.joined.countDown();}
            });
        });
    }
    void joined(Host host) throws Exception {
        assertTrue("public join callback",host.joined.await(55,TimeUnit.SECONDS));
        assertNull("join error",host.failure);assertNotNull(host.room);assertEquals(1,host.callbacks.get());
        connectedSnapshot(host);
    }
    JSONObject snapshot(Host h) throws Exception { return new JSONObject(new JSONTokener(js(h,"JSON.stringify(window.__lifecycleProbe.snapshot())")).nextValue().toString()); }
    JSONObject connectedSnapshot(Host h) throws Exception {
        long deadline=SystemClock.elapsedRealtime()+45000;boolean observed=false;
        while(true){JSONObject s=snapshot(h);assertEquals(0,s.getJSONArray("sensorErrors").length());org.json.JSONArray rooms=s.getJSONArray("rooms");String phase=rooms.getJSONObject(rooms.length()-1).getString("phase");
            int active=open(s);assertTrue("Multiple open transports",active<=1);
            if(phase.equals("connected")&&active==1)return s;
            assertTrue("Room terminated: "+phase,phase.equals("connected")||phase.equals("reconnecting")||phase.equals("connecting"));
            if(!observed){Log.i("DestroyedHostMatrix","WAIT white-sdk/Akko internal "+phase+" open="+active+" snapshot="+s);observed=true;}
            assertTrue("SDK recovery deadline",SystemClock.elapsedRealtime()<deadline);SystemClock.sleep(100);
        }
    }
    int open(JSONObject s) throws Exception {int n=0;for(int i=0;i<s.getJSONArray("sockets").length();i++)if(s.getJSONArray("sockets").getJSONObject(i).getInt("state")==1)n++;return n;}
    void ordered(JSONObject old,JSONObject fresh) throws Exception {
        long closed=0,started=0;
        for(int i=0;i<old.getJSONArray("events").length();i++){JSONObject e=old.getJSONArray("events").getJSONObject(i);if(e.optString("type").equals("room-phase")&&e.optString("phase").equals("disconnected"))closed=e.getLong("at");}
        for(int i=0;i<fresh.getJSONArray("events").length();i++){JSONObject e=fresh.getJSONArray("events").getJSONObject(i);if(e.optString("type").equals("join-start"))started=e.getLong("at");}
        assertTrue("old Room disconnected before replacement actual join",closed>0&&started>=closed);
    }
    static final class Exit {
        final CountDownLatch done=new CountDownLatch(1);
        final AtomicInteger callbacks=new AtomicInteger();
        SDKError failure;
    }
    Exit exit(Host h,String mode) {
        Exit exit=new Exit();
        Promise<Object> callback=new Promise<Object>() {
            public void then(Object result){exit.callbacks.incrementAndGet();exit.done.countDown();}
            public void catchEx(SDKError e){exit.failure=e;exit.callbacks.incrementAndGet();exit.done.countDown();}
        };
        main(()->{if(mode.equals("disconnect"))h.room.disconnect(callback);else h.sdk.leaveRoom(callback);});return exit;
    }
    void exited(Exit exit) throws Exception {
        assertTrue("public exit callback",exit.done.await(25,TimeUnit.SECONDS));
        assertNull("exit error",exit.failure);assertEquals("once-only exit callback",1,exit.callbacks.get());
    }
    void destroy(Host h) {if(h!=null&&h.view!=null){main(()->{activity.boards.removeView(h.view);h.view.destroy();});h.view=null;}}
    @org.junit.After public void finishActivity(){if(activity!=null)main(activity::finish);}
    void initialize() throws Exception {
        credentials=TestCredentialProvider.requireRoomCredentials();
        android.content.Context context=InstrumentationRegistry.getInstrumentation().getTargetContext();
        android.content.Intent intent=new android.content.Intent(context,HostActivity.class).addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK);
        if(InstrumentationRegistry.getArguments().getString("matrixExternalLaunch","false").equals("true")){
            android.os.Bundle status=new android.os.Bundle();status.putString("backgroundAction","{\"id\":0,\"action\":\"launch\",\"category\":\"com.herewhite.sdk.DESTROYED_HOST_LAUNCH\"}");
            InstrumentationRegistry.getInstrumentation().sendStatus(2,status);
            long deadline=SystemClock.elapsedRealtime()+45000;
            while(HostActivity.current==null){assertTrue("Launcher Activity creation",SystemClock.elapsedRealtime()<deadline);SystemClock.sleep(100);}
            activity=HostActivity.current;
        } else activity=(HostActivity)InstrumentationRegistry.getInstrumentation().startActivitySync(intent);
        try(InputStream in=InstrumentationRegistry.getInstrumentation().getContext().getAssets().open("session-lifecycle-probe.js")) {
            java.io.ByteArrayOutputStream out=new java.io.ByteArrayOutputStream();byte[] bytes=new byte[8192];int n;while((n=in.read(bytes))>0)out.write(bytes,0,n);probe=out.toString("UTF-8");
        }
    }
    @Test public void ordinaryJoinExitMatrixTwentyEach() throws Exception {
        initialize();
        int repeats=Integer.parseInt(InstrumentationRegistry.getArguments().getString("matrixRepeats","20"));
        String only=InstrumentationRegistry.getArguments().getString("matrixCase","");
        String[] cases={"destroy-first","removeView-live-takeover","leave-await-destroy","disconnect-await-destroy","leave-destroy-without-wait","disconnect-destroy-without-wait","live-takeover-then-destroy-A","destroy-A-during-takeover","releaseRoom-destroy-first","releaseRoom-live-takeover","duplicate-exit-destroy","different-uid-live","different-room-live","destroy-B-during-takeover"};
        for(String name:cases) {if(!only.isEmpty()&&!only.equals(name))continue;for(int iteration=1;iteration<=repeats;iteration++) {
            Host a=null,b=null,c=null;long started=SystemClock.elapsedRealtime();
            String uid="android-destroy-"+name+"-"+iteration+"-"+System.currentTimeMillis();
            try {
                java.util.List<Exit> exits=new java.util.ArrayList<>();
                a=create();join(a,uid);joined(a);String oldSession=snapshot(a).getJSONArray("rooms").getJSONObject(0).getString("connectionSession");
                if(name.equals("removeView-live-takeover")){
                    Host old=a;main(()->activity.boards.removeView(old.view));
                    assertNull("Old View removed without explicit destroy",a.view.getParent());
                    assertEquals("Removal alone retains the old Room until SDK replacement",1,open(connectedSnapshot(a)));
                }
                if(name.startsWith("releaseRoom")){Host old=a;main(old.sdk::releaseRoom);}
                if(name.equals("leave-await-destroy")||name.equals("disconnect-await-destroy")) {
                    exited(exit(a,name.startsWith("disconnect")?"disconnect":"leave"));destroy(a);
                } else if(name.endsWith("destroy-without-wait")) {exits.add(exit(a,name.startsWith("disconnect")?"disconnect":"leave"));destroy(a);}
                else if(name.equals("duplicate-exit-destroy")) {exits.add(exit(a,"disconnect"));exits.add(exit(a,"leave"));destroy(a);}
                else if(name.endsWith("destroy-first")||name.equals("destroy-first"))destroy(a);
                boolean held=name.equals("destroy-A-during-takeover")||name.equals("destroy-B-during-takeover");
                if(held) js(a,"var f=window.bridge.getFunction('sdk.leaveRoom').function;window.bridge.registerAsync('sdk.leaveRoom',function(){var a=[].slice.call(arguments);setTimeout(function(){f.apply(null,a)},1500)});true");
                b=create();join(b,name.equals("different-uid-live")?uid+"-other":uid,name.equals("different-room-live"));
                if(held){SystemClock.sleep(150);assertEquals(1,b.joined.getCount());
                    if(name.equals("destroy-B-during-takeover")) {destroy(b);assertTrue(b.joined.await(3,TimeUnit.SECONDS));assertNotNull(b.failure);c=create();join(c,uid);joined(c);}
                    else destroy(a);
                }
                Host active=c==null?b:c;joined(active);
                JSONObject fresh=connectedSnapshot(active);assertEquals(1,open(fresh));assertEquals(1,fresh.getInt("joinStarts"));
                assertNotEquals(oldSession,fresh.getJSONArray("rooms").getJSONObject(0).getString("connectionSession"));
                if(a.view!=null) {JSONObject old=snapshot(a);boolean independent=name.equals("different-uid-live")||name.equals("different-room-live");assertEquals(independent?1:0,open(old));if(!independent)ordered(old,fresh);}
                destroy(a);SystemClock.sleep(150);assertEquals(1,open(connectedSnapshot(active)));assertEquals(1,active.callbacks.get());
                for(Exit exit:exits)exited(exit);
                exited(exit(active,"leave"));assertEquals(0,open(snapshot(active)));
                Log.i("DestroyedHostMatrix","PASS "+name+" "+iteration+"/"+repeats+" durationMs="+(SystemClock.elapsedRealtime()-started));
                android.os.Bundle status=new android.os.Bundle();status.putString("stream","DETACH_MATRIX PASS "+name+" "+iteration+"/"+repeats+"\n");
                InstrumentationRegistry.getInstrumentation().sendStatus(2,status);
            } catch(Throwable e) {Log.e("DestroyedHostMatrix","FAIL "+name+" "+iteration+" "+e.getClass().getSimpleName());throw e;}
            finally {destroy(a);destroy(b);destroy(c);}
        }}
    }
    @Test public void preloadedSameHostReuseMatrixTwentyEach() throws Exception {
        preloaded=true;initialize();
        int repeats=Integer.parseInt(InstrumentationRegistry.getArguments().getString("matrixRepeats","20"));
        for(String mode:new String[]{"leave","direct"})for(String identity:new String[]{"same","different-uid","different-room","different-both"})for(int i=1;i<=repeats;i++){
            String name="preload-reuse-"+mode+"-"+identity;Host host=null;long started=SystemClock.elapsedRealtime();
            try{
                String uid="android-reuse-"+name+"-"+i+"-"+System.currentTimeMillis();
                host=create();join(host,uid);joined(host);
                String firstSession=connectedSnapshot(host).getJSONArray("rooms").getJSONObject(0).getString("connectionSession");
                if(identity.equals("same"))js(host,"window.__lifecycleProbe.reconnectOnNextLeave()");
                if(mode.equals("leave"))exited(exit(host,"leave"));
                js(host,"window.__reuseSdk=window.sdk;var f=window.sdk.joinRoom.bind(window.sdk);window.sdk.joinRoom=function(p,c){window.__reuseJoinGate=window.__lifecycleProbe.snapshot();return f(p,c)};true");
                host.joined=new CountDownLatch(1);host.callbacks.set(0);host.failure=null;host.room=null;
                join(host,identity.equals("same")||identity.equals("different-room")?uid:uid+"-other",identity.equals("different-room")||identity.equals("different-both"));joined(host);
                JSONObject gate=new JSONObject(new JSONTokener(js(host,"JSON.stringify(window.__reuseJoinGate)")).nextValue().toString());
                assertEquals("old socket closed before second actual join",0,open(gate));assertEquals("disconnected",gate.getJSONArray("rooms").getJSONObject(0).getString("phase"));
                JSONObject fresh=connectedSnapshot(host);assertEquals(2,fresh.getInt("joinStarts"));assertEquals(2,fresh.getJSONArray("rooms").length());assertEquals(1,open(fresh));
                assertNotEquals(firstSession,fresh.getJSONArray("rooms").getJSONObject(1).getString("connectionSession"));
                assertEquals("true",js(host,"window.sdk===window.__reuseSdk"));assertEquals(1,host.preloadCallbacks.get());
                if(identity.equals("same")){assertEquals("\"reconnecting\"",js(host,"window.__lifecycleProbe.reconnectLeavePhase"));assertTrue("recovery transport must be observed",fresh.getJSONArray("sockets").length()>=3);}
                exited(exit(host,"leave"));assertEquals(0,open(snapshot(host)));
                Log.i("DestroyedHostMatrix","PASS "+name+" "+i+"/"+repeats+" durationMs="+(SystemClock.elapsedRealtime()-started));
            }catch(Throwable e){Log.e("DestroyedHostMatrix","FAIL "+name+" "+i+" "+e.getClass().getSimpleName());throw e;}
            finally{destroy(host);}
        }
    }
    @Test public void nativeSdkReplacementMatrixTenEach() throws Exception {
        initialize();
        int repeats=Integer.parseInt(InstrumentationRegistry.getArguments().getString("matrixRepeats","10"));
        String only=InstrumentationRegistry.getArguments().getString("matrixCase","");
        for(boolean preload:new boolean[]{false,true})for(String mode:new String[]{"drop-reference-takeover","new-sdk-direct-same","new-sdk-direct-different-uid","new-sdk-direct-different-room","new-sdk-direct-different-both","new-sdk-after-leave"})for(int i=1;i<=repeats;i++){
            preloaded=preload;
            String name=(preload?"preload-":"ordinary-")+mode;
            if(!only.isEmpty()&&!only.equals(name))continue;
            Host a=null,b=null;long started=SystemClock.elapsedRealtime();
            try {
                String uid="android-wrapper-"+i+"-"+System.currentTimeMillis();
                a=create();join(a,uid);joined(a);
                JSONObject before=connectedSnapshot(a);String session=before.getJSONArray("rooms").getJSONObject(0).getString("connectionSession");
                if(mode.equals("new-sdk-after-leave"))exited(exit(a,"leave"));
                js(a,"window.__replacementSdk=window.sdk;window.__replacementViewToken={};true");
                WhiteboardView sameView=a.view;
                WeakReference<WhiteSdk> old=releaseSdk(a);boolean collectedBefore=collected(old);
                assertNull(a.sdk);
                JSONObject afterDrop=snapshot(a);
                assertEquals(mode.equals("new-sdk-after-leave")?0:1,open(afterDrop));
                if(mode.equals("drop-reference-takeover")){
                    b=create();join(b,uid);joined(b);
                    JSONObject fresh=connectedSnapshot(b),closed=snapshot(a);
                    assertEquals(1,fresh.getInt("joinStarts"));assertEquals(0,open(closed));ordered(closed,fresh);
                    assertNotEquals(session,fresh.getJSONArray("rooms").getJSONObject(0).getString("connectionSession"));
                    assertSame(sameView,a.view);assertEquals("true",js(a,"window.sdk===window.__replacementSdk"));
                    destroy(a);SystemClock.sleep(150);connectedSnapshot(b);exited(exit(b,"leave"));assertEquals(0,open(snapshot(b)));
                }else{
                    js(a,"var f=window.sdk.joinRoom.bind(window.sdk);window.sdk.joinRoom=function(p,c){window.__replacementJoinGate=window.__lifecycleProbe.snapshot();return f(p,c)};true");
                    Host host=a;main(()->newSdk(host));assertNotNull(a.sdk);assertNotSame(old.get(),a.sdk);assertSame(sameView,a.view);
                    a.callbacks.set(0);a.joined=new CountDownLatch(1);a.failure=null;a.room=null;
                    boolean otherUid=mode.endsWith("different-uid")||mode.endsWith("different-both");
                    boolean otherRoom=mode.endsWith("different-room")||mode.endsWith("different-both");
                    join(a,otherUid?uid+"-other":uid,otherRoom);joined(a);
                    JSONObject gate=new JSONObject(new JSONTokener(js(a,"JSON.stringify(window.__replacementJoinGate)")).nextValue().toString());
                    assertEquals(0,open(gate));assertEquals("disconnected",gate.getJSONArray("rooms").getJSONObject(0).getString("phase"));
                    JSONObject fresh=connectedSnapshot(a);assertEquals(2,fresh.getInt("joinStarts"));assertEquals(2,fresh.getJSONArray("rooms").length());assertEquals(1,open(fresh));
                    assertNotEquals(session,fresh.getJSONArray("rooms").getJSONObject(1).getString("connectionSession"));
                    assertEquals("true",js(a,"window.sdk===window.__replacementSdk"));
                    assertEquals(otherRoom?InstrumentationRegistry.getArguments().getString("matrixOtherUuid"):credentials.roomUuid,fresh.getJSONArray("rooms").getJSONObject(1).getString("uuid"));
                    assertEquals(otherUid?uid+"-other":uid,fresh.getJSONArray("rooms").getJSONObject(1).getString("uid"));
                    exited(exit(a,"leave"));assertEquals(0,open(snapshot(a)));
                }
                boolean collectedAfter=collected(old);
                Log.i("DestroyedHostMatrix","MEMORY "+name+" "+i+" businessReferenceReleased=true sdkCollectedBefore="+collectedBefore+" sdkCollectedAfter="+collectedAfter);
                Log.i("DestroyedHostMatrix","PASS "+name+" "+i+"/"+repeats+" durationMs="+(SystemClock.elapsedRealtime()-started));
            }catch(Throwable e){Log.e("DestroyedHostMatrix","FAIL "+name+" "+i+" "+e.getClass().getSimpleName());
                for(Host failed:new Host[]{a,b})if(failed!=null&&failed.view!=null)try{Log.e("DestroyedHostMatrix","FAIL_SNAPSHOT "+snapshot(failed));}catch(Exception ignored){}
                throw e;}
            finally{destroy(a);destroy(b);}
        }
    }

}
