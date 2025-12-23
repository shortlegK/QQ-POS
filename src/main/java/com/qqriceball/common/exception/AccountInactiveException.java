package com.qqriceball.common.exception;

import com.qqriceball.enumeration.MessageEnum;

public class AccountInactiveException extends BaseException {

    public AccountInactiveException(MessageEnum messageEnum) {
        super(messageEnum);
    }
}
