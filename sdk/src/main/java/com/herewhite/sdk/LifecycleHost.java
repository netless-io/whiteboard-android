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
import java.util.Iterator;
import java.lang.ref.WeakReference;
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
    private boolean destroyed;
    private boolean reloading;
    private boolean navigationInvalid;
    private boolean recoveringNavigation;
    private long documentEpoch;
    private String retiredDocument;
    private Runnable reloadDocument;
    private Object[] sdkInitArgs;
    private Setup setup;
    private static final class Setup {
        final String key;
        final List<Pending> waiters = new ArrayList<>();
        boolean completed;
        SDKError error;
        Setup(Object[] args) { key = Utils.toJson(args); }
    }
    interface Pending { void fail(SDKError error); }
    interface Start { void run(long generation); }
    private Pending pending;
    private final List<Runnable> ready = new ArrayList<>();
    private List<OnReturnValue<String>> closing;
    private String closeResult;
    // Coordinate live Views without keeping abandoned Views/Activities alive.
    private static final Map<String, WeakReference<LifecycleHost>> owners = new HashMap<>();
    private static final Map<String, Replacement> replacements = new HashMap<>();
    private Replacement replacement;
    private String identity;
    private Runnable joinDeadline, leaveDeadline;
    private static LifecycleHost owner(String key) {
        Iterator<Map.Entry<String, WeakReference<LifecycleHost>>> entries = owners.entrySet().iterator();
        while (entries.hasNext()) {
            LifecycleHost host = entries.next().getValue().get();
            if (host == null || host.destroyed) entries.remove();
        }
        WeakReference<LifecycleHost> reference = owners.get(key);
        return reference == null ? null : reference.get();
    }
    private void releaseOwner() {
        WeakReference<LifecycleHost> reference = identity == null ? null : owners.get(identity);
        if (reference != null && reference.get() == this) owners.remove(identity);
        identity = null;
    }
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
            if (error != null) fail.fail(error); else { owners.put(key, new WeakReference<>(host)); host.identity = key; start.run(generation); }
        }
    }

    LifecycleHost(JsBridgeInterface raw) { this.raw = raw; }
    void setDocumentRecovery(Runnable action) { reloadDocument = action; }
    private void recoverNavigation() {
        if (!navigationInvalid || destroyed) return;
        navigationInvalid = false; invalid = false; documentEpoch++;
        releaseOwner(); retiredDocument = documentId; documentId = null;
        generation++; state = "idle"; closeResult = null; closing = null; setup = null; ready.clear();
        reloading = true; recoveringNavigation = true;
        if (reloadDocument != null) reloadDocument.run();
    }
    synchronized void claim() {
        if (initialized) return;
        initialized = true;
        handshake(false);
    }
    void configure(Object[] args, Runnable bind) {
        configure(args, bind, null);
    }
    void configure(Object[] args, Runnable bind, Pending completion) {
        recoverNavigation();
        sdkInitArgs = args;
        if ("idle".equals(state) && replacement == null) { bind.run(); startSetup(args, completion); }
    }
    private void startSetup(Object[] args, Pending completion) {
        if (invalid) { if (completion != null) completion.fail(new SDKError("HostInvalid")); return; }
        String key = Utils.toJson(args);
        if (setup != null && setup.key.equals(key)) {
            if (completion != null) {
                if (setup.completed) completion.fail(setup.error); else setup.waiters.add(completion);
            }
            return;
        }
        Setup previous = setup;
        Setup attempt = new Setup(args);
        setup = attempt;
        if (completion != null) attempt.waiters.add(completion);
        if (previous != null) finishSetup(previous, new SDKError("Cancelled"));
        // A cancellation callback may synchronously install a different SDK.
        if (setup != attempt || invalid) return;
        send(0, "sdk.newWhiteSdk", args, (OnReturnValue<String>) value -> {
            Runnable finish = () -> { if (setup == attempt && !invalid) finishSetup(attempt, SDKError.promiseError(value)); };
            if (Looper.myLooper() == Looper.getMainLooper()) finish.run(); else handler.post(finish);
        });
    }
    private void finishSetup(Setup attempt, SDKError error) {
        if (attempt.completed) return;
        attempt.completed = true; attempt.error = error;
        List<Pending> callbacks = new ArrayList<>(attempt.waiters); attempt.waiters.clear();
        for (Pending callback : callbacks) {
            try { callback.fail(error); }
            catch (RuntimeException exception) { com.herewhite.sdk.internal.Logger.error("SDK preparation callback failed", exception); }
        }
    }
    Object setupToken() { return setup; }
    boolean isSetupCurrent(Object token) { return !invalid && token != null && setup == token && setup.completed && setup.error == null; }
    void observePreload(Object[] args, OnReturnValue<String> completion) { send(0, "sdk.observePreload", args, completion); }
    /** An explicit readiness wait only. Construction/background warming has no new deadline. */
    void prepare(Object[] args, Runnable bind, long timeoutMs, Promise<Void> promise) {
        handler.post(() -> {
            if (promise == null) return;
            recoverNavigation();
            if (timeoutMs <= 0) { promise.catchEx(new SDKError("InvalidPreparationOptions")); return; }
            if (invalid) { promise.catchEx(new SDKError("HostInvalid")); return; }
            if (setup == null || !setup.key.equals(Utils.toJson(args))) {
                if (!"idle".equals(state) || replacement != null) { promise.catchEx(new SDKError("Busy")); return; }
                configure(args, bind);
            }
            final boolean[] settled = {false};
            Pending done = error -> {
                if (settled[0]) return;
                settled[0] = true;
                if (error == null) promise.then(null); else promise.catchEx(error);
            };
            Setup attempt = setup;
            final Pending[] waiter = new Pending[1];
            Runnable deadline = () -> { attempt.waiters.remove(waiter[0]); done.fail(new SDKError("PrepareTimeout")); };
            waiter[0] = error -> { handler.removeCallbacks(deadline); done.fail(error); };
            handler.postDelayed(deadline, timeoutMs);
            startSetup(args, waiter[0]);
        });
    }
    void cancelPreparation() {
        handler.post(() -> {
            Setup previous = setup; setup = null;
            if (previous != null) finishSetup(previous, new SDKError("Cancelled"));
        });
    }
    void replace(String key, Object[] args, long timeout, Runnable bind, Start start, Pending fail) {
        handler.post(() -> {
            recoverNavigation();
            Replacement request = new Replacement(this, key, args, timeout, bind, start, fail);
            Replacement previous = replacements.put(key, request);
            Replacement local = replacement;
            replacement = request;
            if (previous != null) previous.cancel();
            if (local != null && local != previous) local.cancel();
            if (!request.current()) return;
            if (invalid) { request.finish(new SDKError("HostInvalid")); return; }
            // Close this host as well when switching identity/room on the same View.
            final long localGeneration = generation;
            leave(localGeneration, value -> {
                if (!request.current()) return;
                SDKError error = SDKError.promiseError(value);
                if (error != null) continueAfterFailedLeave(localGeneration, value);
                OnReturnValue<String> closed = result -> {
                    if (!request.current()) return;
                    // One creation budget covers readiness, setup and join after both owners leave.
                    request.generation = begin(request::finish, request.timeout);
                    if (request.generation < 0) { request.finish(new SDKError(rejection())); return; }
                    owner(key); // Sweep collected entries before registering another identity.
                    owners.put(key, new WeakReference<>(this)); identity = key;
                    request.bind.run();
                    sdkInitArgs = request.sdkArgs;
                    whenReady(() -> {
                        if (!request.current() || !current(request.generation)) return;
                        startSetup(request.sdkArgs, setupError -> {
                            if (!request.current() || !current(request.generation)) return;
                            if (setupError != null) finish(request.generation, false);
                            request.finish(setupError);
                        });
                    });
                };
                // The target owner may have switched identity while this host was closing.
                LifecycleHost old = owner(key);
                if (old != null && old != this) {
                    final long oldGeneration = old.generation;
                    old.leave(oldGeneration, result -> {
                        if (!request.current()) return;
                        if (SDKError.promiseError(result) != null) old.continueAfterFailedLeave(oldGeneration, result);
                        closed.onValue("{}");
                    });
                }
                else closed.onValue("{}");
            });
        });
    }
    private void handshake(boolean restoreSdk) {
        final long epoch = documentEpoch;
        raw.callHandler("sdk.getLifecycle", (OnReturnValue<JSONObject>) result -> {
            List<Runnable> work;
            synchronized (this) {
                if (invalid || epoch != documentEpoch || documentId != null) return;
                try {
                    JSONObject envelope = result instanceof JSONObject ? result : new JSONObject(String.valueOf(result));
                    JSONObject value = new JSONObject(envelope.getString("value"));
                    String nextDocument = value.getString("documentId");
                    if (nextDocument.equals(retiredDocument)) return;
                    documentId = nextDocument; reloading = false; recoveringNavigation = false;
                } catch (Exception error) { invalidate(); return; }
                work = new ArrayList<>(ready); ready.clear();
            }
            if (restoreSdk && sdkInitArgs != null) startSetup(sdkInitArgs, null);
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
        if (timeout != null) {
            joinDeadline = () -> {
            Pending fail;
            synchronized (this) {
                if (generation != admitted || pending == null || !"joining".equals(state)) return;
                state = "closing"; fail = pending; pending = null;
            }
            // The original request's timeout result wins over the leave's cancellation.
            fail.fail(new SDKError("JoinTimeout"));
            leave(admitted, null);
            };
            handler.postDelayed(joinDeadline, timeout);
        }
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
        cancelJoinDeadline();
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
    private void cancelJoinDeadline() {
        if (joinDeadline != null) handler.removeCallbacks(joinDeadline);
        joinDeadline = null;
    }
    private synchronized void continueAfterFailedLeave(long target, String result) {
        if (target != generation) return;
        SDKError failure = SDKError.promiseError(result);
        String message = failure == null ? "LeaveFailed" : failure.getMessage();
        com.herewhite.sdk.internal.Logger.error("Previous room leave failed; continuing replacement: " + message, null);
        try { send(target, "sdk.continueAfterLeaveFailure", new Object[]{message}, null); }
        catch (RuntimeException error) { com.herewhite.sdk.internal.Logger.error("Failed to notify old room logger", error); }
        releaseOwner();
        cancelJoinDeadline();
        if (leaveDeadline != null) handler.removeCallbacks(leaveDeadline);
        leaveDeadline = null;
        generation++; // Fence replies and events from the retired room before any new creation.
        closeResult = null;
        state = invalid ? "blocked" : "idle";
    }
    synchronized void leave(long target, OnReturnValue<String> callback) {
        if (destroyed) { if (callback != null) callback.onValue("{\"closed\":true}"); return; }
        if (invalid || target != generation) { if (callback != null) callback.onValue(error("StaleScope")); return; }
        if (closeResult != null) { if (callback != null) callback.onValue(closeResult); return; }
        if (closing != null) { if (callback != null) closing.add(callback); return; }
        if ("idle".equals(state)) { if (callback != null) callback.onValue("{\"closed\":true}"); return; }
        state = "closing";
        cancelJoinDeadline();
        closing = new ArrayList<>(); if (callback != null) closing.add(callback);
        Pending cancel = pending; pending = null;
        if (cancel != null) cancel.fail(new SDKError("Cancelled"));
        leaveDeadline = () -> finishLeave(target, error("LeaveTimeout"));
        handler.postDelayed(leaveDeadline, 20000);
        try { send(target, "sdk.leaveRoom", null, (OnReturnValue<String>) value -> finishLeave(target, value)); }
        catch (RuntimeException error) {
            com.herewhite.sdk.internal.Logger.error("Previous room leave threw", error);
            finishLeave(target, error("LeaveFailed"));
        }
    }
    private synchronized void finishLeave(long target, String value) {
        if (target != generation || closeResult != null) return;
        if (leaveDeadline != null) handler.removeCallbacks(leaveDeadline);
        leaveDeadline = null;
        closeResult = value;
        state = SDKError.promiseError(value) == null ? "idle" : "blocked";
        if ("idle".equals(state)) releaseOwner();
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
    public void documentNavigating(Object envelope) {
        final long epoch = documentEpoch;
        if (unwrap(envelope, false) != REJECTED) handler.post(() -> { if (epoch == documentEpoch) prepareReload(); });
    }
    synchronized void documentUnavailable() {
        if (destroyed || invalid || recoveringNavigation) return;
        navigationInvalid = true;
        invalidate();
    }
    public synchronized void pageStarted() { if (documentId != null) prepareReload(); }
    synchronized void prepareReload() {
        if (documentId == null || invalid) return;
        if (!"idle".equals(state) || (generation > 0 && (closeResult == null || SDKError.promiseError(closeResult) != null))) {
            // Navigation may destroy JS before it replies. Retire this document, not the View.
            navigationInvalid = true;
            try { send(generation, "sdk.leaveRoom", null, (OnReturnValue<String>) value -> {
                SDKError failure = SDKError.promiseError(value);
                if (failure != null) com.herewhite.sdk.internal.Logger.error("Room close during navigation: " + failure.getMessage(), null);
            }); } catch (RuntimeException error) { com.herewhite.sdk.internal.Logger.error("Room close during navigation", error); }
            com.herewhite.sdk.internal.Logger.error("Room document navigated; closure unconfirmed, next join will restore the SDK page", null);
            invalidate(); return;
        }
        retiredDocument = documentId; documentId = null; documentEpoch++;
        generation++; ready.clear(); closeResult = null; closing = null; reloading = true;
        Setup previous = setup; setup = null;
        if (previous != null) finishSetup(previous, new SDKError("Cancelled"));
    }
    synchronized void pageFinished() {
        if (!reloading || invalid) return;
        handshake(true);
    }
    synchronized long documentEpoch() { return documentEpoch; }
    synchronized void pageFinished(long epoch, boolean sdkAvailable) {
        if (epoch != documentEpoch || destroyed || invalid) return;
        if (sdkAvailable) pageFinished();
        else documentUnavailable();
    }
    public synchronized void invalidate() {
        if (invalid) return;
        invalid = true; state = "blocked"; ready.clear();
        documentEpoch++;
        cancelJoinDeadline();
        if (leaveDeadline != null) handler.removeCallbacks(leaveDeadline);
        leaveDeadline = null;
        if (setup != null) finishSetup(setup, new SDKError("HostInvalid"));
        if (replacement != null) replacement.finish(new SDKError("HostInvalid"));
        Pending cancel = pending; pending = null; if (cancel != null) cancel.fail(new SDKError("HostInvalid"));
        if (closing != null) { List<OnReturnValue<String>> callbacks = closing; closing = null;
            for (OnReturnValue<String> callback : callbacks) callback.onValue(error("HostInvalid")); }
    }
    /** Called only after WebView destruction or a confirmed renderer exit. */
    public synchronized void destroy() {
        if (destroyed) return;
        destroyed = true;
        releaseOwner();
        List<OnReturnValue<String>> callbacks = closing; closing = null;
        invalidate();
        closeResult = "{\"closed\":true}";
        if (callbacks != null) for (OnReturnValue<String> callback : callbacks) callback.onValue(closeResult);
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
