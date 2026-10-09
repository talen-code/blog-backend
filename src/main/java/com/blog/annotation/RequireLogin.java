package com.blog.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记接口需要登录才能访问。
 *
 * <p>可以打在 Controller 类上（整个类生效），也可以打在方法上（该方法生效）。
 * 由 {@code JwtInterceptor} 在请求进入 Controller 之前校验。</p>
 *
 * <p>相比在拦截器里硬编码路径规则（如 {@code /api/admin/**}），
 * 注解方式的好处是：权限声明和接口定义在一起，改接口时不会忘记改规则。</p>
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireLogin {
}
