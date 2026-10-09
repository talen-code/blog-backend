package com.blog.common;

import lombok.Getter;

/**
 * 业务异常。
 *
 * <p>Service 层校验不通过时直接抛出，由 {@code GlobalExceptionHandler} 统一捕获并转成
 * {@link Result}，避免在 Controller 里写大量 if-else 判断。</p>
 *
 * <p>继承 {@link RuntimeException} 是为了不污染方法签名。JVM 有性能优化，
 * 异常本身不会成为性能瓶颈。</p>
 */
@Getter
public class BusinessException extends RuntimeException {

    /** 业务状态码 */
    private final int code;

    /** 直接指定 code */
    public BusinessException(int code, String message) {
        // 关闭栈追踪可以提升抛异常的性能，同时这里也不需要堆栈信息
        super(message, null, false, false);
        this.code = code;
    }

    /** 使用枚举状态码 */
    public BusinessException(ResultCode resultCode) {
        this(resultCode.getCode(), resultCode.getMessage());
    }

    /** 使用枚举状态码 + 自定义提示语（最常用） */
    public BusinessException(ResultCode resultCode, String message) {
        this(resultCode.getCode(), message);
    }

    /** 便捷方法：未登录 */
    public static BusinessException unauthorized(String message) {
        return new BusinessException(ResultCode.UNAUTHORIZED, message);
    }

    /** 便捷方法：权限不足 */
    public static BusinessException forbidden(String message) {
        return new BusinessException(ResultCode.FORBIDDEN, message);
    }

    /** 便捷方法：资源不存在 */
    public static BusinessException notFound(String message) {
        return new BusinessException(ResultCode.NOT_FOUND, message);
    }

    /** 便捷方法：参数错误 */
    public static BusinessException paramError(String message) {
        return new BusinessException(ResultCode.PARAM_ERROR, message);
    }
}
