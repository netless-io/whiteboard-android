package com.herewhite.sdk.util;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;

import java.util.Arrays;
import java.util.List;

public class GsonFieldIgnoreStrategy implements ExclusionStrategy {
    List<String> ignoreFiledList;

    public GsonFieldIgnoreStrategy(String... ignoreFields) {
        ignoreFiledList = Arrays.asList(ignoreFields);
    }

    @Override
    public boolean shouldSkipField(FieldAttributes f) {
        return ignoreFiledList.contains(f.getName());
    }

    @Override
    public boolean shouldSkipClass(Class<?> clazz) {
        return false;
    }
}
