package com.herewhite.demo;

import android.view.MenuItem;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

public class MenuItemTitleMatcher extends BaseMatcher<Object> {
    private final String title;

    public MenuItemTitleMatcher(String title) {
        this.title = title;
    }

    @Override
    public boolean matches(Object o) {
        if (o instanceof MenuItem) {
            return ((MenuItem) o).getTitle().equals(title);
        }
        return false;
    }

    @Override
    public void describeTo(Description description) {
    }
}