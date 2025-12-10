package com.qqriceball.common.exception;

import com.qqriceball.constant.MessageConstant;

public class AccountNotExistException extends BaseException {

    public AccountNotExistException(MessageConstant messageConstant) {
        super(messageConstant);
    }

}
