package com.herewhite.sdk;

import android.os.Handler;
import android.os.Looper;
import com.herewhite.sdk.domain.Promise;
import com.herewhite.sdk.domain.SDKError;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import wendu.dsbridge.special.OnReturnValue;

/** Internal owner of one actual WebView. Releasing listeners never releases this slot. */
public final class LifecycleHost {
    public static final Object REJECTED = new Object();
    private final JsBridgeInterface raw;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private String documentId;
    private long generation;
    private String state = "idle";
    private boolean initialized, invalid;
    private boolean reloading;
    private Object[] sdkInitArgs;
    interface Pending { void fail(SDKError error); }
    interface Start { void run(long generation); }
    private Pending pending;
    private final List<Runnable> ready = new ArrayList<>();
    private List<OnReturnValue<String>> closing;
    private String closeResult;
    // Process-local ownership survives release/detach and a replacement WebView.
    private static final Map<String, LifecycleHost> owners = new HashMap<>();
    private static final Map<String, Replacement> replacements = new HashMap<>();
    private Replacement replacement;
    private String identity;
    private static final class Replacement {
        final LifecycleHost host; final String key; final Start start; final Pending fail; final Object[] sdkArgs; final Runnable bind; final long timeout;
        long generation;
        boolean waiting = true;
        Replacement(LifecycleHost host, String key, Object[] sdkArgs, long timeout, Runnable bind, Start start, Pending fail) {
            this.host = host; this.key = key; this.sdkArgs = sdkArgs; this.timeout = timeout; this.bind = bind; this.start = start; this.fail = fail;
        }
        boolean current() { return waiting && host.replacement == this && replacements.get(key) == this; }
        void cancel() { if (waiting) { waiting = false; if (replacements.get(key) == this) replacements.remove(key); if (host.replacement == this) host.replacement = null; fail.fail(new SDKError("Cancelled")); } }
        void finish(SDKError error) {
            if (!current()) return;
            waiting = false; replacements.remove(key); host.replacement = null;
            if (error != null) fail.fail(error); else { owners.put(key, host); host.identity = key; start.run(generation); }
        }
    }

    LifecycleHost(JsBridgeInterface raw) { this.raw = raw; }
    synchronized void claim() {
        if (initialized) return;
        initialized = true;
        handshake(false);
    }
    void configure(Object[] args, Runnable bind) {
        sdkInitArgs = args;
        if ("idle".equals(state) && replacement == null) { bind.run(); send(0, "sdk.newWhiteSdk", args, null); }
    }
    void replace(String key, Object[] args, long timeout, Runnable bind, Start start, Pending fail) {
        handler.post(() -> {
            Replacement request = new Replacement(this, key, args, timeout, bind, start, fail);
            Replacement previous = replacements.put(key, request);
            Replacement local = replacement;
            replacement = request;
            if (previous != null) previous.cancel();
            if (local != null && local != previous) local.cancel();
            if (!request.current()) return;
            if (invalid) { request.finish(new SDKError("HostInvalid")); return; }
            // Close this host as well when switching identity/room on the same View.
            leave(generation, value -> {
                if (!request.current()) return;
                SDKError error = SDKError.promiseError(value);
                if (error != null) { request.finish(error); return; }
                OnReturnValue<String> closed = result -> {
                    if (!request.current()) return;
                    SDKError closeError = SDKError.promiseError(result);
                    if (closeError != null) { request.finish(closeError); return; }
                    // One creation budget covers readiness, setup and join after both owners leave.
                    request.generation = begin(request::finish, request.timeout);
                    if (request.generation < 0) { request.finish(new SDKError(rejection())); return; }
                    owners.put(key, this); identity = key;
                    request.bind.run();
                    sdkInitArgs = request.sdkArgs;
                    whenReady(() -> {
                        if (!request.current() || !current(request.generation)) return;
                        send(0, "sdk.newWhiteSdk", request.sdkArgs, (OnReturnValue<String>) setup -> {
                            if (!request.current() || !current(request.generation)) return;
                            SDKError setupError = SDKError.promiseError(setup);
                            if (setupError != null) finish(request.generation, false);
                            request.finish(setupError);
                        });
                    });
                };
                // The target owner may have switched identity while this host was closing.
                LifecycleHost old = owners.get(key);
                if (old != null && old != this) old.leave(old.generation, closed);
                else closed.onValue("{}");
            });
        });
    }
    private void handshake(boolean restoreSdk) {
        raw.callHandler("sdk.getLifecycle", (OnReturnValue<JSONObject>) result -> {
            List<Runnable> work;
            synchronized (this) {
                if (invalid) return;
                try {
                    JSONObject envelope = result instanceof JSONObject ? result : new JSONObject(String.valueOf(result));
                    JSONObject value = new JSONObject(envelope.getString("value"));
                    documentId = value.getString("documentId");
                } catch (Exception error) { invalidate(); return; }
                work = new ArrayList<>(ready); ready.clear();
            }
            if (restoreSdk && sdkInitArgs != null) send(0, "sdk.newWhiteSdk", sdkInitArgs, null);
            for (Runnable task : work) task.run();
        });
    }
    synchronized long admit(Pending onCancelled) {
        if (replacement != null) return -1;
        // Player creation is cancelled explicitly by the host, without a fixed deadline.
        return begin(onCancelled, null);
    }
    private synchronized long begin(Pending onCancelled, Long timeout) {
        if (invalid || !"idle".equals(state)) return -1;
        state = "joining"; generation++; pending = onCancelled; closing = null; closeResult = null;
        final long admitted = generation;
        if (timeout != null) handler.postDelayed(() -> {
            Pending fail;
            synchronized (this) {
                if (generation != admitted || pending == null || !"joining".equals(state)) return;
                state = "closing"; fail = pending; pending = null;
            }
            // The original request's timeout result wins over the leave's cancellation.
            fail.fail(new SDKError("JoinTimeout"));
            leave(admitted, null);
        }, timeout);
        return admitted;
    }
    synchronized boolean continueJoin(long target, Pending onCancelled) {
        if (!current(target) || pending == null) return false;
        pending = onCancelled;
        return true;
    }
    synchronized String rejection() { return invalid ? "HostInvalid" : "active".equals(state) ? "AlreadyJoined" : "Busy"; }
    synchronized boolean finish(long target, boolean success) {
        if (invalid || target != generation || pending == null) return false;
        pending = null;
        if (success) state = "active";
        else if (!"idle".equals(state) && !"closing".equals(state)) state = "blocked";
        return true;
    }
    synchronized boolean current(long target) { return !invalid && generation == target && ("joining".equals(state) || "active".equals(state)); }
    private synchronized Map<String,Object> scope(long target) {
        Map<String,Object> value = new HashMap<>(); value.put("documentId", documentId); value.put("generation", target); return value;
    }
    private void whenReady(Runnable work) {
        synchronized (this) { if (invalid) return; if (documentId == null) { ready.add(work); return; } }
        work.run();
    }
    @SuppressWarnings("unchecked")
    private <T> void send(long target, String method, Object[] args, OnReturnValue<T> callback) {
        whenReady(() -> {
            Map<String,Object> marker = new HashMap<>(); marker.put("__whiteScope", scope(target));
            Object[] scoped = new Object[(args == null ? 0 : args.length) + 1]; scoped[0] = new JSONObject(marker);
            if (args != null) System.arraycopy(args, 0, scoped, 1, args.length);
            OnReturnValue<Object> response = callback == null ? null : result -> {
                Object value = unwrap(result, false);
                if (value == REJECTED) { if (callback != null) callback.onValue((T) error("StaleScope")); return; }
                callback.onValue((T) value);
            };
            raw.callHandler(method, scoped, response);
        });
    }
    private static String error(String code) { return "{\"__error\":{\"message\":\"" + code + "\"}}"; }
    synchronized void leave(long target, OnReturnValue<String> callback) {
        if (invalid || target != generation) { if (callback != null) callback.onValue(error("StaleScope")); return; }
        if (closeResult != null) { if (callback != null) callback.onValue(closeResult); return; }
        if (closing != null) { if (callback != null) closing.add(callback); return; }
        if ("idle".equals(state)) { if (callback != null) callback.onValue("{\"closed\":true}"); return; }
        state = "closing";
        closing = new ArrayList<>(); if (callback != null) closing.add(callback);
        Pending cancel = pending; pending = null;
        if (cancel != null) cancel.fail(new SDKError("Cancelled"));
        handler.postDelayed(() -> finishLeave(target, error("LeaveTimeout")), 20000);
        send(target, "sdk.leaveRoom", null, (OnReturnValue<String>) value -> finishLeave(target, value));
    }
    private synchronized void finishLeave(long target, String value) {
        if (target != generation || closeResult != null) return;
        closeResult = value;
        state = SDKError.promiseError(value) == null ? "idle" : "blocked";
        if ("idle".equals(state) && identity != null && owners.get(identity) == this) owners.remove(identity);
        List<OnReturnValue<String>> callbacks = closing; closing = null;
        if (callbacks != null) for (OnReturnValue<String> callback : callbacks) callback.onValue(value);
    }
    public synchronized Object unwrap(Object input, boolean event) {
        try {
            JSONObject envelope = input instanceof JSONObject ? (JSONObject) input : new JSONObject(String.valueOf(input));
            JSONObject scope = envelope.getJSONObject("scope");
            if (invalid || !scope.getString("documentId").equals(documentId)) return REJECTED;
            long source = scope.getLong("generation");
            if (source != 0 && source != generation) return REJECTED;
            Object value = envelope.opt("value"); return value == JSONObject.NULL ? null : value;
        } catch (Exception error) { return REJECTED; }
    }
    public synchronized void stateEvent(Object input) {
        Object value = unwrap(input, false);
        if (value instanceof JSONObject && !invalid) {
            String next = ((JSONObject) value).optString("state", state);
            if ("blocked".equals(next) || ("idle".equals(next) && closeResult != null && SDKError.promiseError(closeResult) == null)) state = next;
        }
    }
    public synchronized void pageStarted() { if (documentId != null) invalidate(); }
    synchronized void prepareReload() {
        if (documentId == null || invalid) return;
        if (!"idle".equals(state) || (generation > 0 && (closeResult == null || SDKError.promiseError(closeResult) != null))) { invalidate(); return; }
        documentId = null; generation++; ready.clear(); closeResult = null; closing = null; reloading = true;
    }
    synchronized void pageFinished() {
        if (!reloading || invalid) return;
        reloading = false; handshake(true);
    }
    public synchronized void invalidate() {
        if (invalid) return;
        invalid = true; state = "blocked"; ready.clear();
        if (replacement != null) replacement.finish(new SDKError("HostInvalid"));
        Pending cancel = pending; pending = null; if (cancel != null) cancel.fail(new SDKError("HostInvalid"));
        if (closing != null) { List<OnReturnValue<String>> callbacks = closing; closing = null;
            for (OnReturnValue<String> callback : callbacks) callback.onValue(error("HostInvalid")); }
    }
    void leave(Promise<Object> promise) {
        handler.post(() -> {
            if (replacement != null) replacement.cancel();
            leave(generation, value -> {
                if (promise == null) return;
                SDKError error = SDKError.promiseError(value);
                if (error == null) promise.then(new Object()); else promise.catchEx(error);
            });
        });
    }
    <T> void callCurrent(String method, Object[] args, OnReturnValue<T> callback) {
        long target;
        synchronized (this) {
            if (invalid) { if (callback != null) callback.onValue((T) error("HostInvalid")); return; }
            if (method.equals("sdk.newWhiteSdk")) sdkInitArgs = args;
            target = method.startsWith("sdk.") && !method.equals("sdk.hasBackgroundImage") && !method.equals("sdk.reloadBackgroundImage") ? 0 : generation;
        }
        send(target, method, args, callback);
    }
    JsBridgeInterface bridge(long target) { return new ScopedBridge(target); }
    final class ScopedBridge implements JsBridgeInterface {
        private final long target;
        ScopedBridge(long target) { this.target = target; }
        boolean valid() { synchronized (LifecycleHost.this) { return !invalid && generation == target; } }
        public void addJavascriptObject(Object object, String namespace) { raw.addJavascriptObject(object, namespace); }
        public <T> void callHandler(String method, Object[] args, OnReturnValue<T> callback) {
            if (method.equals("room.disconnect") || method.equals("player.stop")) {
                leave(target, value -> { if (callback != null) callback.onValue((T) value); }); return;
            }
            if (!current(target)) { if (callback != null) callback.onValue((T) error("StaleScope")); return; }
            if (method.equals("sdk.newWhiteSdk")) sdkInitArgs = args;
            send(target, method, args, callback);
        }
        public <T> void callHandler(String method, OnReturnValue<T> callback) { callHandler(method, null, callback); }
        public void callHandler(String method, Object[] args) { callHandler(method, args, null); }
        public void evaluateJavascript(String script) {
            if (current(target)) raw.evaluateJavascript("window.__whiteRunScoped(" + Utils.toJson(scope(target)) + ",function(){" + script + "})");
        }
        public void callFocusView() { if (current(target)) raw.callFocusView(); }
    }
}
