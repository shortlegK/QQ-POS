package com.qqriceball.common.exception;

import com.qqriceball.enumeration.MessageEnum;

public class BadRequestArgsException extends BaseException {
    public BadRequestArgsException(MessageEnum messageEnum) {
        super(messageEnum);
    }
}
