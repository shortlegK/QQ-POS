package com.qqriceball.constant;

import lombok.Getter;

@Getter
public enum MessageEnum {

    // 登入/帳號相關
    PASSWORD_ERROR(1001, "密碼錯誤"),
    ACCOUNT_NOT_EXIST(1002, "帳號錯誤"),
    ACCOUNT_INACTIVE(1003, "帳號已停用"),
    USER_NOT_LOGIN(1004, "用戶未登入"),
    PASSWORD_EDIT_FAILED(1005, "修改密碼失敗"),

    //通用錯誤
    UNKNOWN_ERROR(2001, "未知的錯誤"),
    ALREADY_EXISTS(2002, "已存在"),

    //業務邏輯相關
    DISH_ON_SALE(3001, "販賣中的餐點無法刪除"),
    SHOPPING_CART_IS_NULL(3002, "內容為空，無法建立訂單"),
    ORDER_STATUS_ERROR(3003, "訂單狀態錯誤"),
    ORDER_NOT_FOUND(3004, "訂單不存在");


    private final int code;
    private final String message;

    MessageEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }
}