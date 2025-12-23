package com.qqriceball.common.exception;

import com.qqriceball.enumeration.MessageEnum;

public class PermissionDeniedException extends BaseException {
    public PermissionDeniedException(MessageEnum messageEnum) {
        super(messageEnum);
    }
}
