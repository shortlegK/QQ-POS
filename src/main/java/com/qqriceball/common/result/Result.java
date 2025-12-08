package com.qqriceball.common.result;

import com.qqriceball.constant.MessageEnum;
import lombok.Data;

import java.io.Serializable;

@Data
public class Result<T> implements Serializable {

    private Integer code; //200成功，其他數字為失敗
    private String msg;
    private T data;

    public static <T> Result<T> success() {
        Result<T> result = new Result<>();
        result.code = MessageEnum.SUCCESS.getCode();
        return result;
    }

    public static <T> Result<T> success(T object) {
        Result<T> result = new Result<>();
        result.data = object;
        result.code = MessageEnum.SUCCESS.getCode();
        return result;
    }

    public static <T> Result<T> error(MessageEnum messageEnum) {
        Result<T> result = new Result<>();
        result.msg = messageEnum.getMessage();
        result.code = messageEnum.getCode();
        return result;
    }


}
