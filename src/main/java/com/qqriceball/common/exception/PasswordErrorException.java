package com.qqriceball.common.exception;

import com.qqriceball.constant.MessageEnum;

public class PasswordErrorException extends BaseException {

    public PasswordErrorException(MessageEnum messageEnum) {
        super(messageEnum);
    }

}
