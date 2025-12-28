package com.qqriceball.enumeration;

import lombok.Getter;

@Getter
public enum OptionTypeEnum {

    //選項類型(0 - 米飯種類, 1 - 飯量, 2 - 辣度, 3 - 加料)
    RICE_TYPE(0,"米飯種類"),
    RICE_SIZE(1,"飯量"),
    SPICINESS(2,"辣度"),
    PRODUCT_OPTION(3,"加料");

    private final int code;
    private final String desc;

    OptionTypeEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
