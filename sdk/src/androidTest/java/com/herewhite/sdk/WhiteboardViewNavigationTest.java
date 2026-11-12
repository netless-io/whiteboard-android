package com.herewhite.sdk;

import android.content.Context;
import android.os.SystemClock;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.platform.app.InstrumentationRegistry;
import com.herewhite.sdk.domain.Promise;
import com.herewhite.sdk.domain.RoomState;
import com.herewhite.sdk.domain.SDKError;
import org.json.JSONTokener;
import org.junit.Test;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import static org.junit.Assert.*;

/** Public WebView calls with real bundled JS; no lifecycle/manager replacements. */
public class WhiteboardViewNavigationTest extends WhiteSdkIntegrationTestHost {
    private void main(Runnable action) { InstrumentationRegistry.getInstrumentation().runOnMainSync(action); }
    private String js(String expression) throws Exception {
        AtomicReference<String> value=new AtomicReference<>();CountDownLatch done=new CountDownLatch(1);
        main(()->whiteboardView.evaluateJavascript(expression,result->{value.set(result);done.countDown();}));
        assertTrue("JS response",done.await(10,TimeUnit.SECONDS));return value.get();
    }
    private RoomState state() throws Exception {
        AtomicReference<RoomState> value=new AtomicReference<>();AtomicReference<SDKError> error=new AtomicReference<>();CountDownLatch done=new CountDownLatch(1);
        main(()->room.getRoomState(new Promise<RoomState>() {
            public void then(RoomState result){value.set(result);done.countDown();}
            public void catchEx(SDKError result){error.set(result);done.countDown();}
        }));
        assertTrue("Room API callback",done.await(10,TimeUnit.SECONDS));assertNull(error.get());assertNotNull(value.get());return value.get();
    }
    private void join(boolean preloaded) throws Exception {
        TestCredentialProvider.RoomCredentials c=TestCredentialProvider.requireRoomCredentials();
        CountDownLatch prepared=new CountDownLatch(1);AtomicReference<SDKError> error=new AtomicReference<>();
        main(()->{
            whiteboardView=new WhiteboardView(ApplicationProvider.getApplicationContext());
            whiteboardView.getSettings().setAllowUniversalAccessFromFileURLs(true);
            WhiteSdkConfiguration config=new WhiteSdkConfiguration(c.appIdentifier,true);
            config.setUseMultiViews(true);
            String hosts=InstrumentationRegistry.getArguments().getString("whiteboard.test.apiHosts");
            if(hosts!=null&&!hosts.isEmpty())config.setApiHosts(Arrays.asList(hosts.split(",")));
            if(preloaded){config.setEnableModelPreload(true);config.setPreloadBuiltinApps(Collections.singletonList("Slide"));}
            sdk=new WhiteSdk(whiteboardView,ApplicationProvider.getApplicationContext(),config);
            if(preloaded)sdk.prepareCompletion(45000,new Promise<Void>() {
                public void then(Void ignored){prepared.countDown();}
                public void catchEx(SDKError result){error.set(result);prepared.countDown();}
            });else prepared.countDown();
        });
        assertTrue("Preparation",prepared.await(50,TimeUnit.SECONDS));assertNull(error.get());
        joinExisting(c);
    }
    private void joinExisting(TestCredentialProvider.RoomCredentials c) throws Exception {
        CountDownLatch done=new CountDownLatch(1);AtomicReference<SDKError> error=new AtomicReference<>();
        main(()->sdk.joinRoom(new RoomParams(c.roomUuid,c.roomToken,"navigation-"+System.nanoTime()),null,new Promise<Room>() {
            public void then(Room result){room=result;done.countDown();}
            public void catchEx(SDKError result){error.set(result);done.countDown();}
        }));
        assertTrue("Join",done.await(60,TimeUnit.SECONDS));assertNull(error.get());assertNotNull(room);
    }
    private void scriptUrls(boolean preloaded) throws Exception {
        join(preloaded);state();Room original=room;long epoch=whiteboardView.lifecycle.documentEpoch();
        assertEquals("true",js("window.__scriptRoom=window.room;window.__scriptSDK=window.sdk;window.__scriptCalls=0;true"));
        String[] urls={"javascript:void(window.__scriptCalls++)","JaVaScRiPt:void(window.__scriptCalls++)","  javascript:void(window.__scriptCalls++)"};
        for(int i=0;i<urls.length;i++){
            String url=urls[i];main(()->whiteboardView.loadUrl(url));
            long deadline=SystemClock.elapsedRealtime()+10000;
            while(!Integer.toString(i+1).equals(js("window.__scriptCalls"))){assertTrue("Script executed",SystemClock.elapsedRealtime()<deadline);SystemClock.sleep(50);}
            assertSame(original,room);assertEquals(epoch,whiteboardView.lifecycle.documentEpoch());state();
            assertEquals("true",js("window.room===window.__scriptRoom&&window.sdk===window.__scriptSDK&&window.room.phase==='connected'"));
        }
    }
    @Test public void scriptUrlsKeepOrdinaryRoomAlive() throws Exception {scriptUrls(false);}
    @Test public void scriptUrlsKeepPreparedRoomAlive() throws Exception {scriptUrls(true);}

    @Test public void redirectedSdkEntryJoinsAndRejoinsAfterReload() throws Exception {
        try(AssetServer server=new AssetServer(ApplicationProvider.getApplicationContext())) {
            WhiteboardView.setEntryUrl(server.entry());
            try{join(false);}finally{WhiteboardView.setEntryUrl("file:///android_asset/whiteboard/index.html");}
            assertTrue(server.redirects>0);state();
            AtomicReference<String> url=new AtomicReference<>();main(()->url.set(whiteboardView.getUrl()));
            assertTrue(url.get(),url.get().endsWith("/whiteboard/index.html"));assertNotEquals(server.entry(),url.get());
            CountDownLatch left=new CountDownLatch(1);AtomicReference<SDKError> error=new AtomicReference<>();
            main(()->sdk.leaveRoom(new Promise<Object>() {
                public void then(Object ignored){left.countDown();}
                public void catchEx(SDKError result){error.set(result);left.countDown();}
            }));
            assertTrue(left.await(25,TimeUnit.SECONDS));assertNull(error.get());main(()->whiteboardView.reload());
            joinExisting(TestCredentialProvider.requireRoomCredentials());state();
        }
    }

    /** Real HTTP redirect; final document and all chunks are the unmodified SDK assets. */
    private static final class AssetServer implements AutoCloseable {
        final ServerSocket server;final Context context;final Thread worker;volatile int redirects;volatile boolean closed;
        AssetServer(Context context) throws IOException {
            this.context=context;server=new ServerSocket(0);
            worker=new Thread(()->{while(!closed)try{serve(server.accept());}catch(IOException e){if(!closed)throw new RuntimeException(e);}},"sdk-redirect-assets");worker.start();
        }
        String entry(){return "http://127.0.0.1:"+server.getLocalPort()+"/entry";}
        void serve(Socket socket) throws IOException {
            try(Socket client=socket){
                client.setSoTimeout(10000);BufferedReader input=new BufferedReader(new InputStreamReader(client.getInputStream(),"US-ASCII"));
                String line=input.readLine();if(line==null)return;String requestPath=line.split(" ")[1].split("\\?")[0];
                while((line=input.readLine())!=null&&!line.isEmpty()){}
                OutputStream output=client.getOutputStream();
                if(requestPath.equals("/entry")){redirects++;output.write("HTTP/1.1 302 Found\r\nLocation: /whiteboard/index.html\r\nContent-Length: 0\r\nConnection: close\r\n\r\n".getBytes("US-ASCII"));return;}
                if(!requestPath.startsWith("/whiteboard/")||requestPath.contains("..")){output.write("HTTP/1.1 404 Not Found\r\nContent-Length: 0\r\nConnection: close\r\n\r\n".getBytes("US-ASCII"));return;}
                try(InputStream asset=context.getAssets().open(requestPath.substring(1))){
                    ByteArrayOutputStream body=new ByteArrayOutputStream();byte[] buffer=new byte[8192];int n;while((n=asset.read(buffer))!=-1)body.write(buffer,0,n);
                    String mime=requestPath.endsWith(".js")?"application/javascript":requestPath.endsWith(".css")?"text/css":"text/html";
                    output.write(("HTTP/1.1 200 OK\r\nContent-Type: "+mime+"\r\nContent-Length: "+body.size()+"\r\nConnection: close\r\n\r\n").getBytes("US-ASCII"));body.writeTo(output);
                }
            }
        }
        public void close() throws IOException {closed=true;server.close();try{worker.join(1000);}catch(InterruptedException e){Thread.currentThread().interrupt();}}
    }
}
