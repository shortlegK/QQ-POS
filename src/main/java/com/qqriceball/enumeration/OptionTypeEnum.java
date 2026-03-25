package com.qqriceball.enumeration;

import lombok.Getter;

@Getter
public enum OptionTypeEnum {

    RICE_TYPE(0,"米飯種類",true,1),
    RICE_SIZE(1,"飯量",true,1),
    SPICE_LEVEL(2,"辣度",true,1),
    ADD_ON(3,"加料",false,null),
    DRINK_TEMPERATURE(4,"飲品溫度",true,1);

    private final int code;
    private final String desc;
    private final Boolean singleSelect;
    private final Integer limit;

    OptionTypeEnum(int code, String desc,Boolean singleSelect, Integer limit) {
        this.code = code;
        this.desc = desc;
        this.singleSelect = singleSelect;
        this.limit = limit;
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
