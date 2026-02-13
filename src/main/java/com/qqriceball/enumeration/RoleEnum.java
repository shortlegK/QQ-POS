package com.qqriceball.enumeration;

import lombok.Getter;

@Getter
public enum RoleEnum {

    MANAGER(0, "ROLE_MANAGER"),
    STAFF(1, "ROLE_STAFF");

    private final int code;
    private final String roleName;

    RoleEnum(int code, String roleName) {
        this.code = code;
        this.roleName = roleName;
    }
}
