package com.qqriceball.utils;

import java.util.concurrent.atomic.AtomicInteger;

public class TestDataGenerator {

    private static final AtomicInteger counter = new AtomicInteger(1);

    public static String getUnique(String prefix) {
        return prefix + counter.getAndIncrement();
    }
}
