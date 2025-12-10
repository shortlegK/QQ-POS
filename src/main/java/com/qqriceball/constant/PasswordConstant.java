package com.qqriceball.constant;

import lombok.Getter;

@Getter
public enum PasswordConstant {
    DEFAULT_PASSWORD("QQPOS123456");

    private final String password;

    PasswordConstant(String password) {
        this.password = password;
    }


}
