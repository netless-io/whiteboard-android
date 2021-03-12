package com.herewhite.demo;

import com.herewhite.demo.IdlingResource.SimpleIdlingResource;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.appcompat.app.AppCompatActivity;
import androidx.test.espresso.IdlingResource;

public abstract class BaseActivity extends AppCompatActivity {
    @Nullable
    private SimpleIdlingResource mIdlingResource = new SimpleIdlingResource();

    @VisibleForTesting
    @NonNull
    public IdlingResource getIdlingResource() {
        return mIdlingResource;
    }

    protected void testMarkIdling(boolean idling) {
        mIdlingResource.setIdleState(idling);
    }
}
