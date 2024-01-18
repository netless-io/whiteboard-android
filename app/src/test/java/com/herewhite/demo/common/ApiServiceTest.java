package com.herewhite.demo.common;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.concurrent.CompletableFuture;

public class ApiServiceTest {
    public String sdkToken = "";

    @Test
    public void createRoomAndToken() {
        CompletableFuture<String> future = new CompletableFuture<>();
        ApiService.createRoom(sdkToken, 100, "cn-hz", new ApiCallback<RoomCreationResult>() {
            @Override
            public void onSuccess(RoomCreationResult data) {
                String uuid = data.uuid;
                ApiService.createRoomToken(sdkToken, uuid, "cn-hz", new ApiCallback<String>() {
                    @Override
                    public void onSuccess(String token) {
                        System.out.printf("uuid: %s\ntoken: %s\n%n", uuid, token);
                        future.complete(token);
                    }

                    @Override
                    public void onFailure(String message) {
                        future.complete(null);
                    }
                });
            }

            @Override
            public void onFailure(String message) {
                future.complete(null);
            }
        });

        try {
            String token = future.get();
            assertTrue(token != null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}