package com.qqriceball.common.exception;

import com.qqriceball.enumeration.MessageEnum;
import lombok.Getter;

@Getter
public class BaseException extends RuntimeException {

    private final MessageEnum messageEnum;

    public BaseException(MessageEnum messageEnum) {
        super(messageEnum.getMessage());
        this.messageEnum = messageEnum;
    }

}
