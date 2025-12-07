package com.qqriceball.server.handler;


import com.qqriceball.common.exception.AccountInactiveException;
import com.qqriceball.common.exception.AccountNotExistException;
import com.qqriceball.common.exception.BaseException;
import com.qqriceball.common.exception.PasswordErrorException;
import com.qqriceball.common.result.Result;
import com.qqriceball.constant.MessageEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // 登入異常處理
    @ExceptionHandler(AccountNotExistException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED) // 回傳 HTTP 401 Unauthorized
    public Result<Object> handleAccountNotFound(AccountNotExistException ex) {
        log.error("登入異常 - 帳號不存在: {}", ex.getMessage());
        return Result.error(MessageEnum.ACCOUNT_NOT_EXIST);
    }

    @ExceptionHandler(PasswordErrorException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED) // 回傳 HTTP 401 Unauthorized
    public Result<Object> handlePasswordError(PasswordErrorException ex) {
        log.error("登入異常 - 密碼錯誤: {}", ex.getMessage());
        return Result.error(MessageEnum.PASSWORD_ERROR);
    }

    @ExceptionHandler(AccountInactiveException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN) // 回傳 HTTP 403 Forbidden
    public Result<Object> handleAccountLocked(AccountInactiveException ex) {
        log.error("登入異常 - 帳號已停用: {}", ex.getMessage());
        return Result.error(MessageEnum.ACCOUNT_INACTIVE);
    }




    @ExceptionHandler(BaseException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST) // 通用業務異常回傳 400
    public Result<Object> handleBaseException(BaseException ex) {
        log.error("通用業務異常: code={}, message={}", ex.getMessageEnum().getCode(), ex.getMessage());
        return Result.error(ex.getMessageEnum());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR) // 回傳 HTTP 500 Internal Server Error
    public Result<Object> handleUnknownException(Exception ex) {
        log.error("系統未知異常!", ex);
        return Result.error(MessageEnum.UNKNOWN_ERROR);
    }
}
