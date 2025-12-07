package com.qqriceball.common.exception;

import com.qqriceball.constant.MessageEnum;

public class AccountNotExistException extends BaseException {

    public AccountNotExistException(MessageEnum messageEnum) {
        super(messageEnum);
    }

}
