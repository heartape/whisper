package com.heartape.whisper.config;

import com.heartape.whisper.common.Result;
import com.heartape.whisper.exception.BusinessException;
import com.heartape.whisper.exception.SystemException;
import com.heartape.whisper.exception.UnauthorizedException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Objects;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public Result<?> handleBusiness(BusinessException e) {
        log.error(e.getMessage());
        return Result.error(e.getCode());
    }

    /**
     * 未授权异常
     */
    @ExceptionHandler(UnauthorizedException.class)
    public Result<?> handleUnauthorized(UnauthorizedException e) {
        log.error(e.getMessage());
        return Result.error(401);
    }

    /**
     * 参数校验异常（@Valid）
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<?> handleValidException(MethodArgumentNotValidException e) {
        String msg = Objects.requireNonNull(e.getBindingResult()
                        .getFieldError())
                .getDefaultMessage();
        log.error(e.getMessage());
        log.error(msg);
        return Result.error(400);
    }

    /**
     * 单参数校验异常
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public Result<?> handleConstraint(ConstraintViolationException e) {
        log.error(e.getMessage());
        return Result.error(400);
    }

    /**
     * 系统错误
     */
    @ExceptionHandler(SystemException.class)
    public Result<?> handleNpe(SystemException e) {
        log.error(e.getMessage());
        return Result.error(500);
    }

    /**
     * 兜底异常
     */
    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e) {
        log.error(e.getMessage());
        log.error(e.getCause().toString());
        log.error(Arrays.toString(e.getStackTrace()));
        return Result.error(500);
    }
}
