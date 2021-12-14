package com.herewhite.demo.common;

public interface ApiCallback<T> {
    void onSuccess(T data);

    void onFailure(String message);
}