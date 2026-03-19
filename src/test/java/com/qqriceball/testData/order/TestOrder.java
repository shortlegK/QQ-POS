package com.qqriceball.testData.order;

import java.time.LocalDateTime;

public record TestOrder(String orderNo, LocalDateTime pickupTime,
                        Integer total, Integer status) {
}
