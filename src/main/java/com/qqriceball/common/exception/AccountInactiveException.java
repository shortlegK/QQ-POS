package com.qqriceball.common.exception;

import com.qqriceball.constant.MessageConstant;

public class AccountInactiveException extends BaseException {

    public AccountInactiveException(MessageConstant messageConstant) {
        super(messageConstant);
    }
}
