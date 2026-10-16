package com.herewhite.sdk;

import android.os.SystemClock;
import android.content.Context;
import android.util.Log;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.platform.app.InstrumentationRegistry;
import com.herewhite.sdk.domain.Promise;
import com.herewhite.sdk.domain.SDKError;
import com.herewhite.sdk.domain.WindowParams;
import com.herewhite.sdk.domain.GlobalState;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/** Real WebView/server integration. Credentials only come from the existing local test configuration. */
public class SessionLifecycleIntegrationTest {
    WhiteboardView view; WhiteSdk sdk; Room room;
    TestCredentialProvider.RoomCredentials credentials;
    String uid; boolean expectedBlocked, writable, syncedStore;
    JSONObject evidence = new JSONObject();
    void main(Runnable task) { InstrumentationRegistry.getInstrumentation().runOnMainSync(task); }
    String js(String code) throws Exception {
        CountDownLatch done = new CountDownLatch(1); AtomicReference<String> value = new AtomicReference<>();
        main(() -> view.evaluateJavascript(code, result -> { value.set(result); done.countDown(); }));
        assertTrue("JS response", done.await(15, TimeUnit.SECONDS)); return value.get();
    }
    void bootstrap() throws Exception {
        credentials = TestCredentialProvider.requireRoomCredentials(); uid = "android-lifecycle-candidate-" + System.currentTimeMillis();
        Context context=ApplicationProvider.getApplicationContext();
        File assetDir=new File(context.getFilesDir(),"lifecycle-assets"); assetDir.mkdirs();
        String routing="<script>(function(){var O=window.WebSocket;function R(u,p){if(/^wss:\\/\\//.test(u))u='ws://127.0.0.1:18765/relay?group=android&target='+encodeURIComponent(u);return p===undefined?new O(u):new O(u,p)}R.prototype=O.prototype;['CONNECTING','OPEN','CLOSING','CLOSED'].forEach(function(k){R[k]=O[k]});window.WebSocket=R})()</script>";
        for(String name:context.getAssets().list("whiteboard")) {
            try(InputStream in=context.getAssets().open("whiteboard/"+name)) {
                java.io.ByteArrayOutputStream buffer=new java.io.ByteArrayOutputStream();byte[] chunk=new byte[8192];int n;
                while((n=in.read(chunk))>0)buffer.write(chunk,0,n);
                byte[] content=buffer.toByteArray();
                if(name.equals("index.html"))content=new String(content,"UTF-8").replace("<head>","<head>"+routing).getBytes("UTF-8");
                try(java.io.FileOutputStream out=new java.io.FileOutputStream(new File(assetDir,name))){out.write(content);}
            }
        }
        main(() -> {
            WhiteboardView.setEntryUrl("file://"+new File(assetDir,"index.html").getAbsolutePath());
            view = new WhiteboardView(ApplicationProvider.getApplicationContext());
            view.getSettings().setAllowFileAccess(true);
            view.getSettings().setAllowUniversalAccessFromFileURLs(true);
            view.loadUrl("file://"+new File(assetDir,"index.html").getAbsolutePath());
            WhiteSdkConfiguration config = new WhiteSdkConfiguration(credentials.appIdentifier, true);
            config.setUseMultiViews(true);
            config.setEnableAppliancePlugin(true);
            config.setEnableSyncedStore(syncedStore);
            sdk = new WhiteSdk(view, ApplicationProvider.getApplicationContext(), config);
        });
        try (InputStream input = InstrumentationRegistry.getInstrumentation().getContext().getAssets().open("session-lifecycle-probe.js")) {
            byte[] bytes = new byte[input.available()]; input.read(bytes); String probe = new String(bytes, "UTF-8");
            long deadline = SystemClock.elapsedRealtime() + 30000;
            while (!"true".equals(js(probe))) { assertTrue("SDK/probe ready", SystemClock.elapsedRealtime() < deadline); SystemClock.sleep(100); }
        }
    }
    RoomParams params() { RoomParams p = new RoomParams(credentials.roomUuid, credentials.roomToken, uid); p.setWritable(writable); p.setWindowParams(new WindowParams()); return p; }
    Room join() throws Exception {
        CountDownLatch done = new CountDownLatch(1); AtomicReference<SDKError> failure = new AtomicReference<>();
        main(() -> sdk.joinRoom(params(), null, new Promise<Room>() {
            public void then(Room value) { room = value; done.countDown(); }
            public void catchEx(SDKError error) { failure.set(error); done.countDown(); }
        }));
        assertTrue("join callback", done.await(60, TimeUnit.SECONDS)); assertNull("join error: " + failure.get(), failure.get()); return room;
    }
    void rejectDuplicate(String expected) throws Exception {
        CountDownLatch done = new CountDownLatch(1); AtomicReference<String> error = new AtomicReference<>();
        main(() -> sdk.joinRoom(params(), null, new Promise<Room>() {
            public void then(Room value) { done.countDown(); }
            public void catchEx(SDKError value) { error.set(value.getMessage()); done.countDown(); }
        }));
        assertTrue(done.await(5, TimeUnit.SECONDS)); assertNotNull(error.get()); assertEquals(expected,error.get());
    }
    void leave() throws Exception {
        CountDownLatch done = new CountDownLatch(1); AtomicReference<SDKError> error = new AtomicReference<>();
        main(() -> sdk.leaveRoom(new Promise<Object>() { public void then(Object value) { done.countDown(); }
            public void catchEx(SDKError value) { error.set(value); done.countDown(); } }));
        assertTrue(done.await(25, TimeUnit.SECONDS)); assertNull("leave: " + error.get(), error.get());
    }
    JSONObject snapshot(String name) throws Exception {
        String quoted = js("JSON.stringify(window.__lifecycleProbe.snapshot())");
        String raw = new org.json.JSONTokener(quoted).nextValue().toString();
        JSONObject snapshot = new JSONObject(raw); evidence.put(name, snapshot);
        assertEquals(0, snapshot.getJSONArray("sensorErrors").length());
        Log.i("LifecycleCandidate", name + " " + raw); return snapshot;
    }
    int open(JSONObject data) throws Exception { int result=0; for(int i=0;i<data.getJSONArray("sockets").length();i++) if(data.getJSONArray("sockets").getJSONObject(i).getInt("state")==1) result++;return result; }

    @Test public void releaseDuplicateLeaveAndRejoin() throws Exception {
        bootstrap();join();Room old=room;String previous= snapshot("joined").getJSONArray("rooms").getJSONObject(0).getString("session");
        main(() -> sdk.releaseRoom());join();assertNotSame(old,room);
        JSONObject replaced=snapshot("afterReplacement");assertEquals(2,replaced.getInt("joinStarts"));assertEquals(1,open(replaced));
        assertNotEquals(previous,replaced.getJSONArray("rooms").getJSONObject(1).getString("session"));
        assertNotEquals(1,replaced.getJSONArray("sockets").getJSONObject(0).getInt("state"));
        SystemClock.sleep(31000);assertEquals(1,open(snapshot("replacementAfter31s")));
        CountDownLatch stale=new CountDownLatch(1);AtomicReference<String> error=new AtomicReference<>();
        main(()->old.setWritable(true,new Promise<Boolean>(){public void then(Boolean v){stale.countDown();}public void catchEx(SDKError e){error.set(e.getMessage());stale.countDown();}}));
        assertTrue(stale.await(10,TimeUnit.SECONDS));assertEquals("StaleScope",error.get());leave();
    }
    @Test public void cancelLateSuccessKeepsOneCreateAndCompletesOnce() throws Exception {
        bootstrap();
        js("window.__originalJoin=window.sdk.joinRoom;window.sdk.joinRoom=function(p,c){return window.__originalJoin(p,c).then(function(r){return new Promise(function(done){setTimeout(function(){done(r)},4000)})})};true");
        CountDownLatch cancelled = new CountDownLatch(1); AtomicInteger callbacks = new AtomicInteger(); AtomicReference<String> error = new AtomicReference<>();
        main(() -> sdk.joinRoom(params(), null, new Promise<Room>() { public void then(Room r) {callbacks.incrementAndGet();cancelled.countDown();}
            public void catchEx(SDKError e) {callbacks.incrementAndGet();error.set(e.getMessage());cancelled.countDown();} }));
        long deadline=SystemClock.elapsedRealtime()+20000;
        while(snapshot("pending").getJSONArray("rooms").length()==0) { assertTrue(SystemClock.elapsedRealtime()<deadline);SystemClock.sleep(100); }
        CountDownLatch left = new CountDownLatch(1); AtomicReference<SDKError> leaveError=new AtomicReference<>();
        main(() -> sdk.leaveRoom(new Promise<Object>() {public void then(Object r){left.countDown();}public void catchEx(SDKError e){leaveError.set(e);left.countDown();}}));
        assertTrue(cancelled.await(5,TimeUnit.SECONDS)); assertEquals("Cancelled",error.get());
        assertTrue(left.await(20,TimeUnit.SECONDS)); assertNull(leaveError.get());assertEquals(1,callbacks.get());
        SystemClock.sleep(500);JSONObject finalState=snapshot("lateCleanup");assertEquals(1,finalState.getInt("joinStarts"));assertEquals(0,open(finalState));
    }
    @Test public void secondFacadeReplacesRoomOnSameView() throws Exception {
        bootstrap();join();Room old=room;
        main(()->{WhiteSdkConfiguration config=new WhiteSdkConfiguration(credentials.appIdentifier,true);config.setUseMultiViews(true);config.setEnableAppliancePlugin(true);sdk=new WhiteSdk(view,ApplicationProvider.getApplicationContext(),config);});
        assertNotNull(sdk);join();assertNotSame(old,room);JSONObject data=snapshot("secondFacadeReplacement");
        assertEquals(2,data.getInt("joinStarts"));assertEquals(1,open(data));leave();
    }

    @Test public void newWebViewReplacesDetachedOldHost() throws Exception {
        bootstrap();join();WhiteboardView oldView=view;String sameUid=uid;Room oldRoom=room;
        main(()->sdk.releaseRoom());bootstrap();uid=sameUid;join();assertNotSame(oldRoom,room);
        JSONObject fresh=snapshot("newHost");assertEquals(1,fresh.getInt("joinStarts"));assertEquals(1,open(fresh));
        WhiteboardView newView=view;view=oldView;JSONObject closed=snapshot("oldHost");view=newView;
        assertEquals(0,open(closed));assertEquals(1,closed.getInt("joinStarts"));main(()->oldView.destroy());leave();
    }
    @Test public void concurrentIdentitySwitchDoesNotDisconnectOtherUid() throws Exception {
        bootstrap();uid+="-A";join();WhiteboardView aView=view;WhiteSdk aSdk=sdk;String k1=uid;
        bootstrap();uid=k1+"-B";join();WhiteboardView bView=view;WhiteSdk bSdk=sdk;
        try {
            snapshot("B-before");
            js("var f=window.bridge.getFunction('sdk.leaveRoom').function;window.bridge.registerAsync('sdk.leaveRoom',function(){window.bridge.registerAsync('sdk.leaveRoom',f);var a=[].slice.call(arguments),reply=a.pop();a.push(function(v){window.__heldLeave=function(){reply(v)}});return f.apply(null,a)});true");
            uid=k1;CountDownLatch joined=new CountDownLatch(1);AtomicReference<SDKError> failure=new AtomicReference<>();
            main(()->bSdk.joinRoom(params(),null,new Promise<Room>(){public void then(Room r){joined.countDown();}public void catchEx(SDKError e){failure.set(e);joined.countDown();}}));
            long deadline=SystemClock.elapsedRealtime()+15000;while(!"true".equals(js("typeof window.__heldLeave==='function'"))){assertTrue("B local leave held",SystemClock.elapsedRealtime()<deadline);SystemClock.sleep(100);}
            assertEquals(0,open(snapshot("B-local-closed-receipt-held")));
            view=aView;sdk=aSdk;uid=k1+"-C";join();JSONObject aJoined=snapshot("A-new-uid-before-B-resume");
            view=bView;sdk=bSdk;js("window.__heldLeave();true");assertTrue(joined.await(60,TimeUnit.SECONDS));assertNull(failure.get());
            JSONObject bJoined=snapshot("B-target-joined");assertEquals(2,bJoined.getInt("joinStarts"));assertEquals(1,open(bJoined));assertEquals(k1,bJoined.getJSONArray("rooms").getJSONObject(1).getString("uid"));
            view=aView;sdk=aSdk;SystemClock.sleep(2000);JSONObject aAfter=snapshot("A-new-uid-still-connected");
            assertEquals(2,aAfter.getInt("joinStarts"));assertEquals(1,open(aAfter));assertEquals("connected",aAfter.getJSONArray("rooms").getJSONObject(1).getString("phase"));
            assertEquals(aJoined.getJSONArray("rooms").getJSONObject(1).getString("connectionSession"),aAfter.getJSONArray("rooms").getJSONObject(1).getString("connectionSession"));
        } finally {view=bView;sdk=bSdk;try{js("if(window.__heldLeave)window.__heldLeave();true");leave();snapshot("B-cleanup");}finally{main(()->bView.destroy());view=aView;sdk=aSdk;uid=k1+"-C";}}
        leave();
    }

    @Test public void pendingReplacementCancelsOldAndActuallyJoinsAgain() throws Exception {
        bootstrap();js("var original=window.sdk.joinRoom;window.sdk.joinRoom=function(p,c){return original.call(this,p,c).then(function(r){return new Promise(function(done){setTimeout(function(){done(r)},4000)})})};true");
        CountDownLatch oldDone=new CountDownLatch(1);AtomicInteger oldCalls=new AtomicInteger();AtomicReference<String> oldError=new AtomicReference<>();
        main(()->sdk.joinRoom(params(),null,new Promise<Room>(){public void then(Room r){oldCalls.incrementAndGet();oldDone.countDown();}public void catchEx(SDKError e){oldCalls.incrementAndGet();oldError.set(e.getMessage());oldDone.countDown();}}));
        long deadline=SystemClock.elapsedRealtime()+20000;while(snapshot("firstPending").getJSONArray("rooms").length()==0){assertTrue(SystemClock.elapsedRealtime()<deadline);SystemClock.sleep(100);}
        join();assertTrue(oldDone.await(2,TimeUnit.SECONDS));assertEquals("Cancelled",oldError.get());assertEquals(1,oldCalls.get());
        JSONObject finalState=snapshot("pendingReplaced");assertEquals(2,finalState.getInt("joinStarts"));assertEquals(1,open(finalState));assertNotEquals(1,finalState.getJSONArray("sockets").getJSONObject(0).getInt("state"));leave();
    }
    @Test public void queuedJoinThenLeaveCancelsReplacement() throws Exception {
        bootstrap(); join();
        CountDownLatch done = new CountDownLatch(2); AtomicReference<String> error = new AtomicReference<>();
        main(() -> {
            sdk.joinRoom(params(), null, new Promise<Room>() {
                public void then(Room value) { error.set("UnexpectedSuccess"); done.countDown(); }
                public void catchEx(SDKError value) { error.set(value.getMessage()); done.countDown(); }
            });
            sdk.leaveRoom(new Promise<Object>() {
                public void then(Object value) { done.countDown(); }
                public void catchEx(SDKError value) { error.set(value.getMessage()); done.countDown(); }
            });
        });
        assertTrue(done.await(25, TimeUnit.SECONDS)); assertEquals("Cancelled", error.get());
        JSONObject result = snapshot("queuedJoinThenLeave"); assertEquals(1,result.getInt("joinStarts")); assertEquals(0,open(result));
    }
    @Test public void lastQueuedRequestWins() throws Exception {
        bootstrap();join();AtomicInteger cancelled=new AtomicInteger();CountDownLatch done=new CountDownLatch(3);AtomicReference<SDKError> lastError=new AtomicReference<>();
        main(()->{for(int i=0;i<3;i++){final int n=i;sdk.joinRoom(params(),null,new Promise<Room>(){public void then(Room r){if(n!=2)lastError.set(new SDKError("Superseded request succeeded"));else room=r;done.countDown();}public void catchEx(SDKError e){if(n<2&&"Cancelled".equals(e.getMessage()))cancelled.incrementAndGet();else lastError.set(e);done.countDown();}});}});
        assertTrue(done.await(40,TimeUnit.SECONDS));assertNull(lastError.get());assertEquals(2,cancelled.get());JSONObject data=snapshot("latestOnly");assertEquals(2,data.getInt("joinStarts"));assertEquals(1,open(data));leave();
    }

    void gate(boolean blocked) throws Exception {
        java.net.HttpURLConnection c=(java.net.HttpURLConnection)new java.net.URL("http://127.0.0.1:18765/control?group=android&block="+(blocked?1:0)).openConnection();
        try(InputStream in=c.getInputStream()){while(in.read()!=-1){}}finally{c.disconnect();}
    }
    void phase(String expected,int seconds) throws Exception {
        long deadline=SystemClock.elapsedRealtime()+seconds*1000L;
        while(!expected.equals(snapshot("phase-"+expected).getJSONArray("rooms").getJSONObject(0).getString("phase"))){assertTrue("Expected "+expected,SystemClock.elapsedRealtime()<deadline);SystemClock.sleep(250);}
    }
    @Test public void automaticReconnectDoesNotJoinAgain() throws Exception {
        bootstrap();join();gate(true);js("window.__lifecycleProbe.closeTransport(0)");phase("reconnecting",10);
        SystemClock.sleep(8000);JSONObject held=snapshot("reconnectingAfter8s");assertEquals("reconnecting",held.getJSONArray("rooms").getJSONObject(0).getString("phase"));assertEquals(1,held.getInt("joinStarts"));
        gate(false);phase("connected",60);assertEquals(1,open(snapshot("recovered")));assertEquals(1,snapshot("onePublicJoin").getInt("joinStarts"));leave();
    }
    @Test public void reconnectingLeaveTerminalBlocksRejoin() throws Exception {
        bootstrap();join();gate(true);js("window.__lifecycleProbe.closeTransport(0)");phase("reconnecting",10);
        CountDownLatch done=new CountDownLatch(1);AtomicReference<String> failure=new AtomicReference<>();
        main(()->sdk.leaveRoom(new Promise<Object>(){public void then(Object v){done.countDown();}public void catchEx(SDKError e){failure.set(e.getMessage());done.countDown();}}));
        assertTrue(done.await(25,TimeUnit.SECONDS));assertEquals("CloseUnconfirmed",failure.get());expectedBlocked=true;phase("disconnected",10);rejectDuplicate("CloseUnconfirmed");gate(false);SystemClock.sleep(3000);assertEquals(1,snapshot("terminalRetryRejected").getInt("joinStarts"));
    }
    @Test public void initialTimeoutKeepsOneCreateAndLateSuccessIsCleaned() throws Exception {
        bootstrap();js("window.__originalJoin=window.sdk.joinRoom;window.sdk.joinRoom=function(p,c){return window.__originalJoin(p,c).then(function(r){return new Promise(function(done){setTimeout(function(){done(r)},48000)})})};true");
        CountDownLatch done=new CountDownLatch(1);AtomicInteger calls=new AtomicInteger();AtomicReference<String> failure=new AtomicReference<>();
        main(()->sdk.joinRoom(params(),null,new Promise<Room>(){public void then(Room r){calls.incrementAndGet();done.countDown();}public void catchEx(SDKError e){calls.incrementAndGet();failure.set(e.getMessage());done.countDown();}}));
        SystemClock.sleep(30500);assertEquals(1,snapshot("oneJoinAt30s").getInt("joinStarts"));assertTrue(done.await(20,TimeUnit.SECONDS));assertEquals("JoinTimeout",failure.get());
        leave();SystemClock.sleep(500);assertEquals(1,calls.get());JSONObject data=snapshot("timeoutLateCleanup");assertEquals(1,data.getInt("joinStarts"));assertEquals(0,open(data));
    }
    @Test public void disconnectAndLeaveShareOneActualExit() throws Exception {
        bootstrap();join();js("window.__disconnectCount=0;var r=window.room,original=r.disconnect.bind(r);r.disconnect=function(){window.__disconnectCount++;return original()};true");
        CountDownLatch disconnected=new CountDownLatch(1);CountDownLatch left=new CountDownLatch(1);AtomicReference<SDKError> failure=new AtomicReference<>();
        main(()->{room.disconnect(new Promise<Object>(){public void then(Object v){disconnected.countDown();}public void catchEx(SDKError e){failure.set(e);disconnected.countDown();}});sdk.leaveRoom(new Promise<Object>(){public void then(Object v){left.countDown();}public void catchEx(SDKError e){failure.set(e);left.countDown();}});});
        assertTrue(disconnected.await(25,TimeUnit.SECONDS));assertTrue(left.await(2,TimeUnit.SECONDS));assertNull(failure.get());assertEquals("1",js("window.__disconnectCount"));join();leave();
    }


    @Test public void sdkMethodsKeepTheirOriginalReturnTypes() throws Exception {
        bootstrap();CountDownLatch state=new CountDownLatch(1);AtomicReference<SDKError> failure=new AtomicReference<>();
        main(()->sdk.getLocalLogState(new Promise<JSONObject>(){public void then(JSONObject v){assertTrue(v.has("enabled"));assertFalse(v.has("scope"));state.countDown();}public void catchEx(SDKError e){failure.set(e);state.countDown();}}));
        assertTrue(state.await(10,TimeUnit.SECONDS));assertNull(failure.get());join();
        CountDownLatch query=new CountDownLatch(1);
        main(()->sdk.hasBackgroundImage(new com.herewhite.sdk.domain.HasBackgroundImageParams("mainView","/init","https://example.invalid/lifecycle.png",new String[]{"whiteSdk"}),new Promise<Boolean>(){public void then(Boolean v){assertFalse(v);query.countDown();}public void catchEx(SDKError e){failure.set(e);query.countDown();}}));
        assertTrue(query.await(10,TimeUnit.SECONDS));assertNull(failure.get());leave();
    }
    @Test public void playerStopAndLeaveShareOneExit() throws Exception {
        bootstrap();writable=true;join();js("window.room.setGlobalState({candidateReplaySeed:Date.now()});true");SystemClock.sleep(3000);leave();SystemClock.sleep(2000);
        CountDownLatch created=new CountDownLatch(1);AtomicReference<Player> value=new AtomicReference<>();AtomicReference<SDKError> failure=new AtomicReference<>();
        com.herewhite.sdk.domain.PlayerConfiguration config=new com.herewhite.sdk.domain.PlayerConfiguration(credentials.roomUuid,credentials.roomToken);config.setWindowParams(new WindowParams());
        main(()->sdk.createPlayer(config,new Promise<Player>(){public void then(Player p){value.set(p);created.countDown();}public void catchEx(SDKError e){failure.set(e);created.countDown();}}));
        assertTrue(created.await(60,TimeUnit.SECONDS));assertNull(failure.get());
        long schedule=Math.max(0,Long.parseLong(js("window.player.timeDuration"))-1000);
        main(()->value.get().seekToScheduleTime(schedule));
        long deadline=SystemClock.elapsedRealtime()+30000;while(!"\"pause\"".equals(js("window.player.phase"))){assertTrue(SystemClock.elapsedRealtime()<deadline);SystemClock.sleep(200);}
        while(!"true".equals(js("!!window.manager"))){assertTrue(SystemClock.elapsedRealtime()<deadline);SystemClock.sleep(200);}
        js("window.__stopCount=0;var p=window.player,original=p.stop.bind(p);p.stop=function(){window.__stopCount++;return original()};true");
        main(()->value.get().stop());leave();assertEquals("1",js("window.__stopCount"));writable=false;join();leave();
    }


    Player configuredPlayer(WhiteSdk facade) throws Exception {
        CountDownLatch done=new CountDownLatch(1);AtomicReference<Player> value=new AtomicReference<>();AtomicReference<SDKError> error=new AtomicReference<>();
        com.herewhite.sdk.domain.PlayerConfiguration config=new com.herewhite.sdk.domain.PlayerConfiguration(credentials.roomUuid,credentials.roomToken);config.setWindowParams(new WindowParams());
        main(()->facade.createPlayer(config,new Promise<Player>(){public void then(Player p){value.set(p);done.countDown();}public void catchEx(SDKError e){error.set(e);done.countDown();}}));
        assertTrue(done.await(60,TimeUnit.SECONDS));assertNull(error.get());
        long schedule=Math.max(0,Long.parseLong(js("window.player.timeDuration"))-1000);main(()->value.get().seekToScheduleTime(schedule));
        long deadline=SystemClock.elapsedRealtime()+30000;while(!"\"pause\"".equals(js("window.player.phase"))){assertTrue("Player ready",SystemClock.elapsedRealtime()<deadline);SystemClock.sleep(200);}
        return value.get();
    }
    @Test public void playerFacadeConfigurationFollowsSelectedFacade() throws Exception {
        bootstrap();writable=true;join();js("window.room.setGlobalState({candidateReplaySeed:Date.now()});true");SystemClock.sleep(3000);
        WhiteSdk first=sdk;AtomicReference<WhiteSdk> second=new AtomicReference<>();
        main(()->{WhiteSdkConfiguration config=new WhiteSdkConfiguration(credentials.appIdentifier,true);config.setUseMultiViews(false);second.set(new WhiteSdk(view,ApplicationProvider.getApplicationContext(),config));});
        assertEquals("true",js("window.room.useMultiViews"));leave();SystemClock.sleep(2000);
        sdk=second.get();configuredPlayer(sdk);assertEquals("false",js("window.player.useMultiViews"));assertEquals("false",js("!!window.manager"));
        evidence.put("selectedB",new JSONObject((String)new org.json.JSONTokener(js("JSON.stringify({useMultiViews:window.player.useMultiViews,phase:window.player.phase,hasManager:!!window.manager})")).nextValue()));
        leave();sdk=first;configuredPlayer(sdk);assertEquals("true",js("window.player.useMultiViews"));
        long deadline=SystemClock.elapsedRealtime()+30000;while(!"true".equals(js("!!window.manager"))){assertTrue(SystemClock.elapsedRealtime()<deadline);SystemClock.sleep(200);}
        evidence.put("selectedA",new JSONObject((String)new org.json.JSONTokener(js("JSON.stringify({useMultiViews:window.player.useMultiViews,phase:window.player.phase,hasManager:!!window.manager})")).nextValue()));
        assertEquals(0,open(snapshot("oldRoomClosedDuringReplays")));leave();
    }
    @Test public void cancelledPlayerSetupCannotStartLateReplay() throws Exception {
        bootstrap();
        js("window.__holdPlayerSetup=true;window.__actualReplayStarts=0;var init=window.bridge.getFunction('sdk.newWhiteSdk').function,replay=window.bridge.getFunction('sdk.replayRoom').function;window.bridge.registerAsync('sdk.newWhiteSdk',function(){var a=[].slice.call(arguments),reply=a.pop();a.push(function(v){if(window.__holdPlayerSetup)window.__latePlayerSetup=function(){reply(v)};else reply(v)});return init.apply(null,a)});window.bridge.registerAsync('sdk.replayRoom',function(){window.__actualReplayStarts++;return replay.apply(null,arguments)});true");
        AtomicInteger calls=new AtomicInteger();AtomicReference<String> result=new AtomicReference<>();
        main(()->sdk.createPlayer(new com.herewhite.sdk.domain.PlayerConfiguration(credentials.roomUuid,credentials.roomToken),new Promise<Player>(){public void then(Player p){calls.incrementAndGet();result.set("UnexpectedSuccess");}public void catchEx(SDKError e){calls.incrementAndGet();result.set(e.getMessage());}}));
        long deadline=SystemClock.elapsedRealtime()+15000;while(!"true".equals(js("!!window.__latePlayerSetup"))){assertTrue(SystemClock.elapsedRealtime()<deadline);SystemClock.sleep(100);}
        assertEquals(0,calls.get());assertEquals("0",js("window.__actualReplayStarts"));leave();assertEquals("Cancelled",result.get());
        js("window.__holdPlayerSetup=false;true");configuredPlayer(sdk);js("window.__latePlayerSetup();true");SystemClock.sleep(300);
        assertEquals(1,calls.get());assertEquals("1",js("window.__actualReplayStarts"));assertEquals("\"pause\"",js("window.player.phase"));
        evidence.put("cancelledSetup",new JSONObject((String)new org.json.JSONTokener(js("JSON.stringify({actualReplayStarts:window.__actualReplayStarts,phase:window.player.phase})")).nextValue()));leave();
    }
    @Test public void externalWatchdogCancelsSlowPlayerBeforeCreatingNext() throws Exception {
        bootstrap();writable=true;join();js("window.room.setGlobalState({candidateReplaySeed:Date.now()});true");SystemClock.sleep(3000);leave();SystemClock.sleep(2000);
        js("window.__playerStarts=0;window.__playerOrder=[];var original=window.sdk.replayRoom.bind(window.sdk);window.sdk.replayRoom=function(p,c){var n=++window.__playerStarts;window.__playerOrder.push({event:'create-'+n,at:Date.now()});return original(p,c).then(function(player){var stop=player.stop.bind(player);player.stop=function(){window.__playerOrder.push({event:'stop-'+n,at:Date.now()});return stop()};player.seekToProgressTime(Math.max(0,player.timeDuration-1000));if(n!==1)return player;window.__delayedPlayer=player;return new Promise(function(resolve){setTimeout(function(){resolve(player)},50000)})})};true");
        com.herewhite.sdk.domain.PlayerConfiguration config=new com.herewhite.sdk.domain.PlayerConfiguration(credentials.roomUuid,credentials.roomToken);config.setWindowParams(new WindowParams());
        AtomicInteger firstCalls=new AtomicInteger();AtomicReference<String> firstError=new AtomicReference<>();
        main(()->sdk.createPlayer(config,new Promise<Player>(){public void then(Player p){firstCalls.incrementAndGet();firstError.set("UnexpectedSuccess");}public void catchEx(SDKError e){firstCalls.incrementAndGet();firstError.set(e.getMessage());}}));
        SystemClock.sleep(46000);assertEquals(0,firstCalls.get());assertEquals("1",js("window.__playerStarts"));assertEquals("\"pause\"",js("window.__delayedPlayer.phase"));
        evidence.put("pendingAfter46s",new JSONObject((String)new org.json.JSONTokener(js("JSON.stringify({starts:window.__playerStarts,phase:window.__delayedPlayer.phase,order:window.__playerOrder})")).nextValue()));
        leave();assertEquals(1,firstCalls.get());assertEquals("Cancelled",firstError.get());
        CountDownLatch created=new CountDownLatch(1);AtomicReference<SDKError> error=new AtomicReference<>();
        main(()->sdk.createPlayer(config,new Promise<Player>(){public void then(Player p){created.countDown();}public void catchEx(SDKError e){error.set(e);created.countDown();}}));
        assertTrue(created.await(60,TimeUnit.SECONDS));assertNull(error.get());
        long deadline=SystemClock.elapsedRealtime()+30000;while(!"\"pause\"".equals(js("window.player.phase"))||!"true".equals(js("!!window.manager"))){assertTrue(SystemClock.elapsedRealtime()<deadline);SystemClock.sleep(200);}
        leave();assertEquals(1,firstCalls.get());assertEquals("true",js("window.__playerOrder.map(function(e){return e.event}).join(',')==='create-1,stop-1,create-2,stop-2'"));
        evidence.put("playerOrder",new org.json.JSONArray((String)new org.json.JSONTokener(js("JSON.stringify(window.__playerOrder)")).nextValue()));
    }

    @Test public void longOutageTerminalDoesNotAutoJoin() throws Exception {
        bootstrap();join();gate(true);js("window.__lifecycleProbe.closeTransport(0)");phase("reconnecting",10);SystemClock.sleep(51000);
        phase("disconnected",5);expectedBlocked=true;rejectDuplicate("room did disconnect");gate(false);SystemClock.sleep(3000);JSONObject end=snapshot("afterLongOutage");assertEquals(1,end.getInt("joinStarts"));assertEquals(0,open(end));
    }

    @Test public void playerStopBeforeFirstFrameFailureBlocksRejoin() throws Exception {
        bootstrap();CountDownLatch created=new CountDownLatch(1);AtomicReference<Player> value=new AtomicReference<>();AtomicReference<SDKError> failure=new AtomicReference<>();
        com.herewhite.sdk.domain.PlayerConfiguration config=new com.herewhite.sdk.domain.PlayerConfiguration(credentials.roomUuid,credentials.roomToken);config.setWindowParams(new WindowParams());
        main(()->sdk.createPlayer(config,new Promise<Player>(){public void then(Player p){value.set(p);created.countDown();}public void catchEx(SDKError e){failure.set(e);created.countDown();}}));
        assertTrue(created.await(60,TimeUnit.SECONDS));assertNull(failure.get());assertEquals("\"waitingFirstFrame\"",js("window.player.phase"));
        js("window.__stopCount=0;var p=window.player,original=p.stop.bind(p);p.stop=function(){window.__stopCount++;return original()};true");
        CountDownLatch stopped=new CountDownLatch(1);
        main(()->{value.get().stop();sdk.leaveRoom(new Promise<Object>(){public void then(Object v){stopped.countDown();}public void catchEx(SDKError e){failure.set(e);stopped.countDown();}});});
        assertTrue(stopped.await(25,TimeUnit.SECONDS));assertNotNull("Uninitialized upstream stop must remain a failure",failure.get());expectedBlocked=true;
        rejectDuplicate(failure.get().getMessage());assertEquals("1",js("window.__stopCount"));assertEquals(0,snapshot("stopFailureRetryRejected").getInt("joinStarts"));
    }

    @Test public void reloadRequiresCompletedLeaveAndRehandshakes() throws Exception {
        bootstrap();join();Room old=room;String document=js("(function(){var d;window.bridge.getFunction('sdk.getLifecycle').function(function(v){d=JSON.parse(v.value).documentId});return d})()");
        leave();snapshot("beforeControlledReload");main(()->view.reload());
        String probe;try(InputStream in=InstrumentationRegistry.getInstrumentation().getContext().getAssets().open("session-lifecycle-probe.js")){byte[] bytes=new byte[in.available()];in.read(bytes);probe=new String(bytes,"UTF-8");}
        long deadline=SystemClock.elapsedRealtime()+30000;
        while(true){String next=js("window.sdk && (function(){var d;window.bridge.getFunction('sdk.getLifecycle').function(function(v){d=JSON.parse(v.value).documentId});return d})()");
            if(next!=null && !next.equals("null") && !next.equals(document) && "true".equals(js(probe)))break;
            assertTrue("New document handshake",SystemClock.elapsedRealtime()<deadline);SystemClock.sleep(100);
        }
        join();main(()->old.setGlobalState(new GlobalState()));SystemClock.sleep(300);
        assertEquals(1,open(snapshot("afterControlledReload")));assertEquals(1,snapshot("newDocumentOneJoin").getInt("joinStarts"));
        // Active/unknown navigation must never become the safe refresh path.
        main(()->view.reload());SystemClock.sleep(1000);expectedBlocked=true;rejectDuplicate("HostInvalid");
    }

    @Test public void mutableJoinParametersAreCapturedBeforeQueue() throws Exception {
        bootstrap(); RoomParams p=params();String expectedUid=uid;
        CountDownLatch done=new CountDownLatch(1);AtomicReference<SDKError> failure=new AtomicReference<>();
        main(()->{sdk.joinRoom(p,null,new Promise<Room>(){public void then(Room r){room=r;done.countDown();}public void catchEx(SDKError e){failure.set(e);done.countDown();}});p.setTimeout(1,TimeUnit.MILLISECONDS);p.setWritable(!writable);});
        assertTrue(done.await(60,TimeUnit.SECONDS));assertNull(failure.get());JSONObject data=snapshot("capturedParameters");
        assertEquals(expectedUid,data.getJSONArray("rooms").getJSONObject(0).getString("uid"));assertEquals(1,open(data));leave();
    }
    @Test public void setupDelayUsesConfiguredTimeoutAndRejectsPlayer() throws Exception {
        bootstrap();
        js("var f=window.bridge.getFunction('sdk.newWhiteSdk').function;window.bridge.registerAsync('sdk.newWhiteSdk',function(){var a=[].slice.call(arguments),reply=a.pop();a.push(function(v){setTimeout(function(){reply(v)},1800)});return f.apply(null,a)});true");
        CountDownLatch done=new CountDownLatch(1);AtomicInteger calls=new AtomicInteger();AtomicReference<String> failure=new AtomicReference<>();
        RoomParams p=params();p.setTimeout(600,TimeUnit.MILLISECONDS);
        main(()->sdk.joinRoom(p,null,new Promise<Room>(){public void then(Room r){calls.incrementAndGet();done.countDown();}public void catchEx(SDKError e){calls.incrementAndGet();failure.set(e.getMessage());done.countDown();}}));
        SystemClock.sleep(200);CountDownLatch rejected=new CountDownLatch(1);AtomicReference<String> playerError=new AtomicReference<>();
        main(()->sdk.createPlayer(new com.herewhite.sdk.domain.PlayerConfiguration(credentials.roomUuid,credentials.roomToken),new Promise<Player>(){public void then(Player p){rejected.countDown();}public void catchEx(SDKError e){playerError.set(e.getMessage());rejected.countDown();}}));
        assertTrue(rejected.await(2,TimeUnit.SECONDS));assertEquals("Busy",playerError.get());assertTrue(done.await(3,TimeUnit.SECONDS));assertEquals("JoinTimeout",failure.get());
        SystemClock.sleep(2000);assertEquals(1,calls.get());assertEquals(0,snapshot("lateSetupNoJoin").getInt("joinStarts"));leave();
    }
    @Test public void facadeABALastJoinReceivesRoomStoreAndCommonEvents() throws Exception {
        syncedStore=true;writable=true;bootstrap();
        WhiteSdk a=sdk;join();AtomicInteger phases=new AtomicInteger(),media=new AtomicInteger();
        WhiteSdkConfiguration config=new WhiteSdkConfiguration(credentials.appIdentifier,true);config.setUseMultiViews(true);config.setEnableAppliancePlugin(true);config.setEnableSyncedStore(true);
        main(()->sdk=new WhiteSdk(view,ApplicationProvider.getApplicationContext(),config));join();
        main(()->a.setCommonCallbacks(new CommonCallback(){public void onPPTMediaPause(){media.incrementAndGet();}}));
        RoomListener listener=new RoomListener(){public void onPhaseChanged(com.herewhite.sdk.domain.RoomPhase p){phases.incrementAndGet();}public void onDisconnectWithError(Exception e){}public void onKickedWithReason(String r){}public void onRoomStateChanged(com.herewhite.sdk.domain.RoomState s){}public void onCanUndoStepsUpdate(long n){}public void onCanRedoStepsUpdate(long n){}public void onCatchErrorWhenAppendFrame(long id,Exception e){}};
        CountDownLatch done=new CountDownLatch(1);AtomicReference<SDKError> failure=new AtomicReference<>();
        main(()->{sdk=a;a.joinRoom(params(),listener,new Promise<Room>(){public void then(Room r){room=r;done.countDown();}public void catchEx(SDKError e){failure.set(e);done.countDown();}});});
        assertTrue(done.await(60,TimeUnit.SECONDS));assertNull(failure.get());assertTrue("Native phase listener",phases.get()>0);
        js("window.bridge.call('sdk.onPPTMediaPause',{scope:{documentId:(function(){var d;window.bridge.getFunction('sdk.getLifecycle').function(function(v){d=JSON.parse(v.value).documentId});return d})(),generation:3},value:{shapeId:'test',type:'audio'}});true");
        SystemClock.sleep(300);assertEquals(1,media.get());
        CountDownLatch storageReady=new CountDownLatch(1),storageChanged=new CountDownLatch(1);
        String storageName="audit-"+uid;
        main(()->room.getSyncedStore().connectStorage(storageName,"{}",new Promise<String>(){public void then(String v){storageReady.countDown();}public void catchEx(SDKError e){failure.set(e);storageReady.countDown();}}));
        assertTrue(storageReady.await(10,TimeUnit.SECONDS));assertNull(failure.get());
        main(()->{room.getSyncedStore().addOnStateChangedListener(storageName,(v,d)->storageChanged.countDown());room.getSyncedStore().setStorageState(storageName,"{\"key\":1}");});
        assertTrue("Native store listener",storageChanged.await(5,TimeUnit.SECONDS));
        JSONObject data=snapshot("facadeABA");assertEquals(3,data.getInt("joinStarts"));assertEquals(1,open(data));leave();
    }

    @After public void cleanup() throws Exception {
        if(view==null)return;
        try { gate(false); if(sdk!=null && !expectedBlocked) leave(); if("true".equals(js("!!window.__lifecycleProbe")))snapshot("final");else evidence.put("documentDiscarded",true); } finally {
            Context context=ApplicationProvider.getApplicationContext();
            try(FileWriter writer=new FileWriter(new File(context.getFilesDir(),"lifecycle-"+uid+".json"))){writer.write(evidence.toString(2));}
            main(() -> view.destroy());
        }
    }
}
