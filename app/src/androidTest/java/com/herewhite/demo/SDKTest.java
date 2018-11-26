package com.herewhite.demo;

import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewAction;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;
import android.view.View;

import com.herewhite.sdk.AbstractRoomCallbacks;
import com.herewhite.sdk.Environment;
import com.herewhite.sdk.Room;
import com.herewhite.sdk.RoomParams;
import com.herewhite.sdk.WhiteBroadView;
import com.herewhite.sdk.WhiteSdk;
import com.herewhite.sdk.WhiteSdkConfiguration;
import com.herewhite.sdk.domain.BroadcastState;
import com.herewhite.sdk.domain.DeviceType;
import com.herewhite.sdk.domain.GlobalState;
import com.herewhite.sdk.domain.MemberState;
import com.herewhite.sdk.domain.Promise;
import com.herewhite.sdk.domain.RoomState;
import com.herewhite.sdk.domain.SDKError;
import com.herewhite.sdk.domain.ViewMode;

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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by buhe on 2018/8/16.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class SDKTest {

    String UUID = "60b930cbf869408aaebee7a2d051b1df";
    String ROOM_TOKEN = "WHITEcGFydG5lcl9pZD1QNnR4cXJEQlZrZmJNZWRUdGVLenBURXRnZzhjbGZ6ZnZteUQmc2lnPTA3Nzk1YjY5NDU2ZTE2ZjNjODNiMzFkNjMxZTQ0N2YzZWJjNjk3MzU6YWRtaW5JZD0xJnJvb21JZD02MGI5MzBjYmY4Njk0MDhhYWViZWU3YTJkMDUxYjFkZiZ0ZWFtSWQ9MSZleHBpcmVfdGltZT0xNTcyMTg5NzQ0JmFrPVA2dHhxckRCVmtmYk1lZFR0ZUt6cFRFdGdnOGNsZnpmdm15RCZjcmVhdGVfdGltZT0xNTQwNjMyNzkyJm5vbmNlPTE1NDA2MzI3OTI0NTQwMCZyb2xlPXB1Ymxpc2hlcg";

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
                        public void catchEx(SDKError t) {

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
    public void testJoinRoomThrowErrorOnCallback() {
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
                            throw new RuntimeException("I am a runtime exception");
                        }

                        @Override
                        public void catchEx(SDKError t) {

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
                        public void catchEx(SDKError t) {
                            assertNotNull(t);
                            Log.i("white", t.getMessage() + t.getJsStack());
                            lock.lock();
                            waitRoom.signal();
                            lock.unlock();
                            throw new RuntimeException("I am a runtime exception");
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

                            try {
                                Thread.sleep(1000); // FIXME
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                            room.getGlobalState(new Promise<GlobalState>() {
                                @Override
                                public void then(GlobalState globalState) {
                                    assertEquals("set currentSceneIndex", 1, globalState.getCurrentSceneIndex());
                                    lock.lock();
                                    waitRoom.signal();
                                    lock.unlock();
                                }

                                @Override
                                public void catchEx(SDKError t) {

                                }
                            });


                        }

                        @Override
                        public void catchEx(SDKError t) {

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
    public void testSetViewMode() {
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
                            room.setViewMode(ViewMode.broadcaster);


                            try {
                                Thread.sleep(1000); // FIXME
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                            room.getBroadcastState(new Promise<BroadcastState>() {
                                @Override
                                public void then(BroadcastState broadcastState) {
                                    assertEquals("set view mode ", ViewMode.broadcaster, broadcastState.getMode());
                                    lock.lock();
                                    waitRoom.signal();
                                    lock.unlock();
                                }

                                @Override
                                public void catchEx(SDKError t) {

                                }
                            });


                        }

                        @Override
                        public void catchEx(SDKError t) {

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
    public void testSetStrokeColor() {
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
                            MemberState memberState = new MemberState();
                            memberState.setStrokeColor(new int[]{255, 255, 6});
                            room.setMemberState(memberState);


                            try {
                                Thread.sleep(1000); // FIXME
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                            room.getMemberState(new Promise<MemberState>() {
                                @Override
                                public void then(MemberState memberState) {
                                    assertEquals("set StrokeColor ", new int[]{255, 255, 6}[0], memberState.getStrokeColor()[0]);
                                    lock.lock();
                                    waitRoom.signal();
                                    lock.unlock();
                                }

                                @Override
                                public void catchEx(SDKError t) {

                                }
                            });


                        }

                        @Override
                        public void catchEx(SDKError t) {

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
    public void testAddCallBack() {
        // Type text and then press the button.
        final Lock lock = new ReentrantLock();
        lock.lock();
        try {
            final Condition waitRoom = lock.newCondition();
            onView(withId(R.id.white)).perform(new SDKViewAction() {
                @Override
                public void perform(UiController uiController, View view) {
                    WhiteSdk whiteSdk = new WhiteSdk((WhiteBroadView) view, view.getContext(), new WhiteSdkConfiguration(DeviceType.touch, 10d, 0.1d));
                    whiteSdk.addRoomCallbacks(new AbstractRoomCallbacks() {
                        @Override
                        public void onRoomStateChanged(RoomState modifyState) {
                            assertEquals("onRoomStateChanged", modifyState.getMemberState().getCurrentApplianceName(), "rectangle");
                            lock.lock();
                            waitRoom.signal();
                            lock.unlock();
                        }
                    });
                    whiteSdk.joinRoom(new RoomParams(UUID, ROOM_TOKEN), new Promise<Room>() {
                        @Override
                        public void then(Room room) {
                            assertNotNull(room);
                            Log.i("white", "room create");
                            MemberState memberState = new MemberState();
                            memberState.setCurrentApplianceName("rectangle");
                            room.setMemberState(memberState);

                        }

                        @Override
                        public void catchEx(SDKError t) {

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


