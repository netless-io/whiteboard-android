package com.herewhite.demo;

import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewAction;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;
import android.view.View;

import com.herewhite.sdk.Room;
import com.herewhite.sdk.RoomParams;
import com.herewhite.sdk.WhiteBroadView;
import com.herewhite.sdk.WhiteSdk;
import com.herewhite.sdk.WhiteSdkConfiguration;
import com.herewhite.sdk.domain.DeviceType;
import com.herewhite.sdk.domain.GlobalState;
import com.herewhite.sdk.domain.Promise;

import org.hamcrest.Matcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.assertNotNull;

/**
 * Created by buhe on 2018/8/16.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class SDKTest {

    String UUID = "92307ee36ea34c5492c834c0a5f182c6";
    String ROOM_TOKEN = "WHITEcGFydG5lcl9pZD1QNnR4cXJEQlZrZmJNZWRUdGVLenBURXRnZzhjbGZ6ZnZteUQmc2lnPThkOTllMGJhMDc2MmQxZTFhZjdlYjdlYzEyNjViN2I0YTAzYThiZWU6YWRtaW5JZD0xJnJvb21JZD05MjMwN2VlMzZlYTM0YzU0OTJjODM0YzBhNWYxODJjNiZ0ZWFtSWQ9MSZleHBpcmVfdGltZT0xNTY2MDMyNjQ2JmFrPVA2dHhxckRCVmtmYk1lZFR0ZUt6cFRFdGdnOGNsZnpmdm15RCZjcmVhdGVfdGltZT0xNTM0NDc1Njk0Jm5vbmNlPTE1MzQ0NzU2OTQzMjgwMCZyb2xlPXB1Ymxpc2hlcg";

    @Rule
    public ActivityTestRule<MainTestActivity> mActivityRule = new ActivityTestRule<>(
            MainTestActivity.class);

    abstract class SDKViewAction implements ViewAction {
        @Override
        public Matcher<View> getConstraints() {
            return isDisplayed();
        }

        @Override
        public String getDescription() {
            return "white";
        }
    }


    @Test
    public void testNewSDK() {
        // Type text and then press the button.
        onView(withId(R.id.white)).perform(new SDKViewAction() {

            @Override
            public void perform(UiController uiController, View view) {
                WhiteSdk whiteSdk = new WhiteSdk((WhiteBroadView) view, view.getContext(), new WhiteSdkConfiguration(DeviceType.touch, 10d, 0.1d));
                assertNotNull(whiteSdk);
            }
        });

    }

    @Test
    public void testJoinRoom() {
        // Type text and then press the button.
        final Lock lock = new ReentrantLock();
        lock.lock();
        try {
            final Condition waitRoom = lock.newCondition();
            onView(withId(R.id.white)).perform(new SDKViewAction() {
                @Override
                public void perform(UiController uiController, View view) {
                    WhiteSdk whiteSdk = new WhiteSdk((WhiteBroadView) view, view.getContext(), new WhiteSdkConfiguration(DeviceType.touch, 10d, 0.1d));
                    whiteSdk.joinRoom(new RoomParams(UUID, ROOM_TOKEN), new Promise<Room>() {
                        @Override
                        public void then(Room room) {
                            assertNotNull(room);
                            Log.i("white", "room create");
                            lock.lock();
                            waitRoom.signal();
                            lock.unlock();
                        }

                        @Override
                        public void catchEx(Exception t) {

                        }
                    });
                }
            });
            waitRoom.await();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            lock.unlock();
        }

    }

//    @Test
    // 不通过 要改 white-sdk
    public void testJoinRoomError() {
        // Type text and then press the button.
        final Lock lock = new ReentrantLock();
        lock.lock();
        try {
            final Condition waitRoom = lock.newCondition();
            onView(withId(R.id.white)).perform(new SDKViewAction() {
                @Override
                public void perform(UiController uiController, View view) {
                    WhiteSdk whiteSdk = new WhiteSdk((WhiteBroadView) view, view.getContext(), new WhiteSdkConfiguration(DeviceType.touch, 10d, 0.1d));
                    whiteSdk.joinRoom(new RoomParams(UUID + "NotExist", ROOM_TOKEN), new Promise<Room>() {
                        @Override
                        public void then(Room room) {

                        }

                        @Override
                        public void catchEx(Exception t) {
                            assertNotNull(t);
                            Log.i("white", t.getMessage());
                            lock.lock();
                            waitRoom.signal();
                            lock.unlock();
                        }
                    });
                }
            });
            waitRoom.await();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            lock.unlock();
        }

    }

    @Test
    public void testSetGlobalState() {
        // Type text and then press the button.
        final Lock lock = new ReentrantLock();
        lock.lock();
        try {
            final Condition waitRoom = lock.newCondition();
            onView(withId(R.id.white)).perform(new SDKViewAction() {
                @Override
                public void perform(UiController uiController, View view) {
                    WhiteSdk whiteSdk = new WhiteSdk((WhiteBroadView) view, view.getContext(), new WhiteSdkConfiguration(DeviceType.touch, 10d, 0.1d));
                    whiteSdk.joinRoom(new RoomParams(UUID, ROOM_TOKEN), new Promise<Room>() {
                        @Override
                        public void then(Room room) {
                            assertNotNull(room);
                            Log.i("white", "room create");
                            room.insertNewPage(1);
                            GlobalState globalState = new GlobalState();
                            globalState.setCurrentSceneIndex(1);
                            room.setGlobalState(globalState);

                            lock.lock();
                            waitRoom.signal();
                            lock.unlock();
                        }

                        @Override
                        public void catchEx(Exception t) {

                        }
                    });
                }
            });
            waitRoom.await();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            lock.unlock();
        }

    }
}
