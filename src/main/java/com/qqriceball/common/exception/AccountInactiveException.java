package com.qqriceball.common.exception;

import com.qqriceball.constant.MessageEnum;

public class AccountInactiveException extends BaseException {

    public AccountInactiveException(MessageEnum messageEnum) {
        super(messageEnum);
    }
}
