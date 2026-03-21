package com.qqriceball.enumeration;

import lombok.Getter;

@Getter
public enum OrderStatusEnum {

    MAKING(0, "製作中"),
    READY(1,"待領取"),
    PICKED_UP(2, "已領取"),
    CANCELLED(3, "已取消");

    private final int code;
    private final String desc;

    OrderStatusEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static OrderStatusEnum getByCode(Integer code){
        if(code == null) return null;
        for(OrderStatusEnum type : OrderStatusEnum.values()){
            if(type.getCode() == code){
                return type;
            }
        }
        return null;
    }

    public Boolean canTransitionTo(OrderStatusEnum targetStatus) {
        return switch (this){
            case MAKING -> targetStatus == READY || targetStatus == PICKED_UP || targetStatus == CANCELLED;
            case READY -> targetStatus == PICKED_UP;
            case PICKED_UP, CANCELLED -> false; // 已領取和已取消狀態不可再轉換
        };
    }
}