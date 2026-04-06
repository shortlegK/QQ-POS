package com.qqriceball.testData.order;

import com.qqriceball.enumeration.OrderStatusEnum;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class SeedOrderData {

    private static final DateTimeFormatter ORDER_DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final LocalDateTime today =  LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
    private static final LocalDateTime nextWeek = LocalDateTime.now().plusWeeks(1).truncatedTo(ChronoUnit.MINUTES);
    private static final LocalDateTime lastWeek = LocalDateTime.now().minusWeeks(1).truncatedTo(ChronoUnit.MINUTES);
    private static final LocalDateTime lastMonth = LocalDateTime.now().minusMonths(1).truncatedTo(ChronoUnit.MINUTES);
    private static final LocalDateTime yesterday = LocalDateTime.now().minusDays(1).truncatedTo(ChronoUnit.MINUTES);

    private static final AtomicInteger orderNoSequence = new AtomicInteger(1);

    // 製作中
    public static final TestOrder orderMaking = new TestOrder(buildOrderNo(today), today,
            50, OrderStatusEnum.MAKING.getCode());
    public static final TestOrder orderMakingNextWeek = new TestOrder(buildOrderNo(nextWeek), nextWeek,
            50 , OrderStatusEnum.MAKING.getCode());

    // 待領取
    public static final TestOrder orderReady= new TestOrder(buildOrderNo(today), today,
            50, OrderStatusEnum.READY.getCode());

    // 已領取
    public static final TestOrder orderPickedUp = new TestOrder(buildOrderNo(today), today,
            50 , OrderStatusEnum.PICKED_UP.getCode());
    public static final TestOrder orderPickedUpYesterday = new TestOrder(buildOrderNo(yesterday), yesterday,
            100 , OrderStatusEnum.PICKED_UP.getCode());
    public static final TestOrder orderPickedUpLastWeek = new TestOrder(buildOrderNo(lastWeek), lastWeek,
            120 , OrderStatusEnum.PICKED_UP.getCode());
    public static final TestOrder orderPickedUpLastMonth = new TestOrder(buildOrderNo(lastMonth), lastMonth,
            140 , OrderStatusEnum.PICKED_UP.getCode());

    // 已取消
    public static final TestOrder orderCancel = new TestOrder(buildOrderNo(today), today,
            50 , OrderStatusEnum.CANCELLED.getCode());



    private static String buildOrderNo(LocalDateTime dateTime){
        String dateStr = dateTime.format(ORDER_DATE_FORMAT);
        return dateStr + String.format("%04d", orderNoSequence.getAndIncrement());
    }

}
