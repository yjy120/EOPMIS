package com.eopmis.common;

import lombok.Getter;

/**
 * 业务异常
 * 业务层 throw new BizException(40001, "账号已停用") 由 GlobalExceptionHandler 统一处理
 */
@Getter
public class BizException extends RuntimeException {

    private final int code;

    public BizException(String message) {
        this(500, message);
    }

    public BizException(int code, String message) {
        super(message);
        this.code = code;
    }
}
