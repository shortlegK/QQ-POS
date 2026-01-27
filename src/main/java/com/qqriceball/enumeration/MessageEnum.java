package com.qqriceball.enumeration;

import lombok.Getter;

@Getter
public enum MessageEnum {

    SUCCESS(200, "執行成功"),

    // 登入/帳號相關
    PASSWORD_ERROR(1001, "密碼錯誤"),
    ACCOUNT_NOT_EXIST(1002, "帳號不存在"),
    ACCOUNT_INACTIVE(1003, "帳號已停用"),
    USER_NOT_LOGIN(1004, "用戶未登入"),
    PASSWORD_EDIT_FAILED(1005, "修改密碼失敗"),
    TOKEN_INVALID(1006, "Token 無效"),
    ACCESS_DENIED(1007, "權限不足"),
    USERNAME_ALREADY_EXIST(1008,"使用者帳號已存在"),

    // 通用錯誤
    UNKNOWN_ERROR(2001, "未知的錯誤"),
    ALREADY_EXISTS(2002, "資源已存在"),
    BAD_REQUEST(2003, "請求參數格式不正確"),

    // 菜單品項/加料選項相關
    PRODUCT_ON_SALE(3001, "販賣中的品項無法刪除"),
    OPTION_NOT_EXISTS(3002, "加料選項 ID 不存在"),
    PRODUCT_ALREADY_EXIST(3003, "菜單品項名稱已存在"),
    TYPE_NOT_FOUND(3004,"分類 ID 不存在"),
    PRODUCT_NOT_EXIST(3005, "菜單品項不存在"),

    // 訂單相關
    SHOPPING_CART_IS_NULL(4001, "內容為空，無法建立訂單"),
    ORDER_STATUS_ERROR(4002, "訂單狀態錯誤"),
    ORDER_NOT_FOUND(4003, "訂單不存在");


    private final int code;
    private final String message;

    MessageEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }
}