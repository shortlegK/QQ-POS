package com.qqriceball.common.exception;

import com.qqriceball.enumeration.MessageEnum;

public class AlreadyExistsException extends BaseException {
    public AlreadyExistsException(MessageEnum messageEnum) {
        super(messageEnum);
    }
}
