package com.herewhite.sdk.domain;
import com.google.gson.Gson;

public class WhiteObject {

    static Gson gson = new Gson();
    @Override
    public String toString() {
        return gson.toJson(this);
    }
}
