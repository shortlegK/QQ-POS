package com.qqriceball.integration.utils;

import java.util.concurrent.atomic.AtomicInteger;

public class Utils {

    private static AtomicInteger counter = new AtomicInteger(1);

    public static String getUnique(String prefix) {
        return prefix + counter.getAndIncrement();
    }
}
