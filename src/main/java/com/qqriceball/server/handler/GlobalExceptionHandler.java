package com.qqriceball.server.handler;


import com.qqriceball.common.exception.*;
import com.qqriceball.common.result.Result;
import com.qqriceball.constant.MessageConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
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
        return Result.error(ex.getMessageConstant());
    }

    @ExceptionHandler(PasswordErrorException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED) // 回傳 HTTP 401 Unauthorized
    public Result<Object> handlePasswordError(PasswordErrorException ex) {
        log.error("登入異常 - 密碼錯誤: {}", ex.getMessage());
        return Result.error(ex.getMessageConstant());
    }

    @ExceptionHandler(AccountInactiveException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN) // 回傳 HTTP 403 Forbidden
    public Result<Object> handleAccountLocked(AccountInactiveException ex) {
        log.error("登入異常 - 帳號已停用: {}", ex.getMessage());
        return Result.error(ex.getMessageConstant());
    }


    @ExceptionHandler(AlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT) // 回傳 HTTP 409 Conflict
    public Result<Object> handleAlreadyExists(AlreadyExistsException ex) {
        log.error("建立異常 - 資源已存在: {}", ex.getMessageConstant().getMessage());
        return Result.error(ex.getMessageConstant());
    }

    // request 參數異常處理
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(error -> error.getDefaultMessage())
                .orElse("請求參數格式不正確");

        return Result.error(MessageConstant.BAD_REQUEST, msg);
    }


    @ExceptionHandler(BaseException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST) // 通用業務異常回傳 400
    public Result<Object> handleBaseException(BaseException ex) {
        log.error("通用業務異常: code={}, message={}", ex.getMessageConstant().getCode(), ex.getMessage());
        return Result.error(ex.getMessageConstant());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR) // 回傳 HTTP 500 Internal Server Error
    public Result<Object> handleUnknownException(Exception ex) {
        log.error("系統未知異常! {}", ex.getMessage());
        return Result.error(MessageConstant.UNKNOWN_ERROR);
    }
}
