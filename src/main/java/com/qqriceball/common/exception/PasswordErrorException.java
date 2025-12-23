package com.qqriceball.common.exception;

import com.qqriceball.enumeration.MessageEnum;

public class PasswordErrorException extends BaseException {

    public PasswordErrorException(MessageEnum messageEnum) {
        super(messageEnum);
    }

}
