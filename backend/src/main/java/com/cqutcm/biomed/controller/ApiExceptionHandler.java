package com.cqutcm.biomed.controller;

import com.cqutcm.biomed.service.AuthenticationRequiredException;
import com.cqutcm.biomed.service.AuthorizationDeniedException;
import com.cqutcm.biomed.service.StateConflictException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    /** 401 Unauthorized - 认证失败 */
    @ExceptionHandler(AuthenticationRequiredException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Map<String, Object> handleAuthenticationRequired(AuthenticationRequiredException ex) {
        return errorBody(HttpStatus.UNAUTHORIZED, ex.getMessage(), "AUTH_REQUIRED");
    }

    /** 403 Forbidden - 越权操作 */
    @ExceptionHandler(AuthorizationDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Map<String, Object> handleAuthorizationDenied(AuthorizationDeniedException ex) {
        return errorBody(HttpStatus.FORBIDDEN, ex.getMessage(), "ACCESS_DENIED");
    }

    /** 400 Bad Request - 参数校验 */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleIllegalArgument(IllegalArgumentException ex) {
        return errorBody(HttpStatus.BAD_REQUEST, ex.getMessage(), "BAD_REQUEST");
    }

    /** 409 Conflict - 状态流转冲突 */
    @ExceptionHandler(StateConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, Object> handleIllegalState(StateConflictException ex) {
        return errorBody(HttpStatus.CONFLICT, ex.getMessage(), "STATE_CONFLICT");
    }

    /** 400 - 参数校验失败 (Bean Validation) */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleValidation(MethodArgumentNotValidException ex) {
        List<Map<String, String>> errors = new ArrayList<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            errors.add(Map.of("field", fe.getField(), "message", fe.getDefaultMessage()));
        }
        Map<String, Object> body = errorBody(HttpStatus.BAD_REQUEST, "请求参数校验失败", "VALIDATION_ERROR");
        body.put("errors", errors);
        return body;
    }

    /** 400 - JSON 解析失败 */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleMessageNotReadable(HttpMessageNotReadableException ex) {
        return errorBody(HttpStatus.BAD_REQUEST, "请求格式错误，请检查 JSON 结构与字段类型", "PARSE_ERROR");
    }

    /** 400 - 缺少请求头 */
    @ExceptionHandler(MissingRequestHeaderException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleMissingHeader(MissingRequestHeaderException ex) {
        return errorBody(HttpStatus.BAD_REQUEST, "缺少必要的请求头: " + ex.getHeaderName(), "MISSING_HEADER");
    }

    /** 400 - 缺少请求参数 */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleMissingParam(MissingServletRequestParameterException ex) {
        return errorBody(HttpStatus.BAD_REQUEST, "缺少必要的请求参数: " + ex.getParameterName(), "MISSING_PARAM");
    }

    /** 400 - 参数类型不匹配 */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return errorBody(HttpStatus.BAD_REQUEST,
                String.format("参数 '%s' 类型错误，期望 %s，收到 '%s'",
                        ex.getName(),
                        ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown",
                        ex.getValue()),
                "TYPE_MISMATCH");
    }

    /** 500 - 未知异常 */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String, Object> handleUnknown(Exception ex) {
        log.error("Unhandled API exception", ex);
        Map<String, Object> body = errorBody(HttpStatus.INTERNAL_SERVER_ERROR,
                "服务器内部错误", "INTERNAL_ERROR");
        return body;
    }

    private Map<String, Object> errorBody(HttpStatus status, String message, String code) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("code", code);
        body.put("status", status.value());
        body.put("message", message);
        body.put("timestamp", LocalDateTime.now().toString());
        return body;
    }
}
