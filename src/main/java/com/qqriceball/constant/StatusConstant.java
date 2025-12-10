package com.qqriceball.constant;

import lombok.Getter;

@Getter
public enum StatusConstant {

    INACTIVE(0),
    ACTIVE(1);

    private final int value;

    StatusConstant(int value) {
        this.value = value;
    }
}