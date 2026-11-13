package com.herewhite.sdk;

import android.app.Activity;
import android.os.Bundle;
import android.os.SystemClock;
import android.webkit.JavascriptInterface;
import android.widget.FrameLayout;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.webkit.WebViewCompat;
import com.herewhite.sdk.domain.Promise;
import com.herewhite.sdk.domain.SDKError;
import com.herewhite.sdk.domain.WindowParams;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.junit.After;
import org.junit.Test;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import static org.junit.Assert.*;

/** Real task switches, keeping the actual Activity, WebView, Native SDK and Room. */
public class BackgroundLifecycleDeviceTest {
    public static class HostActivity extends Activity {
        static volatile HostActivity current;
        static volatile int creations;
        volatile int resumes, stops;
        volatile boolean resumed, focused;
        FrameLayout content;
        @Override public void onCreate(Bundle saved) {
            super.onCreate(saved); current=this; creations++;
            getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            content=new FrameLayout(this);setContentView(content);
        }
        @Override public void onResume(){super.onResume();resumes++;resumed=true;}
        @Override public void onPause(){resumed=false;super.onPause();}
        @Override public void onStop(){stops++;super.onStop();}
        @Override public void onWindowFocusChanged(boolean value){super.onWindowFocusChanged(value);focused=value;}
        @Override public void onDestroy(){if(current==this)current=null;super.onDestroy();}
    }
    public static class EventRecorder {
        private final JSONArray values=new JSONArray();
        @JavascriptInterface public synchronized void event(Object value) {
            try { values.put(value instanceof JSONObject ? value : new JSONObject(String.valueOf(value))); }
            catch(Exception error){values.put("Invalid background event");}
        }
        synchronized JSONArray snapshot() throws Exception {return new JSONArray(values.toString());}
    }
    private HostActivity activity;
    private WhiteboardView view;
    private WhiteSdk sdk;
    private Room room;
    private final EventRecorder sensor=new EventRecorder();
    private final JSONArray rows=new JSONArray();
    private final JSONObject report=new JSONObject();
    private int actionSequence;
    private void main(Runnable task){InstrumentationRegistry.getInstrumentation().runOnMainSync(task);}
    private String argument(String key,String fallback){return InstrumentationRegistry.getArguments().getString(key,fallback);}
    private void poll(java.util.concurrent.Callable<Boolean> condition,String name,long timeout) throws Exception {
        long deadline=SystemClock.elapsedRealtime()+timeout;
        while(!condition.call()){assertTrue(name,SystemClock.elapsedRealtime()<deadline);SystemClock.sleep(100);}
    }
    private String asset(String name) throws Exception {
        try(InputStream in=InstrumentationRegistry.getInstrumentation().getContext().getAssets().open(name)){
            ByteArrayOutputStream out=new ByteArrayOutputStream();byte[] b=new byte[8192];int n;
            while((n=in.read(b))>0)out.write(b,0,n);return out.toString("UTF-8");
        }
    }
    private Object js(String code) throws Exception {
        CountDownLatch done=new CountDownLatch(1);AtomicReference<String> result=new AtomicReference<>();
        main(()->view.evaluateJavascript(code,value->{result.set(value);done.countDown();}));
        assertTrue("JavaScript reply",done.await(15,TimeUnit.SECONDS));
        return new JSONTokener(result.get()).nextValue();
    }
    private JSONObject snapshot() throws Exception {
        return new JSONObject(String.valueOf(js("JSON.stringify(window.__backgroundProbe ? window.__backgroundProbe.snapshot() : {probeMissing:true})")));
    }
    private JSONObject nativeSnapshot() throws Exception {
        JSONObject value=new JSONObject();
        main(()->{try{
            value.put("url",view.getUrl()).put("progress",view.getProgress()).put("width",view.getWidth()).put("height",view.getHeight());
            for(String name:new String[]{"documentId","generation","state","invalid","destroyed"}){
                Field f=LifecycleHost.class.getDeclaredField(name);f.setAccessible(true);value.put(name,f.get(view.lifecycle));
            }
        }catch(Exception e){throw new AssertionError(e);}});
        return value;
    }
    private void requestAction(String action) throws Exception {
        requestAction(action,0);
    }
    private void requestAction(String action,long delayMs) throws Exception {
        // The host uses ADB directly, preserving any existing Appium/UiAutomation service.
        Bundle status=new Bundle();status.putString("backgroundAction",new JSONObject().put("id",++actionSequence).put("action",action).put("delayMs",delayMs).put("taskId",activity==null?-1:activity.getTaskId()).toString());
        InstrumentationRegistry.getInstrumentation().sendStatus(2,status);
    }
    private void persist() throws Exception {
        report.put("rows",rows).put("nativePageEvents",sensor.snapshot());
        File output=new File(InstrumentationRegistry.getInstrumentation().getTargetContext().getFilesDir(),"background-lifecycle-result.json");
        try(FileOutputStream stream=new FileOutputStream(output)){stream.write(report.toString(2).getBytes("UTF-8"));}
    }
    private JSONObject connected() throws Exception {
        final JSONObject[] result={null};
        poll(()->{
            JSONObject value=snapshot();assertFalse("Document/probe replaced",value.optBoolean("probeMissing"));
            JSONObject transport=value.getJSONObject("transport");
            assertEquals("Transport probe errors",0,transport.getJSONArray("sensorErrors").length());
            int open=0;JSONArray sockets=transport.getJSONArray("sockets");
            for(int i=0;i<sockets.length();i++){int state=sockets.getJSONObject(i).optInt("state",-1);if(state==0||state==1)open++;}
            assertTrue("At most one opening/open transport",open<=1);
            String phase=value.optString("phase");
            assertTrue("Room must recover without a business join: "+phase,phase.equals("connected")||phase.equals("connecting")||phase.equals("reconnecting"));
            result[0]=value;return phase.equals("connected")&&open==1;
        },"white-sdk/Akko automatic recovery",65000);
        return result[0];
    }
    @Test public void backgroundDoesNotLeaveRoom() throws Exception {
        TestCredentialProvider.RoomCredentials credentials=TestCredentialProvider.loadRoomCredentials();
        assertNotNull("Runtime room credentials required",credentials);
        int repeats=Integer.parseInt(argument("backgroundRepeats","10"));
        assertTrue(repeats>0&&repeats<=100);
        String[] durations=argument("backgroundDurationsMs","1000,10000,60000").split(",");
        String[] modes=argument("backgroundModes","home,other-app").split(",");
        boolean multi=Boolean.parseBoolean(argument("backgroundMultiViews","true"));
        boolean prepare=Boolean.parseBoolean(argument("backgroundPrepare","false"));
        report.put("status","running").put("model",android.os.Build.MODEL).put("manufacturer",android.os.Build.MANUFACTURER)
            .put("android",android.os.Build.VERSION.RELEASE).put("api",android.os.Build.VERSION.SDK_INT)
            .put("repeats",repeats).put("multiViews",multi).put("explicitPrepare",prepare);
        try {
            requestAction("launch");
            poll(()->HostActivity.current!=null,"Launcher Activity creation",45000);
            activity=HostActivity.current;
            poll(()->activity.resumed&&activity.focused,"Initial foreground Activity",15000);
            main(()->{
                android.webkit.WebView.setWebContentsDebuggingEnabled(true);
                view=new WhiteboardView(activity);activity.content.addView(view,new FrameLayout.LayoutParams(-1,-1));
                view.getSettings().setAllowUniversalAccessFromFileURLs(true);view.addJavascriptObject(sensor,"backgroundProbe");
                WhiteSdkConfiguration config=new WhiteSdkConfiguration(credentials.appIdentifier,true);config.setUseMultiViews(multi);
                String apiHosts=argument("whiteboard.test.apiHosts","");if(!apiHosts.isEmpty())config.setApiHosts(java.util.Arrays.asList(apiHosts.split(",")));
                sdk=new WhiteSdk(view,activity,config);
                try {
                    report.put("userAgent",view.getSettings().getUserAgentString());
                    android.content.pm.PackageInfo provider=WebViewCompat.getCurrentWebViewPackage(activity);
                    if(provider!=null)report.put("webViewPackage",provider.packageName).put("webViewVersion",provider.versionName);
                } catch(Exception e){throw new AssertionError(e);}
            });
            String lifecycle=asset("session-lifecycle-probe.js"),background=asset("background-lifecycle-probe.js");
            // Wait for the actual Bridge document. Evaluations submitted during the
            // initial about:blank navigation can lose their callback on old WebViews.
            poll(()->nativeSnapshot().has("documentId"),"Initial Bridge document handshake",60000);
            poll(()->Boolean.TRUE.equals(js(lifecycle)),"Real JS SDK ready",45000);
            assertEquals(Boolean.TRUE,js(background));
            if(prepare){
                CountDownLatch ready=new CountDownLatch(1);AtomicReference<SDKError> error=new AtomicReference<>();
                main(()->sdk.prepareCompletion(new Promise<Void>(){public void then(Void v){ready.countDown();}public void catchEx(SDKError e){error.set(e);ready.countDown();}}));
                assertTrue(ready.await(45,TimeUnit.SECONDS));assertNull(error.get());
            }
            join(credentials);
            assertEquals(Boolean.TRUE,js("window.__backgroundProbe.watchRoom()"));
            connected();int creations=HostActivity.creations;
            JSONObject initialNative=nativeSnapshot();
            for(String mode:modes)for(String duration:durations)for(int i=1;i<=repeats;i++){
                long hold=Long.parseLong(duration);assertTrue(hold>=1000&&hold<=600000);
                assertTrue(mode.equals("home")||mode.equals("other-app"));
                JSONObject row=new JSONObject().put("mode",mode).put("durationMs",hold).put("iteration",i).put("passed",false);
                JSONObject before=connected();int resumes=activity.resumes,stops=activity.stops;
                row.put("before",before);rows.put(row);persist();
                requestAction(mode);
                poll(()->activity.stops>stops&&!activity.resumed&&!activity.focused,"Actual Activity background",15000);
                long started=SystemClock.elapsedRealtime();
                // The Mac owns the timer: vendor ROMs can freeze this entire process.
                // Request it only after confirming the actual Activity onStop.
                requestAction("return",hold);
                try {poll(()->activity.resumes>resumes&&activity.resumed&&activity.focused,"Actual whiteboard foreground",hold+45000);}
                finally {row.put("resumesBefore",resumes).put("activityResumes",activity.resumes).put("activityStops",activity.stops)
                    .put("activityResumed",activity.resumed).put("activityFocused",activity.focused).put("activityCreations",HostActivity.creations);persist();}
                row.put("actualBackgroundMs",SystemClock.elapsedRealtime()-started);
                assertTrue("Full background hold",row.getLong("actualBackgroundMs")>=hold);
                assertEquals("Only the requested return resumes the Activity",resumes+1,activity.resumes);
                assertSame("Original Activity retained",activity,HostActivity.current);assertEquals("No Activity recreation",creations,HostActivity.creations);
                JSONObject after=connected(),nativeAfter=nativeSnapshot();
                row.put("after",after).put("nativeAfter",nativeAfter).put("nativePageEvents",sensor.snapshot());persist();
                assertEquals(initialNative.getString("documentId"),nativeAfter.getString("documentId"));
                assertEquals(initialNative.getLong("generation"),nativeAfter.getLong("generation"));
                assertFalse(nativeAfter.getBoolean("invalid"));assertFalse(nativeAfter.getBoolean("destroyed"));
                assertTrue("Original JS Room retained",after.getBoolean("sameRoom"));
                assertEquals("No SDK disconnect call",0,after.getInt("disconnectCalls"));
                JSONObject transport=after.getJSONObject("transport");
                assertEquals("No automatic/business duplicate join",1,transport.getInt("joinStarts"));assertEquals(1,transport.getJSONArray("rooms").length());
                JSONArray events=sensor.snapshot();
                for(int n=0;n<events.length();n++)assertNotEquals("Background must not dispatch pagehide","pagehide",events.getJSONObject(n).optString("type"));
                JSONArray protocol=transport.getJSONArray("protocol");
                for(int n=0;n<protocol.length();n++){
                    String method=protocol.getJSONObject(n).optString("method");
                    assertFalse("Background must not retire/leave the document: "+method,method.equals("sdk.documentNavigating")||method.equals("sdk.leaveRoom")||method.equals("room.disconnect"));
                }
                row.put("passed",true).put("activityResumes",activity.resumes).put("activityStops",activity.stops);persist();
                Bundle status=new Bundle();status.putString("stream","BACKGROUND PASS "+mode+" "+hold+"ms "+i+"/"+repeats+"\n");
                InstrumentationRegistry.getInstrumentation().sendStatus(2,status);
            }
            report.put("status","passed").put("initialNative",initialNative);persist();
        } catch(Throwable error){
            if(view!=null)report.put("failureNative",nativeSnapshot());
            report.put("status","failed").put("error",String.valueOf(error));persist();
            if(error instanceof Exception)throw (Exception)error;if(error instanceof Error)throw (Error)error;throw new AssertionError(error);
        }
    }
    private void join(TestCredentialProvider.RoomCredentials credentials) throws Exception {
        CountDownLatch done=new CountDownLatch(1);AtomicReference<SDKError> error=new AtomicReference<>();
        main(()->{
            RoomParams params=new RoomParams(credentials.roomUuid,credentials.roomToken,"background-"+java.util.UUID.randomUUID());
            params.setWindowParams(new WindowParams());
            sdk.joinRoom(params,null,new Promise<Room>(){public void then(Room value){room=value;done.countDown();}public void catchEx(SDKError e){error.set(e);done.countDown();}});
        });
        assertTrue("Public join callback",done.await(60,TimeUnit.SECONDS));assertNull("Public join error",error.get());assertNotNull(room);
    }
    @After public void cleanup(){
        main(()->{if(view!=null){if(activity!=null)activity.content.removeView(view);view.destroy();}if(activity!=null)activity.finish();});
    }
}
