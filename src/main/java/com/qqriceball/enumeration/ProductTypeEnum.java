package com.qqriceball.enumeration;

import lombok.Getter;

@Getter
public enum ProductTypeEnum {

    //(0 - 葷飯糰,1 - 素飯糰 ,2 - 飲品 )
    MEAT(0,"葷食飯糰"),
    VEGAN(1,"素食飯糰"),
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
