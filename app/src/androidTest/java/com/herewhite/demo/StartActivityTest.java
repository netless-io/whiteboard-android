package com.herewhite.demo;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import androidx.test.rule.ActivityTestRule;

import org.junit.Rule;
import org.junit.Test;

public class StartActivityTest {
    @Rule
    public ActivityTestRule<StartActivity> activityRule = new ActivityTestRule<>(StartActivity.class);

    @Test
    public void showsPrimaryEntriesAndQaEntry() {
        onView(withText(R.string.basic_room)).check(matches(isDisplayed()));
        onView(withText(R.string.window_room)).check(matches(isDisplayed()));
        onView(withText(R.string.replay)).check(matches(isDisplayed()));
        onView(withText(R.string.replay_pure)).check(matches(isDisplayed()));
        onView(withText(R.string.qa_debug_examples)).check(matches(isDisplayed()));
    }

    @Test
    public void qaEntryOpensQaPage() {
        onView(withText(R.string.qa_debug_examples)).perform(click());
        onView(withText(R.string.qa_single_window_group)).check(matches(isDisplayed()));
        onView(withText(R.string.qa_window_group)).check(matches(isDisplayed()));
        onView(withText(R.string.qa_resource_group)).check(matches(isDisplayed()));
    }
}
