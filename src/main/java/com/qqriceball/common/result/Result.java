package com.qqriceball.common.result;

import com.qqriceball.constant.MessageConstant;
import lombok.Data;

import java.io.Serializable;

@Data
public class Result<T> implements Serializable {

    private Integer code; //200成功，其他數字為失敗
    private String msg;
    private T data;

    public static <T> Result<T> success() {
        Result<T> result = new Result<>();
        result.code = MessageConstant.SUCCESS.getCode();
        result.msg = MessageConstant.SUCCESS.getMessage();
        return result;
    }

    public static <T> Result<T> success(T object) {
        Result<T> result = new Result<>();
        result.data = object;
        result.code = MessageConstant.SUCCESS.getCode();
        result.msg = MessageConstant.SUCCESS.getMessage();
        return result;
    }

    public static <T> Result<T> error(MessageConstant messageConstant) {
        Result<T> result = new Result<>();
        result.msg = messageConstant.getMessage();
        result.code = messageConstant.getCode();
        return result;
    }

    public static <T> Result<T> error(MessageConstant messageConstant,String message) {
        Result<T> result = new Result<>();
        result.msg = message;
        result.code = messageConstant.getCode();
        return result;
    }


}
