package com.herewhite.demo;

import android.app.Activity;
import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewAction;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;
import android.view.View;

import com.google.gson.Gson;
import com.herewhite.sdk.Room;
import com.herewhite.sdk.RoomParams;
import com.herewhite.sdk.WhiteboardView;
import com.herewhite.sdk.WhiteSdk;
import com.herewhite.sdk.WhiteSdkConfiguration;
import com.herewhite.sdk.domain.AkkoEvent;
import com.herewhite.sdk.domain.Appliance;
import com.herewhite.sdk.domain.BroadcastState;
import com.herewhite.sdk.domain.DeviceType;
import com.herewhite.sdk.domain.EventEntry;
import com.herewhite.sdk.domain.EventListener;
import com.herewhite.sdk.domain.ImageInformationWithUrl;
import com.herewhite.sdk.domain.MemberState;
import com.herewhite.sdk.domain.PptPage;
import com.herewhite.sdk.domain.Promise;
import com.herewhite.sdk.domain.SDKError;
import com.herewhite.sdk.domain.Scene;
import com.herewhite.sdk.domain.ViewMode;

import org.hamcrest.Matcher;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.concurrent.CountDownLatch;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

/**
 * Created by buhe on 2018/8/16.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class SDKRoomTest {

    @Rule
    public ActivityTestRule<TestActivity> mActivityRule = new ActivityTestRule<>(
            TestActivity.class);
    private DemoAPI demoAPI = new DemoAPI();
    private Activity activity = mActivityRule.getActivity();
    private Gson gson = new Gson();
    private Room mRoom;
    private WhiteSdk whiteSdk;
    final long TIME_OUT = 15000;

    @Before
    public void setup() {
        logTestAction("setup");
        final CountDownLatch latch = new CountDownLatch(1);

        demoAPI.getRoom(new DemoAPI.Result() {
            @Override
            public void fail(String message) {
                Assert.fail(message);
                latch.countDown();
            }

            @Override
            public void success(final String uuid, final String roomToken) {
                onView(withId(R.id.white)).perform(new SDKViewAction() {
                    @Override
                    public void perform(UiController uiController, View view) {
                        whiteSdk = new WhiteSdk((WhiteboardView) view, view.getContext(), new WhiteSdkConfiguration(DeviceType.touch, 10d, 0.1d));
                        whiteSdk.joinRoom(new RoomParams(uuid, roomToken), new Promise<Room>() {
                            @Override
                            public void then(Room room) {
                                Assert.assertNotNull(room);
                                mRoom = room;
                                Log.i("white", "room create");
                                latch.countDown();
                            }

                            @Override
                            public void catchEx(SDKError t) {
                                Assert.fail("加入房间失败：" + t.toString());
                                latch.countDown();
                            }
                        });
                    }
                });
            }
        });

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
            Assert.fail(e.toString());
        }
    }

    @Test(timeout=TIME_OUT)
    public void testSetMemberState() {
        final CountDownLatch latch = new CountDownLatch(1);
        final MemberState mMemberState = new MemberState();
        mMemberState.setStrokeColor(new int[]{200, 200, 200});
        mMemberState.setCurrentApplianceName(Appliance.RECTANGLE);
        mMemberState.setStrokeWidth(4);
        mMemberState.setTextSize(10);
        mRoom.setMemberState(mMemberState);
        mRoom.getMemberState(new Promise<MemberState>() {
            @Override
            public void then(MemberState memberState) {
                Assert.assertTrue("test appliance name", memberState.getCurrentApplianceName().equals(mMemberState.getCurrentApplianceName()));
                Assert.assertArrayEquals("test stroke color", memberState.getStrokeColor(), mMemberState.getStrokeColor());
                Assert.assertTrue("test stroke width",memberState.getStrokeWidth() == mMemberState.getStrokeWidth());
                Assert.assertTrue("test text size",memberState.getTextSize() == mMemberState.getTextSize());
                latch.countDown();
            }

            @Override
            public void catchEx(SDKError t) {
                Assert.fail("get MemberState fail" + t.getMessage());
                latch.countDown();
            }
        });

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
            assertFail(e.toString());
        }
    }

    @Test(timeout=TIME_OUT)
    public void testSetViewMode() {
        final CountDownLatch latch = new CountDownLatch(1);

        mRoom.setViewMode(ViewMode.Broadcaster);
        mRoom.getBroadcastState(new Promise<BroadcastState>() {
            @Override
            public void then(BroadcastState broadcastState) {
                Assert.assertEquals("set view mode ", ViewMode.Broadcaster, broadcastState.getMode());
                Assert.assertNotEquals("view mode", ViewMode.Freedom, broadcastState.getMode());
                latch.countDown();
            }

            @Override
            public void catchEx(SDKError t) {
                Assert.fail(t.getMessage());
                latch.countDown();
            }
        });

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
            assertFail(e.toString());
        }
    }

    private String imageUrl = "https://white-pan.oss-cn-shanghai.aliyuncs.com/101/image/Rectangle.png";
    @Test(timeout=TIME_OUT)
    public void testInsertImage() {
        ImageInformationWithUrl informationWithUrl = new ImageInformationWithUrl(0d, 0d, 151d, 226d, imageUrl);
        mRoom.insertImage(informationWithUrl);
        //TODO:通过图片拦截替换 API 测试
    }

    @Test(timeout=TIME_OUT)
    public void testNewScene() {
        Scene scene1 = new Scene("ppt1", new PptPage(imageUrl, 151d, 226d));
        Scene scene2 = new Scene("ppt2", new PptPage(imageUrl, 151d, 226d));

        final Scene[] mScenes = new Scene[]{scene1, scene2};
        mRoom.putScenes("/ppt", mScenes, 0);
        mRoom.setScenePath("/ppt/ppt1");

        final CountDownLatch latch = new CountDownLatch(1);

        mRoom.getScenes(new Promise<Scene[]>() {
            @Override
            public void then(Scene[] scenes) {
                Assert.assertTrue("test scenes number", mScenes.length == scenes.length);
                Assert.assertTrue("test scenes src", mScenes[0].getPpt().getSrc().equals(scenes[0].getPpt().getSrc()));
                Assert.assertTrue("test scenes width", mScenes[0].getPpt().getWidth() == (scenes[0].getPpt().getWidth()));
                Assert.assertTrue("test scenes height", mScenes[0].getPpt().getHeight() == (scenes[0].getPpt().getHeight()));
                Assert.assertTrue("test scenes src", mScenes[1].getPpt().getSrc().equals(scenes[1].getPpt().getSrc()));
                latch.countDown();
            }

            @Override
            public void catchEx(SDKError t) {
                assertFail(t.toString());
                latch.countDown();
            }
        });
    }

    @Test(timeout=TIME_OUT)
    public void testCleanScene() {
        Scene scene1 = new Scene("ppt1", new PptPage(imageUrl, 151d, 226d));

        final Scene[] mScenes = new Scene[]{scene1};
        mRoom.putScenes("/ppt", mScenes, 0);
        mRoom.setScenePath("/ppt/ppt1");
        mRoom.cleanScene(false);

        final CountDownLatch latch = new CountDownLatch(1);

        mRoom.getScenes(new Promise<Scene[]>() {
            @Override
            public void then(Scene[] scenes) {
                Assert.assertNull("ppt clean", scenes[0].getPpt());
                Assert.assertTrue("test name", scenes[0].getName().equals(mScenes[0].getName()));
                latch.countDown();
            }

            @Override
            public void catchEx(SDKError t) {
                assertFail(t.toString());
                latch.countDown();
            }
        });

        try {
            latch.await();
        } catch (Exception e) {
            e.printStackTrace();
            assertFail(e.toString());
        }
    }

    final static String EVENT_NAME = "customEvent";
    @Test(timeout=TIME_OUT)
    public void testCustomEvent() {
        final CountDownLatch latch = new CountDownLatch(1);
        mRoom.addMagixEventListener(EVENT_NAME, new EventListener() {
            @Override
            public void onEvent(EventEntry eventEntry) {
                logTestAction(eventEntry.toString());
                latch.countDown();
            }
        });

        mRoom.dispatchMagixEvent(new AkkoEvent(EVENT_NAME, new HashMap<String, String>() {{
            put("key1", "value1");
            put("key2", "value2");
        }}));
        try {
            latch.await();
        } catch (Exception e) {
            e.printStackTrace();
            assertFail(e.toString());
        }
    }

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

    private void logTestAction(String str) {
        Log.i("test action", Thread.currentThread().getStackTrace()[3].getMethodName() + " " + str);
    }

    private void assertFail(String str) {
        Assert.fail(str);
    }
}


