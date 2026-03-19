package com.qqriceball.common.exception;

import com.qqriceball.enumeration.MessageEnum;

public class ResourceNotFoundException extends BaseException {

    public ResourceNotFoundException(MessageEnum messageEnum) {
        super(messageEnum);
    }

}
