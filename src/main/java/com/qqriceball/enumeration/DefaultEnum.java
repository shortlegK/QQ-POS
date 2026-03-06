package com.qqriceball.enumeration;

import lombok.Getter;

@Getter
public enum DefaultEnum {

    NO(0),
    YES(1);

    private final int code;

    DefaultEnum(int code) {
        this.code = code;
    }
}