package com.qqriceball.common.exception;

import com.qqriceball.enumeration.MessageEnum;

public class OptionNotFoundException extends BaseException {
    public OptionNotFoundException(MessageEnum messageEnum) {
        super(messageEnum);
    }
}
