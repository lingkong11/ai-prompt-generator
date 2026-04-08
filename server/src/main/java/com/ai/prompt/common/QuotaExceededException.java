package com.ai.prompt.common;

/**
 * 配额不足异常
 *
 * @author 马可行
 * @since 1.2.0
 */
public class QuotaExceededException extends RuntimeException {
    public QuotaExceededException(String message) {
        super(message);
    }
}
