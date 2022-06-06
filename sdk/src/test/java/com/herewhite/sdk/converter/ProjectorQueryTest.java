package com.herewhite.sdk.converter;

import com.herewhite.sdk.domain.ConvertException;
import com.herewhite.sdk.domain.Region;

import org.junit.Assert;
import org.junit.Test;

import java.util.Random;
import java.util.concurrent.CountDownLatch;

public class ProjectorQueryTest {
    static class TestCallback implements ProjectorQuery.Callback {
        private final CountDownLatch latch;
        public int countFinish;
        public int countFailure;

        public TestCallback(CountDownLatch latch) {
            this.latch = latch;
        }

        @Override
        public void onProgress(double progress, ProjectorQuery.QueryResponse convertInfo) {
            System.out.println(progress);
        }

        @Override
        public void onFinish(ProjectorQuery.QueryResponse response) {
            System.out.println("onFinish");
            countFinish++;
            latch.countDown();
        }

        @Override
        public void onFailure(ConvertException e) {
            System.out.println("onFailure " + e.getMessage());
            countFailure++;
            latch.countDown();
        }
    }

    class Item {
        private final String taskUuid;
        private final String taskToken;
        private final Region region;

        public Item(String taskUuid, String taskToken, Region region) {
            this.taskUuid = taskUuid;
            this.taskToken = taskToken;
            this.region = region;
        }
    }

    private Item[] items = new Item[]{
            new Item("a2db5393242e410b879f1382ba7eaf78", "NETLESSTASK_YWs9c21nRzh3RzdLNk1kTkF5WCZub25jZT03ZjdkNGViMC1lNTZjLTExZWMtODYyMS00OWUwMGY1YWE4Njgmcm9sZT0yJnNpZz0wMmQzNzFmMzMzNmYwOGEyNGEwNWYwYzRkM2QwNTk4M2I1YTcwNzdhYWQ0NjhhMjNjMmJjMzA0M2Y4ZWU3YjIxJnV1aWQ9YTJkYjUzOTMyNDJlNDEwYjg3OWYxMzgyYmE3ZWFmNzg", Region.cn),
            new Item("39afca4336344419909ef711f227f60a", "NETLESSTASK_YWs9c21nRzh3RzdLNk1kTkF5WCZub25jZT03M2ExMjM3MC1lNTc0LTExZWMtOGRjOS0yN2QzM2I1YWZiMjUmcm9sZT0yJnNpZz1mYmI3NjFiMzk0YWZiYmI0ZGY2YjMyZjRhM2Q5NzQ1ZDgzN2EwZTI0YzFmOTIxODc0Mjc5ODQ3NDkyYzRiMDAxJnV1aWQ9MzlhZmNhNDMzNjM0NDQxOTkwOWVmNzExZjIyN2Y2MGE", Region.cn),
            new Item("39a", "RzdLNk1kTkF5WCZub25jZT03M2ExMjM3MC1lNTc0LTExZWMtOGRjOS0yN2QzM2I1YWZiMjUmcm9sZT0yJnNpZz1mYmI3NjFiMzk0YWZiYmI0ZGY2YjMyZjRhM2Q5NzQ1ZDgzN2EwZTI0YzFmOTIxODc0Mjc5ODQ3NDkyYzRiMDAxJnV1aWQ9MzlhZmNhNDMzNjM0NDQxOTkwOWVmNzExZjIyN2Y2MGE", Region.cn),
    };

    @Test(expected = RuntimeException.class)
    public void when_token_or_uuid_error_throw_exception() {
        ProjectorQuery query = new ProjectorQuery
                .Builder()
                .setTaskUuid(null)
                .setTaskToken(null)
                .build();
        query.startQuery();
    }

    @Test
    public void callback_called_only_once() {
        int testCount = 100;

        CountDownLatch latch = new CountDownLatch(testCount);
        TestCallback[] callbacks = new TestCallback[testCount];
        for (int i = 0; i < testCount; i++) {
            callbacks[i] = new TestCallback(latch);
            Item item = items[randomInt(items.length)];
            ProjectorQuery query = new ProjectorQuery
                    .Builder()
                    .setTaskUuid(item.taskUuid)
                    .setTaskToken(item.taskToken)
                    .setRegion(item.region)
                    .setCallback(callbacks[i])
                    .build();
            query.startQuery();
        }

        try {
            latch.await();
        } catch (InterruptedException ignored) {
        }

        int count = 0;
        for (int i = 0; i < callbacks.length; i++) {
            System.out.println(String.format("count %d: %d", i, (callbacks[i].countFailure + callbacks[i].countFinish)));
            count += callbacks[i].countFailure + callbacks[i].countFinish;
        }
        Assert.assertEquals(count, testCount);
    }

    Random r = new Random();

    private int randomInt(int max) {
        return r.nextInt(max);
    }
}