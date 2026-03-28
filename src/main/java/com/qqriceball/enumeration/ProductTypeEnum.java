package com.qqriceball.enumeration;

import lombok.Getter;

@Getter
public enum ProductTypeEnum {

    MEAT(0,"葷食"),
    VEGAN(1,"素食"),
    DRINKS(2,"飲品");

    private final int code;
    private final String desc;

    ProductTypeEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static ProductTypeEnum getByCode(Integer code){
        if(code == null) return null;
        for(ProductTypeEnum type : ProductTypeEnum.values()){
            if(type.getCode() == code){
                return type;
            }
        }
        return null;
    }

}
