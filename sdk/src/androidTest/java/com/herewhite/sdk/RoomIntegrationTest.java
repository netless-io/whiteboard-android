package com.herewhite.sdk;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import androidx.test.platform.app.InstrumentationRegistry;

import com.herewhite.sdk.domain.Appliance;
import com.herewhite.sdk.domain.AppState;
import com.herewhite.sdk.domain.MemberState;
import com.herewhite.sdk.domain.Promise;
import com.herewhite.sdk.domain.RoomPhase;
import com.herewhite.sdk.domain.RoomState;
import com.herewhite.sdk.domain.SDKError;
import com.herewhite.sdk.domain.Scene;
import com.herewhite.sdk.domain.SlidePageState;
import com.herewhite.sdk.domain.WindowAppParam;
import com.herewhite.sdk.domain.WindowDocsEvent;

import org.junit.Ignore;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class RoomIntegrationTest extends WhiteSdkIntegrationTestHost {
    private static final String SLIDE_PREFIX = "https://white-cover.oss-cn-hangzhou.aliyuncs.com/flat/dynamicConvert";
    private static final String SLIDE_TASK_ID = "46e8ff5db5714fec818f5594a6c55083";

    @Test
    public void closeFocusedApp_triggersFocusChangedRoomState() throws Throwable {
        CountDownLatch connectedLatch = new CountDownLatch(1);
        CountDownLatch secondFocusLatch = new CountDownLatch(1);
        CountDownLatch closeAppStateLatch = new CountDownLatch(1);
        AtomicReference<String> firstAppIdRef = new AtomicReference<>();
        AtomicReference<String> secondAppIdRef = new AtomicReference<>();
        AtomicReference<String> expectedFocusRef = new AtomicReference<>();
        AtomicReference<String> focusedAfterCloseRef = new AtomicReference<>();
        AtomicReference<AppState> appStateAfterCloseRef = new AtomicReference<>();
        AtomicBoolean waitingForCloseFocusRef = new AtomicBoolean(false);

        RoomListener roomListener = new RoomListener() {
            @Override
            public void onPhaseChanged(RoomPhase phase) {
                if (phase == RoomPhase.connected) {
                    connectedLatch.countDown();
                }
            }

            @Override
            public void onDisconnectWithError(Exception e) {
            }

            @Override
            public void onKickedWithReason(String reason) {
            }

            @Override
            public void onRoomStateChanged(RoomState modifyState) {
                AppState appState = modifyState == null ? null : modifyState.getAppState();
                String focusedId = appState == null ? null : appState.getFocusedId();
                if (appState != null && waitingForCloseFocusRef.get()) {
                    appStateAfterCloseRef.set(appState);
                    String secondAppId = secondAppIdRef.get();
                    if (secondAppId != null
                            && !secondAppId.equals(focusedId)
                            && !contains(appState.getAppIds(), secondAppId)) {
                        focusedAfterCloseRef.set(focusedId);
                        closeAppStateLatch.countDown();
                    }
                }
                if (focusedId == null || expectedFocusRef.get() == null) {
                    return;
                }
                if (focusedId.equals(expectedFocusRef.get())) {
                    if (focusedId.equals(secondAppIdRef.get())) {
                        secondFocusLatch.countDown();
                    }
                }
            }

            @Override
            public void onCanUndoStepsUpdate(long canUndoSteps) {
            }

            @Override
            public void onCanRedoStepsUpdate(long canRedoSteps) {
            }

            @Override
            public void onCatchErrorWhenAppendFrame(long userId, Exception error) {
            }
        };

        Room room = joinRoom(roomListener, true);
        if (!connectedLatch.await(20, TimeUnit.SECONDS)) {
            throw new AssertionError("Room did not reach connected phase");
        }

        String firstAppId = addDocsViewerApp(room, "/close-focus-first-" + System.currentTimeMillis(), "Close Focus First");
        String secondAppId = addDocsViewerApp(room, "/close-focus-second-" + System.currentTimeMillis(), "Close Focus Second");
        firstAppIdRef.set(firstAppId);
        secondAppIdRef.set(secondAppId);

        expectedFocusRef.set(secondAppId);
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> room.focusApp(secondAppId));
        if (!secondFocusLatch.await(5, TimeUnit.SECONDS)
                && !secondAppId.equals(queryRoomState(room).getAppState().getFocusedId())) {
            throw new AssertionError("Timed out waiting for second app focus");
        }

        waitingForCloseFocusRef.set(true);
        closeApp(room, secondAppId);
        if (!closeAppStateLatch.await(20, TimeUnit.SECONDS)) {
            AppState appState = appStateAfterCloseRef.get();
            String focusedId = appState == null ? null : appState.getFocusedId();
            throw new AssertionError("Timed out waiting for appState after closeApp, focusedId=" + focusedId);
        }

        AppState appStateAfterClose = appStateAfterCloseRef.get();
        assertNotNull(appStateAfterClose);
        assertEquals(false, contains(appStateAfterClose.getAppIds(), secondAppId));
        assertEquals(false, secondAppId.equals(focusedAfterCloseRef.get()));
    }

    @Test
    public void addSlideApp_querySlidePageState() throws Throwable {
        CountDownLatch connectedLatch = new CountDownLatch(1);
        AtomicReference<String> slideAppIdRef = new AtomicReference<>();
        AtomicReference<SlidePageState> queriedStateRef = new AtomicReference<>();

        RoomListener roomListener = new RoomListener() {
            @Override
            public void onPhaseChanged(RoomPhase phase) {
                if (phase == RoomPhase.connected) {
                    connectedLatch.countDown();
                }
            }

            @Override
            public void onDisconnectWithError(Exception e) {
            }

            @Override
            public void onKickedWithReason(String reason) {
            }

            @Override
            public void onRoomStateChanged(RoomState modifyState) {
            }

            @Override
            public void onCanUndoStepsUpdate(long canUndoSteps) {
            }

            @Override
            public void onCanRedoStepsUpdate(long canRedoSteps) {
            }

            @Override
            public void onCatchErrorWhenAppendFrame(long userId, Exception error) {
            }
        };

        Room room = joinRoom(roomListener, true);
        if (!connectedLatch.await(20, TimeUnit.SECONDS)) {
            throw new AssertionError("Room did not reach connected phase");
        }

        CountDownLatch addAppLatch = new CountDownLatch(1);
        AtomicReference<SDKError> addAppErrorRef = new AtomicReference<>();
        WindowAppParam slideApp = WindowAppParam.createSlideApp(SLIDE_TASK_ID, SLIDE_PREFIX, "Android Slide Integration");

        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> room.addApp(slideApp, new Promise<String>() {
            @Override
            public void then(String appId) {
                slideAppIdRef.set(appId);
                addAppLatch.countDown();
            }

            @Override
            public void catchEx(SDKError t) {
                addAppErrorRef.set(t);
                addAppLatch.countDown();
            }
        }));
        if (!addAppLatch.await(20, TimeUnit.SECONDS)) {
            throw new AssertionError("Timed out waiting for add Slide App");
        }
        if (addAppErrorRef.get() != null) {
            throw new AssertionError("Add Slide App failed: " + addAppErrorRef.get().getMessage());
        }
        assertNotNull(slideAppIdRef.get());

        waitForSlidePageStateReady(room, slideAppIdRef.get(), queriedStateRef);
        int initialPage = queriedStateRef.get().getPage();
        int pageCount = queriedStateRef.get().getPageCount();
        assertSlidePageState(queriedStateRef.get(), slideAppIdRef.get(), initialPage, pageCount);
    }

    @Test
    public void setWindowBoxState_triggersRoomStateChangedCallback() throws Throwable {
        CountDownLatch connectedLatch = new CountDownLatch(1);
        CountDownLatch normalStateChangedLatch = new CountDownLatch(1);
        CountDownLatch boxStateChangedLatch = new CountDownLatch(1);
        AtomicReference<String> expectedBoxStateRef = new AtomicReference<>("normal");
        AtomicReference<RoomState> roomStateRef = new AtomicReference<>();

        RoomListener roomListener = new RoomListener() {
            @Override
            public void onPhaseChanged(RoomPhase phase) {
                if (phase == RoomPhase.connected) {
                    connectedLatch.countDown();
                }
            }

            @Override
            public void onDisconnectWithError(Exception e) {
            }

            @Override
            public void onKickedWithReason(String reason) {
            }

            @Override
            public void onRoomStateChanged(RoomState modifyState) {
                String windowBoxState = modifyState == null ? null : modifyState.getWindowBoxState();
                if (expectedBoxStateRef.get().equals(windowBoxState)) {
                    roomStateRef.set(modifyState);
                    if ("normal".equals(windowBoxState)) {
                        normalStateChangedLatch.countDown();
                    }
                    if ("maximized".equals(windowBoxState)) {
                        boxStateChangedLatch.countDown();
                    }
                }
            }

            @Override
            public void onCanUndoStepsUpdate(long canUndoSteps) {
            }

            @Override
            public void onCanRedoStepsUpdate(long canRedoSteps) {
            }

            @Override
            public void onCatchErrorWhenAppendFrame(long userId, Exception error) {
            }
        };

        Room room = joinRoom(roomListener, true);
        if (!connectedLatch.await(20, TimeUnit.SECONDS)) {
            throw new AssertionError("Room did not reach connected phase");
        }

        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> room.setWindowBoxState("normal"));
        normalStateChangedLatch.await(5, TimeUnit.SECONDS);

        expectedBoxStateRef.set("maximized");
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> room.setWindowBoxState("maximized"));

        if (!boxStateChangedLatch.await(20, TimeUnit.SECONDS)) {
            throw new AssertionError("Timed out waiting for windowBoxState in onRoomStateChanged");
        }

        RoomState roomState = roomStateRef.get();
        assertNotNull(roomState);
        assertEquals("maximized", roomState.getWindowBoxState());
    }

    @Test
    public void setMemberState_triggersRoomStateChangedCallback() throws Throwable {
        CountDownLatch connectedLatch = new CountDownLatch(1);
        CountDownLatch memberStateChangedLatch = new CountDownLatch(1);
        AtomicReference<RoomState> roomStateRef = new AtomicReference<>();
        AtomicReference<String> expectedApplianceRef = new AtomicReference<>(Appliance.TEXT);

        RoomListener roomListener = new RoomListener() {
            @Override
            public void onPhaseChanged(RoomPhase phase) {
                if (phase == RoomPhase.connected) {
                    connectedLatch.countDown();
                }
            }

            @Override
            public void onDisconnectWithError(Exception e) {
            }

            @Override
            public void onKickedWithReason(String reason) {
            }

            @Override
            public void onRoomStateChanged(RoomState modifyState) {
                MemberState changedMemberState = modifyState == null ? null : modifyState.getMemberState();
                if (changedMemberState != null
                        && expectedApplianceRef.get().equals(changedMemberState.getCurrentApplianceName())) {
                    roomStateRef.set(modifyState);
                    memberStateChangedLatch.countDown();
                }
            }

            @Override
            public void onCanUndoStepsUpdate(long canUndoSteps) {
            }

            @Override
            public void onCanRedoStepsUpdate(long canRedoSteps) {
            }

            @Override
            public void onCatchErrorWhenAppendFrame(long userId, Exception error) {
            }
        };

        Room room = joinRoom(roomListener);
        if (!connectedLatch.await(20, TimeUnit.SECONDS)) {
            throw new AssertionError("Room did not reach connected phase");
        }

        MemberState memberState = new MemberState();
        memberState.setCurrentApplianceName(Appliance.TEXT);
        memberState.setStrokeColor(new int[]{12, 34, 56});
        memberState.setStrokeWidth(6d);

        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> room.setMemberState(memberState));

        if (!memberStateChangedLatch.await(20, TimeUnit.SECONDS)) {
            throw new AssertionError("Timed out waiting for onRoomStateChanged");
        }

        RoomState roomState = roomStateRef.get();
        assertNotNull(roomState);
        MemberState updatedState = roomState.getMemberState();
        assertNotNull(updatedState);
        assertEquals(Appliance.TEXT, updatedState.getCurrentApplianceName());
        assertArrayEquals(new int[]{12, 34, 56}, updatedState.getStrokeColor());
        assertEquals(6d, updatedState.getStrokeWidth(), 0.001d);
    }

    private String addDocsViewerApp(Room room, String scenePath, String title) throws InterruptedException {
        CountDownLatch addAppLatch = new CountDownLatch(1);
        AtomicReference<String> appIdRef = new AtomicReference<>();
        AtomicReference<SDKError> errorRef = new AtomicReference<>();
        WindowAppParam appParam = WindowAppParam.createDocsViewerApp(scenePath, new Scene[]{new Scene("page1"), new Scene("page2")}, title);
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> room.addApp(appParam, new Promise<String>() {
            @Override
            public void then(String appId) {
                appIdRef.set(appId);
                addAppLatch.countDown();
            }

            @Override
            public void catchEx(SDKError t) {
                errorRef.set(t);
                addAppLatch.countDown();
            }
        }));
        if (!addAppLatch.await(20, TimeUnit.SECONDS)) {
            throw new AssertionError("Timed out waiting for add DocsViewer App");
        }
        if (errorRef.get() != null) {
            throw new AssertionError("Add DocsViewer App failed: " + errorRef.get().getMessage());
        }
        assertNotNull(appIdRef.get());
        return appIdRef.get();
    }

    private void closeApp(Room room, String appId) throws InterruptedException {
        CountDownLatch closeAppLatch = new CountDownLatch(1);
        AtomicReference<SDKError> errorRef = new AtomicReference<>();
        AtomicReference<Boolean> successRef = new AtomicReference<>();
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> room.closeApp(appId, new Promise<Boolean>() {
            @Override
            public void then(Boolean success) {
                successRef.set(success);
                closeAppLatch.countDown();
            }

            @Override
            public void catchEx(SDKError t) {
                errorRef.set(t);
                closeAppLatch.countDown();
            }
        }));
        if (!closeAppLatch.await(20, TimeUnit.SECONDS)) {
            throw new AssertionError("Timed out waiting for closeApp");
        }
        if (errorRef.get() != null) {
            throw new AssertionError("closeApp failed: " + errorRef.get().getMessage());
        }
        assertEquals(Boolean.TRUE, successRef.get());
    }

    private void waitForSlidePageStateReady(
            Room room,
            String appId,
            AtomicReference<SlidePageState> stateRef
    ) throws InterruptedException {
        AssertionError lastError = null;
        for (int i = 0; i < 12; i++) {
            try {
                querySlidePageState(room, appId, stateRef);
                SlidePageState state = stateRef.get();
                assertNotNull(state);
                assertEquals(appId, state.getAppId());
                if (state.getPage() > 0 && state.getPageCount() > 0) {
                    return;
                }
                lastError = new AssertionError("Slide page state is not ready");
            } catch (AssertionError error) {
                lastError = error;
            }
            Thread.sleep(500L);
        }
        throw lastError == null ? new AssertionError("Timed out waiting for Slide page state") : lastError;
    }

    private void querySlidePageState(Room room, String appId, AtomicReference<SlidePageState> stateRef) throws InterruptedException {
        CountDownLatch queryLatch = new CountDownLatch(1);
        AtomicReference<SDKError> errorRef = new AtomicReference<>();
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> room.querySlidePageState(appId, new Promise<SlidePageState>() {
            @Override
            public void then(SlidePageState slidePageState) {
                stateRef.set(slidePageState);
                queryLatch.countDown();
            }

            @Override
            public void catchEx(SDKError t) {
                errorRef.set(t);
                queryLatch.countDown();
            }
        }));
        if (!queryLatch.await(20, TimeUnit.SECONDS)) {
            throw new AssertionError("Timed out waiting for querySlidePageState");
        }
        if (errorRef.get() != null) {
            throw new AssertionError("querySlidePageState failed: " + errorRef.get().getMessage());
        }
    }

    private RoomState queryRoomState(Room room) throws InterruptedException {
        CountDownLatch queryLatch = new CountDownLatch(1);
        AtomicReference<RoomState> roomStateRef = new AtomicReference<>();
        AtomicReference<SDKError> errorRef = new AtomicReference<>();
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> room.getRoomState(new Promise<RoomState>() {
            @Override
            public void then(RoomState roomState) {
                roomStateRef.set(roomState);
                queryLatch.countDown();
            }

            @Override
            public void catchEx(SDKError t) {
                errorRef.set(t);
                queryLatch.countDown();
            }
        }));
        if (!queryLatch.await(20, TimeUnit.SECONDS)) {
            throw new AssertionError("Timed out waiting for getRoomState");
        }
        if (errorRef.get() != null) {
            throw new AssertionError("getRoomState failed: " + errorRef.get().getMessage());
        }
        assertNotNull(roomStateRef.get());
        return roomStateRef.get();
    }

    private void assertSlidePageState(SlidePageState state, String appId, int page, int pageCount) {
        assertNotNull(state);
        assertEquals(appId, state.getAppId());
        assertEquals(page, state.getPage());
        assertEquals(pageCount, state.getPageCount());
    }

    private boolean contains(String[] values, String expected) {
        if (values == null || expected == null) {
            return false;
        }
        for (String value : values) {
            if (expected.equals(value)) {
                return true;
            }
        }
        return false;
    }
}
