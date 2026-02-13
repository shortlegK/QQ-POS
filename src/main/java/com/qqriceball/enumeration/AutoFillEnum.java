package com.qqriceball.enumeration;

import lombok.Getter;

@Getter
public enum AutoFillEnum {

    SET_CREATE_ID("setCreateId"),
    SET_CREATE_TIME("setCreateTime"),
    SET_UPDATE_ID("setUpdateId"),
    SET_UPDATE_TIME("setUpdateTime");

    private final String message;

    AutoFillEnum(String message) {
        this.message = message;
    }
}
