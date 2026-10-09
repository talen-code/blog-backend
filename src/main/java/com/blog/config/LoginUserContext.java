package com.blog.config;

import com.blog.common.LoginUser;

/**
 * 当前登录用户的线程上下文。
 *
 * <p>Tomcat 每个请求由一个线程处理，因此用 {@link ThreadLocal} 存放登录信息是安全的。
 * 拦截器在 {@code preHandle} 里写入，在 {@code afterCompletion} 里<b>必须清理</b>，
 * 否则线程被复用时会读到上一个请求的用户（内存泄漏 + 越权风险）。</p>
 */
public final class LoginUserContext {

    private static final ThreadLocal<LoginUser> HOLDER = new ThreadLocal<>();

    private LoginUserContext() {
    }

    public static void set(LoginUser loginUser) {
        HOLDER.set(loginUser);
    }

    /** 可能返回 null，表示未登录 */
    public static LoginUser get() {
        return HOLDER.get();
    }

    public static Long getUserId() {
        LoginUser user = HOLDER.get();
        return user == null ? null : user.getUserId();
    }

    public static boolean isAdmin() {
        LoginUser user = HOLDER.get();
        return user != null && user.isAdmin();
    }

    /** 必须在请求结束时调用 */
    public static void clear() {
        HOLDER.remove();
    }
}
