package com.qqriceball.enumeration;

import lombok.Getter;

@Getter
public enum PeriodTypeEnum {

    TODAY(0,"當日"),
    THIS_WEEK(1,"當週"),
    THIS_MONTH(2,"當月");

    private final int code;
    private final String desc;

    PeriodTypeEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static PeriodTypeEnum getByCode(Integer code){
        if(code == null) return null;
        for(PeriodTypeEnum type : PeriodTypeEnum.values()){
            if(type.getCode() == code){
                return type;
            }
        }
        return null;
    }
}
