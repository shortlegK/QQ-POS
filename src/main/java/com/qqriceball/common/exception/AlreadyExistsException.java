package com.qqriceball.common.exception;

import com.qqriceball.constant.MessageConstant;

public class AlreadyExistsException extends BaseException {
    public AlreadyExistsException(MessageConstant messageConstant) {
        super(messageConstant);
    }
}
