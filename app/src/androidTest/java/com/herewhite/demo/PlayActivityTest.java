package com.herewhite.demo;

import android.view.MenuItem;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;

import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.IdlingResource;
import androidx.test.espresso.ViewInteraction;
import androidx.test.rule.ActivityTestRule;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;

import com.herewhite.sdk.domain.Promise;
import com.herewhite.sdk.domain.SDKError;

public class PlayActivityTest {
    private PlayActivity mActivity;
    private IdlingResource mIdlingResource;

    @Rule
    public ActivityTestRule<PlayActivity> activityRule = new ActivityTestRule<PlayActivity>(PlayActivity.class) {
        @Override
        protected void afterActivityLaunched() {
            mActivity = activityRule.getActivity();

            mIdlingResource = mActivity.getIdlingResource();
            IdlingRegistry.getInstance().register(mIdlingResource);
        }
    };

    @Test
    public void menuItemClickTest() {
        onView(withId(R.id.white)).check(matches(isDisplayed()));

        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
        onData(allOf(is(instanceOf(MenuItem.class)), withTitle(mActivity.getString(R.string.getTimeInfo_command)))).perform(click());

        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
        onData(allOf(is(instanceOf(MenuItem.class)), withTitle(mActivity.getString(R.string.getPlayState_command)))).perform(click());

        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
        onData(allOf(is(instanceOf(MenuItem.class)), withTitle(mActivity.getString(R.string.getPhase_command)))).perform(click());

        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
        onData(allOf(is(instanceOf(MenuItem.class)), withTitle(mActivity.getString(R.string.getPlaybackSpeed_command)))).perform(click());
    }

    @Test
    public void playerPauseTest() {
        onView(withId(R.id.white)).check(matches(isDisplayed()));

        ViewInteraction interaction = onView(withId(R.id.button_pause)).perform(click());
        TestUtils.sleep(1000);

        onView(withId(R.id.button_play)).perform(click());
        TestUtils.sleep(1000);
    }

    @Test
    public void testSetAndGet_PlaybackSpeed() {
        onView(withId(R.id.white)).check(matches(isDisplayed()));
        // 参数化测试支持不理想
        final double[] speeds = new double[]{1.25f, 1.5f, 2.0f};
        for (double speedTarget : speeds) {
            mActivity.mPlaybackPlayer.setPlaybackSpeed(speedTarget);
            assertEquals(speedTarget, mActivity.mPlaybackPlayer.getPlaybackSpeed(), Constants.DOUBLE_DELTA);
            mActivity.mPlaybackPlayer.getPlaybackSpeed(new Promise<Double>() {
                @Override
                public void then(Double speed) {
                    assertEquals(speed, speed, Constants.DOUBLE_DELTA);
                }

                @Override
                public void catchEx(SDKError t) {

                }
            });
        }
    }

    @After
    public void unregisterIdlingResource() {
        if (mIdlingResource != null) {
            IdlingRegistry.getInstance().unregister(mIdlingResource);
        }
    }

    static MenuItemTitleMatcher withTitle(String title) {
        return new MenuItemTitleMatcher(title);
    }
}