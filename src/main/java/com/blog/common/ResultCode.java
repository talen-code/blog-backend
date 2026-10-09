package com.blog.common;

import lombok.Getter;

/**
 * 统一业务状态码。
 *
 * <p>约定：HTTP 状态码一律返回 200，真正的业务结果看响应体里的 {@code code} 字段。
 * 这样前端 axios 拦截器只需判断 body.code 即可，无需区分 HTTP 状态码分支。</p>
 */
@Getter
public enum ResultCode {

    /** 成功 */
    SUCCESS(200, "操作成功"),
    /** 参数校验失败 */
    PARAM_ERROR(400, "参数校验失败"),
    /** 未登录 / token 失效 */
    UNAUTHORIZED(401, "未登录或登录已过期"),
    /** 已登录但权限不足 */
    FORBIDDEN(403, "权限不足，拒绝访问"),
    /** 资源不存在 */
    NOT_FOUND(404, "请求的资源不存在"),
    /** 业务处理失败（用户名已存在等） */
    BUSINESS_ERROR(500, "业务处理失败"),
    /** 服务器内部错误 */
    SYSTEM_ERROR(500, "系统繁忙，请稍后再试");

    /** 业务状态码 */
    private final int code;
    /** 默认提示文案 */
    private final String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
