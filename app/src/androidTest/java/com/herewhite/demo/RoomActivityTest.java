package com.herewhite.demo;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onIdle;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static com.herewhite.demo.TestUtils.downToUp;
import static com.herewhite.demo.TestUtils.waitFor;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import android.view.MenuItem;
import android.view.View;

import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.IdlingResource;
import androidx.test.espresso.UiController;
import androidx.test.espresso.idling.CountingIdlingResource;
import androidx.test.rule.ActivityTestRule;

import com.herewhite.demo.utils.SimpleViewAction;
import com.herewhite.sdk.AbstractRoomCallbacks;
import com.herewhite.sdk.domain.AnimationMode;
import com.herewhite.sdk.domain.Appliance;
import com.herewhite.sdk.domain.CameraConfig;
import com.herewhite.sdk.domain.GlobalState;
import com.herewhite.sdk.domain.MemberState;
import com.herewhite.sdk.domain.Promise;
import com.herewhite.sdk.domain.RoomPhase;
import com.herewhite.sdk.domain.RoomState;
import com.herewhite.sdk.domain.SDKError;
import com.herewhite.sdk.domain.Scene;
import com.herewhite.sdk.domain.SceneState;
import com.herewhite.sdk.domain.ViewMode;
import com.herewhite.sdk.domain.WhiteDisplayerState;

import org.junit.After;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

public class RoomActivityTest {
    public static final int GRID_NUM = 10;
    public static final double GRID_SIZE = 200d;
    private final String PUT_TEST_DIR = "/test";    @Rule
    public ActivityTestRule<RoomActivity> activityRule = new ActivityTestRule<RoomActivity>(RoomActivity.class) {
        @Override
        protected void afterActivityLaunched() {
            mActivity = activityRule.getActivity();

            mIdlingResource = mActivity.getIdlingResource();
            IdlingRegistry.getInstance().register(mIdlingResource);
        }
    };
    private final String PUT_TEST_PAGE = "page";
    private final String PUT_TEST_PATH = PUT_TEST_DIR + "/" + PUT_TEST_PAGE;
    private final String PUT_TEST_PAGE_TARGET = "pagetarget";
    private final String PUT_TEST_PATH_TARGET = PUT_TEST_DIR + "/" + PUT_TEST_PAGE_TARGET;
    SceneState currentSceneState = null;
    String errorMessage;
    private RoomActivity mActivity;
    private IdlingResource mIdlingResource;

    static MenuItemTitleMatcher withTitle(String title) {
        return new MenuItemTitleMatcher(title);
    }

    /**
     * 使用onView方式无法绕过列表过长问题
     */
    @Test
    public void menuItemClickTest() {
        onView(withId(R.id.white)).check(matches(isDisplayed()));

        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
        onData(allOf(is(instanceOf(MenuItem.class)), withTitle(mActivity.getString(R.string.reconnect)))).perform(click());
        // onView(anyOf(withId(R.id.reconnect), withText(R.string.reconnect))).perform(click());

        // openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
        // onView(anyOf(withId(R.id.orientation), withText(R.string.orientation))).perform(click());
        // openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
        // onView(anyOf(withId(R.id.orientation), withText(R.string.orientation))).perform(click());

        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
        onData(allOf(is(instanceOf(MenuItem.class)), withTitle(mActivity.getString(R.string.broadcast_command)))).perform(click());

        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
        onData(allOf(is(instanceOf(MenuItem.class)), withTitle(mActivity.getString(R.string.scalePptToFit_command)))).perform(click());

        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
        onData(allOf(is(instanceOf(MenuItem.class)), withTitle(mActivity.getString(R.string.getBroadcastState_command)))).perform(click());

        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
        onData(allOf(is(instanceOf(MenuItem.class)), withTitle(mActivity.getString(R.string.move_camera)))).perform(click());

        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
        onData(allOf(is(instanceOf(MenuItem.class)), withTitle(mActivity.getString(R.string.redo_undo)))).perform(click());

        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
        onData(allOf(is(instanceOf(MenuItem.class)), withTitle(mActivity.getString(R.string.duplicate)))).perform(click());

        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
        onData(allOf(is(instanceOf(MenuItem.class)), withTitle(mActivity.getString(R.string.copy_paste)))).perform(click());

        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
        onData(allOf(is(instanceOf(MenuItem.class)), withTitle(mActivity.getString(R.string.deleteOperation)))).perform(click());

        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
        onData(allOf(is(instanceOf(MenuItem.class)), withTitle(mActivity.getString(R.string.moveRectangle)))).perform(click());

        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
        onData(allOf(is(instanceOf(MenuItem.class)), withTitle(mActivity.getString(R.string.dispatchCustomEvent_command)))).perform(click());

        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
        onData(allOf(is(instanceOf(MenuItem.class)), withTitle(mActivity.getString(R.string.cleanScene_command)))).perform(click());

        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
        onData(allOf(is(instanceOf(MenuItem.class)), withTitle(mActivity.getString(R.string.insertNewScene_command)))).perform(click());

        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
        onData(allOf(is(instanceOf(MenuItem.class)), withTitle(mActivity.getString(R.string.insert_ppt_command)))).perform(click());

        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
        onData(allOf(is(instanceOf(MenuItem.class)), withTitle(mActivity.getString(R.string.next_scene)))).perform(click());

        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
        onData(allOf(is(instanceOf(MenuItem.class)), withTitle(mActivity.getString(R.string.staticConvert)))).perform(click());

        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
        onData(allOf(is(instanceOf(MenuItem.class)), withTitle(mActivity.getString(R.string.dynamicConvert)))).perform(click());

        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
        onData(allOf(is(instanceOf(MenuItem.class)), withTitle(mActivity.getString(R.string.insertImage_command)))).perform(click());

        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
        onData(allOf(is(instanceOf(MenuItem.class)), withTitle(mActivity.getString(R.string.getPreview_command)))).perform(click());

        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
        onData(allOf(is(instanceOf(MenuItem.class)), withTitle(mActivity.getString(R.string.getSceneImage_command)))).perform(click());

        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
        onData(allOf(is(instanceOf(MenuItem.class)), withTitle(mActivity.getString(R.string.getScene_command)))).perform(click());

        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
        onData(allOf(is(instanceOf(MenuItem.class)), withTitle(mActivity.getString(R.string.getRoomPhase_command)))).perform(click());

        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
        onData(allOf(is(instanceOf(MenuItem.class)), withTitle(mActivity.getString(R.string.getRoomState_command)))).perform(click());

        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
        onData(allOf(is(instanceOf(MenuItem.class)), withTitle(mActivity.getString(R.string.disconnect_command)))).perform(click());

        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
        onData(allOf(is(instanceOf(MenuItem.class)), withTitle(mActivity.getString(R.string.writable_false_command)))).perform(click());

        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
        onData(allOf(is(instanceOf(MenuItem.class)), withTitle(mActivity.getString(R.string.writable_true_command)))).perform(click());

        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
        onData(allOf(is(instanceOf(MenuItem.class)), withTitle(mActivity.getString(R.string.disable_command)))).perform(click());

        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
        onData(allOf(is(instanceOf(MenuItem.class)), withTitle(mActivity.getString(R.string.cancelDisable_command)))).perform(click());

        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
        onData(allOf(is(instanceOf(MenuItem.class)), withTitle(mActivity.getString(R.string.textarea_command)))).perform(click());

        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
        onData(allOf(is(instanceOf(MenuItem.class)), withTitle(mActivity.getString(R.string.selector_command)))).perform(click());

        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
        onData(allOf(is(instanceOf(MenuItem.class)), withTitle(mActivity.getString(R.string.pencil_command)))).perform(click());

        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
        onData(allOf(is(instanceOf(MenuItem.class)), withTitle(mActivity.getString(R.string.rectangle_command)))).perform(click());

        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
        onData(allOf(is(instanceOf(MenuItem.class)), withTitle(mActivity.getString(R.string.color_command)))).perform(click());

        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
        onData(allOf(is(instanceOf(MenuItem.class)), withTitle(mActivity.getString(R.string.convertPoint_command)))).perform(click());

        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
        onData(allOf(is(instanceOf(MenuItem.class)), withTitle(mActivity.getString(R.string.externalEvent_command)))).perform(click());

        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
        onData(allOf(is(instanceOf(MenuItem.class)), withTitle(mActivity.getString(R.string.zoomChange_command)))).perform(click());
    }

    @Test
    public void testSetAndGet_MemberState() {
        onIdle();

        MemberState state = new MemberState();
        state.setStrokeColor(new int[]{99, 99, 99});
        state.setCurrentApplianceName(Appliance.TEXT);
        state.setStrokeWidth(10);
        state.setTextSize(10);

        mActivity.mRoom.setMemberState(state);
        mActivity.mRoom.getMemberState(new Promise<MemberState>() {
            @Override
            public void then(MemberState memberState) {
                assertEquals(state.getCurrentApplianceName(), memberState.getCurrentApplianceName());
                assertArrayEquals(state.getStrokeColor(), memberState.getStrokeColor());
                assertEquals(state.getStrokeWidth(), memberState.getStrokeWidth(), Constants.DOUBLE_DELTA);
                assertEquals(state.getTextSize(), memberState.getTextSize(), Constants.DOUBLE_DELTA);
            }

            @Override
            public void catchEx(SDKError t) {

            }
        });
    }

    @Test
    public void testSetAndGet_GlobalState() {
        onView(withId(R.id.white)).check(matches(isDisplayed()));

        LocalGlobalState testState = new LocalGlobalState();
        mActivity.mRoom.setGlobalState(testState);

        WhiteDisplayerState.setCustomGlobalStateClass(LocalGlobalState.class);
        LocalGlobalState globalState = (LocalGlobalState) mActivity.mRoom.getGlobalState();
        assertTrue(globalState.globalInt == testState.globalInt);

        mActivity.mRoom.getGlobalState(new Promise<GlobalState>() {
            @Override
            public void then(GlobalState globalState) {
                LocalGlobalState globalState2 = (LocalGlobalState) mActivity.mRoom.getGlobalState();
                assertTrue(globalState2.globalInt == testState.globalInt);
                assertEquals(testState.globalString, globalState2.globalString);
            }

            @Override
            public void catchEx(SDKError t) {

            }
        });
    }

    @Test
    public void testSetAndGet_Writable() {
        onView(withId(R.id.white)).check(matches(isDisplayed()));

        mActivity.mRoom.setWritable(true, new Promise<Boolean>() {
            @Override
            public void then(Boolean writeable) {
                assertTrue(writeable);
                assertTrue(mActivity.mRoom.getWritable());
            }

            @Override
            public void catchEx(SDKError t) {

            }
        });
    }

    @Test
    public void testDrawLine() {
        onView(withId(R.id.white)).check(matches(isDisplayed()));

        onView(withId(R.id.white)).perform(downToUp(100, 120, 300, 300));
        onView(isRoot()).perform(waitFor(500));

        onView(withId(R.id.white)).perform(downToUp(300, 400, 500, 600));
        onView(isRoot()).perform(waitFor(500));
    }

    @Test
    @Ignore
    public void testDrawLineNet() {
        onView(withId(R.id.white)).check(matches(isDisplayed()));

        MemberState memberState = mActivity.mRoom.getMemberState();
        memberState.setStrokeColor(new int[]{0xEF, 0x3A, 0x48});
        mActivity.mRoom.setMemberState(memberState);

        drawVLines();

        drawHLines();
    }

    private void drawVLines() {
        int centerX = mActivity.mWhiteboardView.getWidth() / 2;

        CameraConfig config = new CameraConfig();
        config.setScale(0.1);
        config.setAnimationMode(AnimationMode.Immediately);

        for (int i = -GRID_NUM; i <= GRID_NUM; i++) {
            config.setCenterX(i * GRID_SIZE);
            config.setCenterY(0d);
            mActivity.mRoom.moveCamera(config);

            onView(isRoot()).perform(waitFor(500));
            onView(withId(R.id.white)).perform(downToUp(centerX, 0, centerX, mActivity.mWhiteboardView.getHeight()));
        }
    }

    private void drawHLines() {
        int centerY = mActivity.mWhiteboardView.getHeight() / 2;

        CameraConfig config = new CameraConfig();
        config.setScale(0.1);
        config.setAnimationMode(AnimationMode.Immediately);

        for (int i = -GRID_NUM; i <= GRID_NUM; i++) {
            config.setCenterX(0d);
            config.setCenterY(i * GRID_SIZE);
            mActivity.mRoom.moveCamera(config);

            onView(isRoot()).perform(waitFor(500));
            onView(withId(R.id.white)).perform(downToUp(0, centerY, mActivity.mWhiteboardView.getWidth(), centerY));
        }
    }

    @Test
    public void undoAndRedo() {
        onView(withId(R.id.white)).check(matches(isDisplayed()));

        FakeRoomCallbacks fakeRoomCallbacks = new FakeRoomCallbacks();
        mActivity.mRoom.disableSerialization(false);
        mActivity.mRoomCallbackHock = fakeRoomCallbacks;

        // action
        onView(withId(R.id.white)).perform(downToUp(100, 120, 300, 300));
        onView(isRoot()).perform(waitFor(500));

        onView(withId(R.id.white)).perform(downToUp(300, 400, 500, 600));
        onView(isRoot()).perform(waitFor(500));

        // undo test
        long lastUndoSteps = fakeRoomCallbacks.canUndoSteps;
        assertTrue(lastUndoSteps > 0);
        mActivity.mRoom.undo();
        onView(isRoot()).perform(waitFor(500)); // delay for sync
        assertEquals(lastUndoSteps - 1, fakeRoomCallbacks.canUndoSteps);

        // redo test
        long lastRedoSteps = fakeRoomCallbacks.canRedoSteps;
        assertTrue(lastRedoSteps > 0);
        mActivity.mRoom.redo();
        onView(isRoot()).perform(waitFor(500)); // delay for sync
        assertEquals(lastRedoSteps - 1, fakeRoomCallbacks.canRedoSteps);
    }

    @Test
    public void setViewMode() {
        FakeRoomCallbacks fakeRoomCallbacks = new FakeRoomCallbacks();
        mActivity.mRoomCallbackHock = fakeRoomCallbacks;
        fakeRoomCallbacks.registerIdle();

        onView(isRoot()).perform(new SimpleViewAction() {
            @Override
            public void perform(UiController uiController, View view) {
                fakeRoomCallbacks.start(FakeRoomCallbacks.MODIFY_STATE);
                mActivity.mRoom.setViewMode(ViewMode.Broadcaster);
            }
        });

        onView(isRoot()).perform(new SimpleViewAction() {
            @Override
            public void perform(UiController uiController, View view) {
                assertEquals(ViewMode.Broadcaster, mActivity.mRoom.getBroadcastState().getMode());
                fakeRoomCallbacks.unregisterIdle();
            }
        });
    }

    @Test
    public void disconnect_roomPhase_disconnected() {
        FakeRoomCallbacks fakeRoomCallbacks = new FakeRoomCallbacks();
        mActivity.mRoomCallbackHock = fakeRoomCallbacks;
        fakeRoomCallbacks.registerIdle();

        onIdle();
        assertEquals(RoomPhase.connected, fakeRoomCallbacks.roomPhase);

        fakeRoomCallbacks.start(FakeRoomCallbacks.ROOM_PHASE_DISCONNECTED);
        mActivity.mRoom.disconnect();

        onView(isRoot()).perform(new SimpleViewAction() {
            @Override
            public void perform(UiController uiController, View view) {
                assertEquals(RoomPhase.disconnected, fakeRoomCallbacks.roomPhase);
                fakeRoomCallbacks.unregisterIdle();
            }
        });
    }

    @Test
    public void setScenePath() {
        onIdle();

        CountingIdlingResource idlingResource = new CountingIdlingResource("getScenePath");
        idlingResource.increment();
        mActivity.mRoom.getSceneState(new Promise<SceneState>() {
            @Override
            public void then(SceneState sceneState) {
                currentSceneState = sceneState;
                idlingResource.decrement();
            }

            @Override
            public void catchEx(SDKError t) {
                idlingResource.decrement();
            }
        });
        IdlingRegistry.getInstance().register(idlingResource);

        SetScenePathResult result = new SetScenePathResult();
        result.register();
        onView(isRoot()).perform(new SimpleViewAction() {
            @Override
            public void perform(UiController uiController, View view) {
                IdlingRegistry.getInstance().unregister(idlingResource);
                if (currentSceneState == null) {
                    fail();
                }

                result.start();
                mActivity.mRoom.setScenePath(getNextScenePath(currentSceneState), result);
            }

            private String getNextScenePath(SceneState currentSceneState) {
                String dir = currentSceneState.getScenePath().substring(0, currentSceneState.getScenePath().lastIndexOf('/'));

                int index = currentSceneState.getIndex();
                Scene[] scenes = currentSceneState.getScenes();
                Scene tScene = scenes[(index + 1) % scenes.length];

                return dir + '/' + tScene.getName();
            }
        });

        onIdle(); // wait for idling

        onView(isRoot()).perform(new SimpleViewAction() {
            @Override
            public void perform(UiController uiController, View view) {
                assertEquals(true, result.success);
                result.unregister();
            }
        });
    }

    @Test
    public void setScenePath_not_a_scene() {
        onIdle();

        SetScenePathResult result = new SetScenePathResult();
        result.register();

        onView(isRoot()).perform(new SimpleViewAction() {
            @Override
            public void perform(UiController uiController, View view) {
                result.start();
                mActivity.mRoom.setScenePath("/invalid/dir/", result);
            }
        });

        onView(isRoot()).perform(new SimpleViewAction() {
            @Override
            public void perform(UiController uiController, View view) {
                assertEquals("/invalid/dir is not a scene", result.errorMessage);
                result.unregister();
            }
        });
    }

    @Test
    public void setScenePath_should_start_with_slash() {
        onIdle();

        SetScenePathResult result = new SetScenePathResult();
        result.register();

        onView(isRoot()).perform(new SimpleViewAction() {
            @Override
            public void perform(UiController uiController, View view) {
                result.start();
                mActivity.mRoom.setScenePath("invalid/path", result);
            }
        });

        onView(isRoot()).perform(new SimpleViewAction() {
            @Override
            public void perform(UiController uiController, View view) {
                assertEquals("path \"invalid/path\" should start with \"/\"", result.errorMessage);
                result.unregister();
            }
        });
    }

    @Test
    public void setScenePath_should_start_with_slash_CDL() {
        onIdle();

        CountDownLatch countDownLatch = new CountDownLatch(1);
        onView(isRoot()).perform(new SimpleViewAction() {
            @Override
            public void perform(UiController uiController, View view) {
                errorMessage = null;
                mActivity.mRoom.setScenePath("invalid/path", new Promise<Boolean>() {
                    @Override
                    public void then(Boolean aBoolean) {
                        countDownLatch.countDown();
                    }

                    @Override
                    public void catchEx(SDKError t) {
                        errorMessage = t.getMessage();
                        countDownLatch.countDown();
                    }
                });
            }
        });

        try {
            countDownLatch.await();
            assertEquals("path \"invalid/path\" should start with \"/\"", errorMessage);
        } catch (InterruptedException e) {
            fail();
        }
    }

    @Test
    public void putScenes() {
        onIdle();
        SceneStateRecover recover = new SceneStateRecover(mActivity.mRoom.getSceneState());

        CountDownLatch latch = new CountDownLatch(2);
        onIdle((Callable<Void>) () -> {
            mActivity.mRoom.putScenes(PUT_TEST_DIR, new Scene[]{new Scene(PUT_TEST_PAGE)}, 0);
            mActivity.mRoom.setScenePath(PUT_TEST_PATH, new SimpleLatchDownPromise<>(latch));
            mActivity.mRoom.getSceneState(new SimpleLatchDownPromise<>(latch));
            return null;
        });

        try {
            latch.await();
        } catch (InterruptedException e) {
            fail();
        }

        onIdle((Callable<Void>) () -> {
            assertEquals(PUT_TEST_PATH, mActivity.mRoom.getSceneState().getScenePath());
            mActivity.mRoom.removeScenes(PUT_TEST_DIR);
            return null;
        });

        recover.restore();
    }

    @Test
    public void moveScene() {
        onIdle();
        SceneStateRecover recover = new SceneStateRecover(mActivity.mRoom.getSceneState());

        CountDownLatch latch = new CountDownLatch(2);
        onIdle((Callable<Void>) () -> {
            mActivity.mRoom.putScenes(PUT_TEST_DIR, new Scene[]{new Scene(PUT_TEST_PAGE)}, 0);
            mActivity.mRoom.setScenePath(PUT_TEST_PATH, new SimpleLatchDownPromise<>(latch));
            mActivity.mRoom.moveScene(PUT_TEST_PATH, PUT_TEST_PATH_TARGET);
            mActivity.mRoom.getSceneState(new SimpleLatchDownPromise<>(latch));
            return null;
        });

        try {
            latch.await();
        } catch (InterruptedException e) {
            fail();
        }

        onIdle((Callable<Void>) () -> {
            assertEquals(PUT_TEST_PATH_TARGET, mActivity.mRoom.getSceneState().getScenePath());
            mActivity.mRoom.removeScenes(PUT_TEST_DIR);
            return null;
        });

        recover.restore();
    }

    @Test
    public void zoomChange() {
        onView(isRoot()).perform(new SimpleViewAction() {
            @Override
            public void perform(UiController uiController, View view) {
                CameraConfig config = new CameraConfig();
                config.setScale(2.0);
                config.setAnimationMode(AnimationMode.Immediately);
                mActivity.mRoom.moveCamera(config);
            }
        });

        onView(isRoot()).perform(waitFor(1000));
        assertEquals(2.0, mActivity.mRoom.getRoomState().getCameraState().getScale(), Constants.DOUBLE_DELTA);
    }

    @After
    public void unregisterIdlingResource() {
        if (mIdlingResource != null) {
            IdlingRegistry.getInstance().unregister(mIdlingResource);
        }
    }

    class LocalGlobalState extends GlobalState {
        private String globalString = "globalString";
        private int globalInt = 0x1234;
    }

    class FakeRoomCallbacks extends AbstractRoomCallbacks {
        static final String MODIFY_STATE = "modifyState";
        // 无法依赖 modifyState 确定 zoomChange 成功，not used
        static final String MODIFY_STATE_ZOOM = "modifyStateZoom";
        static final String ROOM_PHASE_CONNECTED = "roomPhaseConnected";
        static final String ROOM_PHASE_DISCONNECTED = "roomPhaseDisconnected";

        public long canUndoSteps;
        public long canRedoSteps;
        private RoomState modifyState;
        private RoomPhase roomPhase;

        private CountingIdlingResource idlingResource = new CountingIdlingResource("RoomCallbacks");
        private volatile String optKey;

        @Override
        public void onCanUndoStepsUpdate(long canUndoSteps) {
            this.canUndoSteps = canUndoSteps;
            super.onCanUndoStepsUpdate(canUndoSteps);
        }

        @Override
        public void onCanRedoStepsUpdate(long canRedoSteps) {
            this.canRedoSteps = canRedoSteps;
            super.onCanRedoStepsUpdate(canRedoSteps);
        }

        @Override
        public void onRoomStateChanged(RoomState modifyState) {
            this.modifyState = modifyState;
            if (modifyState.getCameraState() != null && modifyState.getCameraState().getScale() != null) {
                checkAndMark(MODIFY_STATE_ZOOM);
            }
            if (modifyState.getBroadcastState() != null) {
                checkAndMark(MODIFY_STATE);
            }
            super.onRoomStateChanged(modifyState);
        }

        @Override
        public void onPhaseChanged(RoomPhase phase) {
            this.roomPhase = phase;
            if (phase == RoomPhase.connected) {
                checkAndMark(ROOM_PHASE_CONNECTED);
            } else if (phase == RoomPhase.disconnected) {
                checkAndMark(ROOM_PHASE_DISCONNECTED);
            }
            super.onPhaseChanged(phase);
        }

        private void checkAndMark(String key) {
            if (key.equals(optKey) && !idlingResource.isIdleNow()) {
                idlingResource.decrement();
            }
        }

        public void registerIdle() {
            IdlingRegistry.getInstance().register(idlingResource);
        }

        public void unregisterIdle() {
            IdlingRegistry.getInstance().unregister(idlingResource);
        }

        public void start(String key) {
            idlingResource.increment();
            optKey = key;
        }
    }

    class SetScenePathResult implements Promise<Boolean> {
        CountingIdlingResource idlingResource = new CountingIdlingResource("SetScenePathResult");
        volatile String errorMessage;
        boolean success;

        SetScenePathResult() {

        }

        @Override
        public void then(Boolean success) {
            this.success = success;
            idlingResource.decrement();
        }

        @Override
        public void catchEx(SDKError t) {
            errorMessage = t.getMessage();
            idlingResource.decrement();
        }

        public void register() {
            IdlingRegistry.getInstance().register(idlingResource);
        }

        public void unregister() {
            IdlingRegistry.getInstance().unregister(idlingResource);
        }


        public void start() {
            idlingResource.increment();
        }
    }

    class SimpleLatchDownPromise<T> implements Promise<T> {
        private final CountDownLatch latch;

        public SimpleLatchDownPromise(CountDownLatch latch) {
            this.latch = latch;
        }

        @Override
        public void then(T t) {
            latch.countDown();
        }

        @Override
        public void catchEx(SDKError t) {
            latch.countDown();
        }
    }

    class SceneStateRecover {
        private final SceneState sceneState;

        public SceneStateRecover(SceneState sceneState) {
            this.sceneState = sceneState;
        }

        public void restore() {
            SetScenePathResult result = new SetScenePathResult();
            result.register();
            onIdle((Callable<Void>) () -> {
                result.start();
                mActivity.mRoom.setScenePath(sceneState.getScenePath(), result);
                return null;
            });
            onIdle((Callable<Void>) () -> {
                result.unregister();
                return null;
            });
        }
    }


}
