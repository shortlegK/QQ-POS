package com.qqriceball.enumeration;

import lombok.Getter;

@Getter
public enum ProductTypeEnum {

    MEAT(0),
    VEGAN(1),
    DRINKS(2);

    private final int value;

    ProductTypeEnum(int value) {
        this.value = value;
    }
}
