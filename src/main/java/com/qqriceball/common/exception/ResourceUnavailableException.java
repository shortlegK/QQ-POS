package com.qqriceball.common.exception;

import com.qqriceball.enumeration.MessageEnum;

public class ResourceUnavailableException extends BaseException {
    public ResourceUnavailableException(MessageEnum messageEnum) {
        super(messageEnum);
    }
}
