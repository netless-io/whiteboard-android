package com.herewhite.demo.utils;

import android.view.View;

import org.hamcrest.Matcher;

import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.matcher.ViewMatchers;

public class SimpleViewAction implements ViewAction {
    public SimpleViewAction() {

    }

    @Override
    public Matcher<View> getConstraints() {
        return ViewMatchers.isRoot();
    }

    @Override
    public String getDescription() {
        return "AbstractViewAction";
    }

    @Override
    public void perform(UiController uiController, View view) {

    }
}
