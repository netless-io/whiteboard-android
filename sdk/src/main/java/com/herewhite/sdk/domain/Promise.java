package com.herewhite.sdk.domain;

/**
 * Created by buhe on 2018/8/12.
 */

public interface Promise<T> {
    void then(T t);

    void catchEx(Exception t);
}
