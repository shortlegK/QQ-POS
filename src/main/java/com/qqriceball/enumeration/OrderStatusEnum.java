package com.qqriceball.enumeration;

import lombok.Getter;

@Getter
public enum OrderStatusEnum {

    MAKING(0, "製作中"),
    READY(1,"待領取"),
    PICKED_UP(1, "已領取"),
    CANCELLED(2, "已取消");

    private final int code;
    private final String desc;

    OrderStatusEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}