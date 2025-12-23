package com.qqriceball.enumeration;

import lombok.Getter;

@Getter
public enum PasswordEnum {
    DEFAULT_PASSWORD("QQPOS123456");

    private final String password;

    PasswordEnum(String password) {
        this.password = password;
    }


}
