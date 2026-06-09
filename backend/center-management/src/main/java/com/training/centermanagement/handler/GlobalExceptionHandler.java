package com.training.centermanagement.handler;

import com.training.centermanagement.dto.Result;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 * 统一捕获 Controller 层抛出的异常，向前端返回友好的错误信息
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Result<Void>> handleDataIntegrity(DataIntegrityViolationException e) {
        String msg = "数据完整性冲突";
        String detail = e.getMostSpecificCause().getMessage();
        if (detail.contains("foreign key")) {
            msg = "操作失败：存在关联数据，请先处理关联记录后再试";
        } else if (detail.contains("Duplicate")) {
            msg = "操作失败：数据已存在，不允许重复添加";
        } else if (detail.contains("chk_capacity")) {
            msg = "操作失败：班级已满员";
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Result.fail(400, msg));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Result<Void>> handleMissingParam(MissingServletRequestParameterException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Result.fail(400, "缺少必要参数: " + e.getParameterName()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Result<Void>> handleBadBody(HttpMessageNotReadableException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Result.fail(400, "请求体格式错误，请检查 JSON 格式"));
    }

    @ExceptionHandler(NumberFormatException.class)
    public ResponseEntity<Result<Void>> handleNumberFormat(NumberFormatException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Result.fail(400, "数字格式错误: " + e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Void>> handleGeneral(Exception e) {
        e.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Result.fail(500, "服务器内部错误: " + e.getClass().getSimpleName()));
    }
}