(function () {
    if (window.__backgroundProbe) return true;
    if (!window.__lifecycleProbe) return false;
    var probe = window.__backgroundProbe = { events: [], disconnectCalls: 0 };
    function event(e) {
        var value = { type: e.type, at: Date.now(), trusted: e.isTrusted,
            visibility: document.visibilityState, persisted: e.persisted === true };
        probe.events.push(value);
        // A separate test namespace keeps the event visible even if the production
        // pagehide hook invalidates the old SDK scope or the document goes away.
        window.bridge.call('backgroundProbe.event', value);
    }
    ['pagehide', 'pageshow', 'freeze', 'resume'].forEach(function (type) {
        window.addEventListener(type, event);
    });
    document.addEventListener('visibilitychange', event);
    probe.watchRoom = function () {
        if (probe.originalRoom) return false;
        probe.originalRoom = window.room;
        var disconnect = window.room.disconnect;
        window.room.disconnect = function () {
            probe.disconnectCalls++;
            return disconnect.apply(this, arguments);
        };
        return true;
    };
    probe.snapshot = function () {
        var transport = window.__lifecycleProbe.snapshot();
        return { visibility: document.visibilityState, events: probe.events.slice(),
            disconnectCalls: probe.disconnectCalls, sameRoom: window.room === probe.originalRoom,
            phase: window.room && window.room.phase, transport: transport };
    };
    return true;
})()
