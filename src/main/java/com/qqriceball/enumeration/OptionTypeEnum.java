package com.qqriceball.enumeration;

import lombok.Getter;

@Getter
public enum OptionTypeEnum {

    RICE_TYPE(0,"米飯種類",true),
    RICE_SIZE(1,"飯量",true),
    SPICE_LEVEL(2,"辣度",true),
    ADD_ON(3,"加料",false),
    DRINK_TEMPERATURE(4,"飲品溫度",true),
    NO_INGREDIENT(5,"去除配料",false);

    private final int code;
    private final String desc;
    private final Boolean singleSelect;

    OptionTypeEnum(int code, String desc,Boolean singleSelect) {
        this.code = code;
        this.desc = desc;
        this.singleSelect = singleSelect;
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

    public Boolean isSingleSelect() {
        return singleSelect;
    }
}
