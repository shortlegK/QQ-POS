package com.qqriceball.common.exception;

import com.qqriceball.enumeration.MessageEnum;

public class TypeNotFoundException extends BaseException {
    public TypeNotFoundException(MessageEnum messageEnum) {
        super(messageEnum);
    }
}
