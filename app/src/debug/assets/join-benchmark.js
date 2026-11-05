(function () {
    "use strict";
    window.__wbInitBench = {
        mark: function (stage, detail) {
            var event = {
                sdk: "1.0",
                stage: stage,
                deviceEpochMs: Date.now(),
                performanceMs: performance.now()
            };
            if (detail) {
                event.appId = detail.appId;
                event.taskId = detail.taskId;
            }
            var json = JSON.stringify(event);
            // Capture the timestamp here, before crossing the WebView boundary.
            // A detached debug observer must not change an SDK method's outcome.
            try { window.__wbInitBenchNative.record(json); } catch (ignored) {}
            try { console.info("WB_INIT_BENCH " + json); } catch (ignored) {}
        }
    };
}());
