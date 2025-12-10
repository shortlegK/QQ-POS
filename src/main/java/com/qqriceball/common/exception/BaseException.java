package com.qqriceball.common.exception;

import com.qqriceball.constant.MessageConstant;
import lombok.Getter;

@Getter
public class BaseException extends RuntimeException {

    private final MessageConstant messageConstant;

    public BaseException(MessageConstant messageConstant) {
        super(messageConstant.getMessage());
        this.messageConstant = messageConstant;
    }

}
