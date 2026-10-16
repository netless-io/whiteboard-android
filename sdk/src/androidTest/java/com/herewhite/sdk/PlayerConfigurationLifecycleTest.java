package com.herewhite.sdk;

import android.content.Context;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.platform.app.InstrumentationRegistry;
import com.herewhite.sdk.domain.PlayerConfiguration;
import com.herewhite.sdk.domain.Promise;
import com.herewhite.sdk.domain.SDKError;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Test;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import wendu.dsbridge.special.OnReturnValue;
import static org.junit.Assert.*;

/** Production Native entry points with a controlled transport; no room credentials. */
public class PlayerConfigurationLifecycleTest {
    static class View extends WhiteboardView {
        JSONObject configuration, replayParameters;
        int replayStarts;
        boolean holdSetup;
        final List<OnReturnValue<String>> setupReplies = new ArrayList<>();
        View(Context context) { super(context); }
        @Override public void loadUrl(String url) { }
        @Override public void addJavascriptObject(Object value, String namespace) { }
        @SuppressWarnings("unchecked") @Override public <T> void callHandler(String method, Object[] args, OnReturnValue<T> reply) {
            try {
                if (method.equals("sdk.getLifecycle")) {
                    reply.onValue((T) new JSONObject().put("value", "{\"documentId\":\"player-unit-doc\"}")); return;
                }
                // Unscoped SDK helpers normally enter through WhiteboardView.callCurrent.
                if (args == null || args.length == 0 || !(args[0] instanceof JSONObject) || !((JSONObject) args[0]).has("__whiteScope")) {
                    lifecycle.callCurrent(method, args, reply); return;
                }
                JSONObject scope = ((JSONObject) args[0]).getJSONObject("__whiteScope");
                if (method.equals("sdk.newWhiteSdk")) {
                    configuration = new JSONObject(Utils.toJson(args[1]));
                    if (reply != null) {
                        OnReturnValue<String> completion = value -> {
                            try { reply.onValue((T) new JSONObject().put("scope", scope).put("value", value)); }
                            catch (Exception e) { throw new AssertionError(e); }
                        };
                        setupReplies.add(completion); if (!holdSetup) completion.onValue("{}");
                    }
                    return;
                }
                if (method.equals("sdk.replayRoom")) {
                    replayStarts++; replayParameters = new JSONObject(Utils.toJson(args[1]));
                }
                if (reply != null) reply.onValue((T) new JSONObject().put("scope", scope).put("value",
                    method.equals("sdk.replayRoom") ? "{\"timeInfo\":{}}" : method.equals("sdk.joinRoom") ? "{\"state\":{},\"observerId\":1,\"isWritable\":false}" : "{}"));
            } catch (Exception e) { throw new AssertionError(e); }
        }
    }
    static class Result<T> implements Promise<T> {
        final CountDownLatch done = new CountDownLatch(1);
        T value; SDKError error; int calls;
        public void then(T value) { this.value = value; calls++; done.countDown(); }
        public void catchEx(SDKError error) { this.error = error; calls++; done.countDown(); }
        void await() throws Exception { assertTrue(done.await(3, TimeUnit.SECONDS)); }
    }
    View view;
    void main(Runnable task) { InstrumentationRegistry.getInstrumentation().runOnMainSync(task); }
    void prepare() { main(() -> view = new View(ApplicationProvider.getApplicationContext())); }
    WhiteSdk facade(String app, boolean multi) {
        WhiteSdkConfiguration config = new WhiteSdkConfiguration(app); config.setUseMultiViews(multi);
        return new WhiteSdk(view, ApplicationProvider.getApplicationContext(), config);
    }
    Result<Player> player(WhiteSdk sdk) {
        Result<Player> result = new Result<>();
        main(() -> sdk.createPlayer(new PlayerConfiguration("unit-room", "mock"), result)); return result;
    }
    void leave(WhiteSdk sdk) throws Exception {
        Result<Object> result = new Result<>(); main(() -> sdk.leaveRoom(result)); result.await(); assertNull(result.error);
    }
    @After public void cleanup() { if (view != null) main(() -> view.destroy()); }

    @Test public void facadeConstructedDuringRoomAppliesItsConfigurationBeforeReplay() throws Exception {
        prepare(); final WhiteSdk[] sdk = new WhiteSdk[2];
        main(() -> sdk[0] = facade("app-A", false));
        Result<Room> joined = new Result<>();
        main(() -> sdk[0].joinRoom(new RoomParams("unit-room", "mock", "unit-uid"), joined));
        joined.await(); assertNull(joined.error);
        main(() -> sdk[1] = facade("app-B", true));
        assertEquals("app-A", view.configuration.getString("appIdentifier")); leave(sdk[0]);
        Result<Player> replay = player(sdk[1]); replay.await(); assertNull(replay.error);
        assertEquals("app-B", view.configuration.getString("appIdentifier")); assertTrue(view.configuration.getBoolean("useMultiViews"));
        assertEquals(1, view.replayStarts); leave(sdk[1]);
    }
    @Test public void earlierIdleFacadeReappliesItsConfigurationAndRestoreArguments() throws Exception {
        prepare(); final WhiteSdk[] sdk = new WhiteSdk[2];
        main(() -> { sdk[0] = facade("app-A", false); sdk[1] = facade("app-B", true); });
        Result<Player> replay = player(sdk[0]); replay.await(); assertNull(replay.error);
        assertEquals("app-A", view.configuration.getString("appIdentifier")); assertFalse(view.configuration.getBoolean("useMultiViews"));
        java.lang.reflect.Field args = LifecycleHost.class.getDeclaredField("sdkInitArgs"); args.setAccessible(true);
        JSONObject restored = new JSONObject(Utils.toJson(((Object[]) args.get(view.lifecycle))[0]));
        assertEquals("app-A", restored.getString("appIdentifier")); leave(sdk[0]);
    }
    @Test public void replayWaitsForSetupKeepsBusyAndSnapshotsParameters() throws Exception {
        prepare(); final WhiteSdk[] sdk = new WhiteSdk[1]; main(() -> { sdk[0] = facade("app-A", false); view.holdSetup = true; });
        Result<Player> first = new Result<>(); PlayerConfiguration parameters = new PlayerConfiguration("original-room", "mock");
        main(() -> sdk[0].createPlayer(parameters, first));
        assertEquals(0, first.calls); assertEquals(0, view.replayStarts);
        Result<Player> second = player(sdk[0]); second.await(); assertEquals("Busy", second.error.getMessage());
        parameters.setRoom("mutated-room");
        main(() -> view.setupReplies.get(0).onValue("{}")); first.await(); assertNull(first.error);
        assertEquals("original-room", view.replayParameters.getString("room")); assertEquals(1, view.replayStarts); leave(sdk[0]);
    }
    @Test public void setupErrorCompletesOnceWithoutReplayAndExplicitLeaveAllowsRetry() throws Exception {
        prepare(); final WhiteSdk[] sdk = new WhiteSdk[1]; main(() -> { sdk[0] = facade("app-A", false); view.holdSetup = true; });
        Result<Player> first = player(sdk[0]);
        main(() -> view.setupReplies.get(0).onValue("{\"__error\":{\"message\":\"SetupRejected\"}}"));
        first.await(); assertEquals("SetupRejected", first.error.getMessage()); assertEquals(0, view.replayStarts);
        leave(sdk[0]); main(() -> { view.holdSetup = false; view.setupReplies.get(0).onValue("{}"); });
        assertEquals(1, first.calls); assertEquals(0, view.replayStarts);
        Result<Player> retry = player(sdk[0]); retry.await(); assertNull(retry.error); assertEquals(1, view.replayStarts); leave(sdk[0]);
    }
    @Test public void cancelledSetupReplyCannotStartOldReplayOrAffectNewGeneration() throws Exception {
        prepare(); final WhiteSdk[] sdk = new WhiteSdk[2]; main(() -> { sdk[0] = facade("app-A", false); view.holdSetup = true; });
        Result<Player> first = player(sdk[0]); leave(sdk[0]); first.await(); assertEquals("Cancelled", first.error.getMessage());
        main(() -> { view.holdSetup = false; sdk[1] = facade("app-B", true); });
        Result<Player> retry = player(sdk[1]); retry.await(); assertNull(retry.error);
        main(() -> view.setupReplies.get(0).onValue("{}"));
        assertEquals(1, first.calls); assertEquals(1, view.replayStarts); assertEquals("app-B", view.configuration.getString("appIdentifier")); leave(sdk[1]);
    }
}
