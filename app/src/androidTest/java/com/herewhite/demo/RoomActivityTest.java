package com.herewhite.demo;


import android.view.MenuItem;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;

import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.IdlingResource;
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

public class RoomActivityTest {
    private RoomActivity mActivity;
    private IdlingResource mIdlingResource;

    @Rule
    public ActivityTestRule<RoomActivity> activityRule = new ActivityTestRule<RoomActivity>(RoomActivity.class) {
        @Override
        protected void afterActivityLaunched() {
            mActivity = activityRule.getActivity();

            mIdlingResource = mActivity.getIdlingResource();
            IdlingRegistry.getInstance().register(mIdlingResource);
        }
    };


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
