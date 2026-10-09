package com.blog.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.io.Serializable;

/**
 * 统一响应结果封装。
 *
 * <p>所有 Controller 一律返回 {@code Result<T>}，JSON 结构固定为：</p>
 * <pre>
 * {
 *   "code": 200,
 *   "msg": "操作成功",
 *   "data": { ... },
 *   "timestamp": 1710000000000
 * }
 * </pre>
 *
 * @param <T> 业务数据类型
 */
@Data
public class Result<T> implements Serializable {

    /** 业务状态码，200 表示成功 */
    private int code;

    /** 提示信息，失败时前端可直接 toast 展示 */
    private String msg;

    /** 业务数据，失败时为 null */
    private T data;

    /** 服务器时间戳，便于前端排查/时钟校准 */
    private long timestamp = System.currentTimeMillis();

    /** 工具类禁止实例化 */
    private Result() {
    }

    private Result(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }

    // ==================== 成功 ====================

    /** 成功且无返回数据 */
    public static <T> Result<T> success() {
        return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), null);
    }

    /** 成功并携带数据 */
    public static <T> Result<T> success(T data) {
        return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), data);
    }

    /** 成功并自定义提示语 */
    public static <T> Result<T> success(String msg, T data) {
        return new Result<>(ResultCode.SUCCESS.getCode(), msg, data);
    }

    // ==================== 失败 ====================

    /** 使用枚举返回失败 */
    public static <T> Result<T> error(ResultCode resultCode) {
        return new Result<>(resultCode.getCode(), resultCode.getMessage(), null);
    }

    /** 使用枚举 + 自定义提示语返回失败 */
    public static <T> Result<T> error(ResultCode resultCode, String msg) {
        return new Result<>(resultCode.getCode(), msg, null);
    }

    /** 自定义 code + msg 返回失败 */
    public static <T> Result<T> error(int code, String msg) {
        return new Result<>(code, msg, null);
    }

    /**
     * 快速判断是否成功（仅后端内部使用）。
     * <p>加上 {@code @JsonIgnore} 是为了保持对外 JSON 结构严格为
     * {@code {code, msg, data, timestamp}} 四个字段。</p>
     */
    @JsonIgnore
    public boolean isSuccess() {
        return this.code == ResultCode.SUCCESS.getCode();
    }
}
