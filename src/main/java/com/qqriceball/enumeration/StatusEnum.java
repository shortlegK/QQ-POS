package com.qqriceball.enumeration;

import lombok.Getter;

@Getter
public enum StatusEnum {

    INACTIVE(0,"停用/下架"),
    ACTIVE(1,"啟用/上架");

    private final int code;
    private final String desc;

    StatusEnum(int code,String desc) {
        this.code = code;
        this.desc = desc;
    }
}