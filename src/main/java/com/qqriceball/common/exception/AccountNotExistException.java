package com.qqriceball.common.exception;

import com.qqriceball.enumeration.MessageEnum;

public class AccountNotExistException extends BaseException {

    public AccountNotExistException(MessageEnum messageEnum) {
        super(messageEnum);
    }

}
