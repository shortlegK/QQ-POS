package com.qqriceball.common.exception;

import com.qqriceball.enumeration.MessageEnum;

public class NotExistException extends BaseException {

    public NotExistException(MessageEnum messageEnum) {
        super(messageEnum);
    }

}
