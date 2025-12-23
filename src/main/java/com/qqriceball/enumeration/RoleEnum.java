package com.qqriceball.enumeration;

import lombok.Getter;

@Getter
public enum RoleEnum {

    MANAGER(0, "ROLE_MANAGER"),
    STAFF(1, "ROLE_STAFF");

    private final int value;
    private final String roleName;

    RoleEnum(int value, String roleName) {
        this.value = value;
        this.roleName = roleName;
    }
}
