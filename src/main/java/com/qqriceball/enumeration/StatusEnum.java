package com.qqriceball.enumeration;

import lombok.Getter;

@Getter
public enum StatusEnum {

    INACTIVE(0),
    ACTIVE(1);

    private final int code;

    StatusEnum(int code) {
        this.code = code;
    }
}