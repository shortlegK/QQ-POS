package com.qqriceball.server.handler;


import com.qqriceball.common.exception.*;
import com.qqriceball.common.result.Result;
import com.qqriceball.enumeration.MessageEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {


    @ExceptionHandler(AccountNotExistException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED) // 回傳 HTTP 401 Unauthorized
    public Result<Object> handleAccountNotFound(AccountNotExistException ex) {
        log.error("操作異常: {}", ex.getMessage());
        return Result.error(ex.getMessageEnum());
    }

    @ExceptionHandler(PasswordErrorException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED) // 回傳 HTTP 401 Unauthorized
    public Result<Object> handlePasswordError(PasswordErrorException ex) {
        log.error("操作異常: {}", ex.getMessage());
        return Result.error(ex.getMessageEnum());
    }

    @ExceptionHandler(AccountInactiveException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN) // 回傳 HTTP 403 Forbidden
    public Result<Object> handleAccountLocked(AccountInactiveException ex) {
        log.error("操作異常: {}", ex.getMessage());
        return Result.error(ex.getMessageEnum());
    }


    @ExceptionHandler(AlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT) // 回傳 HTTP 409 Conflict
    public Result<Object> handleAlreadyExists(AlreadyExistsException ex) {
        log.error("操作異常: {}", ex.getMessageEnum().getMessage());
        return Result.error(ex.getMessageEnum());
    }

    @ExceptionHandler(OptionNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND) // 回傳 HTTP 404 Not Found
    public Result<Object> handleOptionNotFound(OptionNotFoundException ex) {
        log.error("操作異常: {}", ex.getMessageEnum().getMessage());
        return Result.error(ex.getMessageEnum());
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

        return Result.error(MessageEnum.BAD_REQUEST, msg);
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
        log.error("系統未知異常! {}", ex.getMessage());
        return Result.error(MessageEnum.UNKNOWN_ERROR);
    }
}
