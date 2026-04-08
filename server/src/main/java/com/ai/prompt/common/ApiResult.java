package com.ai.prompt.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 统一 API 响应包装
 *
 * <p>所有 Controller 接口统一通过此类返回数据，前端可根据 code 字段判断请求状态。
 * 成功响应使用 {@link #success(Object)} 工厂方法，失败响应使用 {@link #error(int, String)}。</p>
 *
 * @param <T> 响应载荷类型
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResult<T> {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /** 业务状态码，0 表示成功 */
    private int code;

    /** 状态描述，成功时为 "ok" */
    private String message;

    /** 业务数据 */
    private T data;

    /** 服务端响应时间 */
    private String timestamp;

    private ApiResult(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = LocalDateTime.now().format(FMT);
    }

    /**
     * 构建成功响应
     *
     * @param data 业务数据
     * @return code=0, message="ok" 的响应对象
     */
    public static <T> ApiResult<T> success(T data) {
        return new ApiResult<>(0, "ok", data);
    }

    /**
     * 构建成功响应（无载荷）
     */
    public static <T> ApiResult<T> success() {
        return new ApiResult<>(0, "ok", null);
    }

    /**
     * 构建 error 响应
     *
     * @param code    HTTP 兼容状态码（如 400、401、404、500）
     * @param message 可读的错误描述
     */
    public static <T> ApiResult<T> error(int code, String message) {
        return new ApiResult<>(code, message, null);
    }

    /**
     * 便捷工厂：参数校验不通过 → 400
     */
    public static <T> ApiResult<T> badRequest(String message) {
        return error(400, message);
    }

    /**
     * 便捷工厂：未认证 → 401
     */
    public static <T> ApiResult<T> unauthorized(String message) {
        return error(401, message);
    }
}
