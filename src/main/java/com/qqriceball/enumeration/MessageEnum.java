package com.qqriceball.enumeration;

import lombok.Getter;

@Getter
public enum MessageEnum {

    SUCCESS(200, "執行成功"),

    // 登入/帳號相關
    PASSWORD_ERROR(10001, "帳號或密碼錯誤。"),
    ACCOUNT_NOT_EXISTS(10002, "帳號或密碼錯誤。"),
    ACCOUNT_INACTIVE(10003, "您的帳號目前已被停用，如有疑問請聯繫管理員。"),
    TOKEN_INVALID(10004, "登入已過期，請重新登入。"),
    USERNAME_ALREADY_EXISTS(10005,"此帳號已被使用，請使用其他帳號。"),
    OLD_PASSWORD_ERROR(10006,"舊密碼輸入錯誤，請再試一次。"),

    // 通用錯誤
    UNKNOWN_ERROR(20001, "系統發生未預期的錯誤，請稍後再試。"),
    BAD_REQUEST(20002, "請求資料格式有誤，請確認後再試。"),

    // 產品相關
    PRODUCT_NOT_EXIST(30001, "找不到指定的產品，請確認後再試。"),
    PRODUCT_ALREADY_EXISTS(30002, "產品名稱已存在，請使用其他名稱。"),


    // 選項相關
    OPTION_NOT_EXIST(40001, "找不到指定的選項，請確認後再試。"),
    OPTION_ALREADY_EXISTS(40002, "選項名稱已存在，請使用其他名稱。"),
    OPTION_ADD_ON_DEFAULT_ERROR(40003, "加料類選項不可設為預設，請重新設定。"),
    OPTION_NO_INGREDIENT_DEFAULT_ERROR(40004, "去除配料類選項不可設為預設，請重新設定。"),

    // 訂單相關
    ORDER_NOT_EXIST(50001, "找不到指定的訂單，請確認後再試。"),
    PRODUCT_UNAVAILABLE(50002,"訂單中包含已下架的商品，請重新確認訂單內容。"),
    OPTION_UNAVAILABLE(50003,"訂單中包含已下架的選項，請重新確認訂單內容。"),
    OPTION_TYPE_NOT_ALLOWED(50004,"商品選項類型設定有誤，請重新確認。"),
    DUPLICATE_OPTION(50005, "商品選項重複設定，請重新確認訂單內容。"),
    REQUIRED_OPTION_MISSING(50006, "有必填的選項尚未選擇，請補充後再試。"),
    SINGLE_SELECT_OPTION_QUANTITY_EXCEED(50007, "單選細節選項只能選擇一項，請重新確認。"),
    ORDER_NOT_EDITABLE(50008, "此訂單狀態已無法修改，請確認訂單狀態。"),
    ORDER_STATUS_CODE_INVALID(50009, "訂單狀態代碼無效，請重新確認。"),
    ORDER_STATUS_TRANSITION_NOT_ALLOWED(50010, "訂單目前無法變更為指定狀態，請確認操作是否正確。");

    private final int code;
    private final String message;

    MessageEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }
}