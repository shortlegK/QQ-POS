package com.qqriceball.constant;

import lombok.Getter;

@Getter
public enum RoleConstant {

    MANAGER(0, "ROLE_MANAGER"),
    STAFF(1, "ROLE_STAFF");

    private final int value;
    private final String roleName;

    RoleConstant(int value, String roleName) {
        this.value = value;
        this.roleName = roleName;
    }
}
