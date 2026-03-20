package com.qqriceball.enumeration;

import lombok.Getter;

@Getter
public enum MessageEnum {

    SUCCESS(200, "執行成功"),

    // 登入/帳號相關
    PASSWORD_ERROR(10001, "密碼錯誤"),
    ACCOUNT_NOT_EXISTS(10002, "帳號不存在"),
    ACCOUNT_INACTIVE(10003, "帳號已停用"),
    USER_NOT_LOGIN(10004, "用戶未登入"),
    PASSWORD_EDIT_FAILED(10005, "修改密碼失敗"),
    TOKEN_INVALID(10006, "Token 無效"),
    ACCESS_DENIED(10007, "權限不足"),
    USERNAME_ALREADY_EXISTS(10008,"使用者帳號已存在"),

    // 通用錯誤
    UNKNOWN_ERROR(20001, "未知的錯誤"),
    ALREADY_EXISTS(20002, "資源已存在"),
    BAD_REQUEST(20003, "資料查詢異常"),

    // 產品相關
    PRODUCT_NOT_EXIST(30001, "產品 ID 不存在"),
    PRODUCT_ALREADY_EXISTS(30002, "產品名稱已存在"),


    // 選項相關
    OPTION_NOT_EXIST(40001, "細節選項 ID 不存在"),
    OPTION_ALREADY_EXISTS(40002, "細節選項名稱已存在"),
    OPTION_ADD_ON_DEFAULT_ERROR(40003, "加料類選項不可設為預設"),

    // 訂單相關
    ORDER_STATUS_ERROR(50001, "訂單狀態錯誤"),
    ORDER_NOT_EXIST(50002, "訂單不存在"),
    PRODUCT_UNAVAILABLE(50003,"訂單內容含有已下架商品"),
    OPTION_UNAVAILABLE(50004,"訂單內容含有已下架細節選項"),
    OPTION_TYPE_NOT_ALLOWED(50005,"商品細節選項類型設定錯誤"),
    DUPLICATE_OPTION(50006, "商品選項重複設定"),
    REQUIRED_OPTION_MISSING(50007, "缺少必填商品細節選項"),
    SINGLE_SELECT_OPTION_QUANTITY_EXCEED(50008, "單選的細節選項設定數量超過限制"),
    ORDER_CAN_NOT_BE_MODIFIED(50009, "訂單狀態非製作中，無法修改資料");

    private final int code;
    private final String message;

    MessageEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }
}