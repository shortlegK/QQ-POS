package com.qqriceball.enumeration;

import lombok.Getter;

@Getter
public enum OptionTypeEnum {

    //選項類型(0 - 米飯種類, 1 - 飯量, 2 - 辣度, 3 - 加料)
    RICE_TYPE(0,"米飯種類"),
    RICE_SIZE(1,"飯量"),
    SPICE_LEVEL(2,"辣度"),
    ADD_ON(3,"加料"),
    DRINK_TEMPERATURE(4,"飲品溫度");

    private final int code;
    private final String desc;

    OptionTypeEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static OptionTypeEnum getByCode(Integer code){
        if(code == null) return null;
        for(OptionTypeEnum type : OptionTypeEnum.values()){
            if(type.getCode() == code){
                return type;
            }
        }
        return null;
    }

}
