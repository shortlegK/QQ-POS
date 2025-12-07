package com.qqriceball.constant;

import lombok.Getter;

@Getter
public enum StatusEnum {

    INACTIVE(0),
    ACTIVE(1);

    private final int value;

    StatusEnum(int value) {
        this.value = value;
    }
}