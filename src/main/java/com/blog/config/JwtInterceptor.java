package com.blog.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blog.annotation.RequireAdmin;
import com.blog.annotation.RequireLogin;
import com.blog.common.BusinessException;
import com.blog.common.LoginUser;
import com.blog.entity.User;
import com.blog.entity.UserToken;
import com.blog.mapper.UserMapper;
import com.blog.mapper.UserTokenMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.annotation.Annotation;

/**
 * JWT 登录 / 权限拦截器。
 *
 * <p>处理流程：</p>
 * <ol>
 *   <li>从 {@code Authorization: Bearer xxx} 请求头取出 token；</li>
 *   <li>去 <b>数据库白名单表 {@code t_user_token}</b> 里查
 *       （查不到就视为未登录，所以退出登录后 token 立即失效）；</li>
 *   <li>顺手做一次惰性过期清理，避免白名单表无限膨胀；</li>
 *   <li>回查 {@code t_user} 拿到最新的角色与状态
 *       —— 好处是管理员改了某人的角色、或禁用了账号，<b>立刻生效</b>，不用等 token 过期；</li>
 *   <li>把登录用户放进 {@link LoginUserContext}，供 Service 层随时取用；</li>
 *   <li>读取方法/类上的 {@link RequireLogin} / {@link RequireAdmin} 注解做鉴权。</li>
 * </ol>
 *
 * <p>抛出 {@link BusinessException} 后会由 {@code GlobalExceptionHandler} 统一转成 JSON，
 * 因此前端看到的仍然是 HTTP 200 + {@code code:401/403}。</p>
 *
 * <p>注意：本类使用 {@code javax.servlet.*}（Spring Boot 2.7 体系）。
 * 升级 Spring Boot 3 时，只需把这里以及全局异常处理里的 javax 换成 jakarta。</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtInterceptor implements HandlerInterceptor {

    private final UserTokenMapper userTokenMapper;
    private final UserMapper userMapper;

    @Value("${blog.jwt.header:Authorization}")
    private String headerName;

    @Value("${blog.jwt.prefix:Bearer }")
    private String tokenPrefix;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 跨域预检请求（OPTIONS）不带 token，直接放行
        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            return true;
        }
        // 非 Controller 方法（静态资源、错误页等）不拦截
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        HandlerMethod handlerMethod = (HandlerMethod) handler;

        // ---------- 1. 解析 token 并还原登录用户 ----------
        LoginUser loginUser = resolveLoginUser(request);
        if (loginUser != null) {
            LoginUserContext.set(loginUser);
        }

        // ---------- 2. 鉴权 ----------
        RequireAdmin requireAdmin = findAnnotation(handlerMethod, RequireAdmin.class);
        RequireLogin requireLogin = findAnnotation(handlerMethod, RequireLogin.class);

        if ((requireAdmin != null || requireLogin != null) && loginUser == null) {
            throw BusinessException.unauthorized("请先登录后再操作");
        }
        if (requireAdmin != null && !loginUser.isAdmin()) {
            throw BusinessException.forbidden("该操作仅管理员可用");
        }
        return true;
    }

    /**
     * 请求结束后清理 ThreadLocal。
     * <p>这一步绝对不能省：Tomcat 线程池会复用线程，不清理会导致
     * 「下一个请求莫名其妙带着上一个用户的身份」这类越权问题。</p>
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        LoginUserContext.clear();
    }

    /**
     * 从请求头解析登录用户。
     *
     * @return 未登录 / token 无效 / 已过期 / 账号被禁用时返回 null
     */
    private LoginUser resolveLoginUser(HttpServletRequest request) {
        String token = resolveToken(request);
        if (!StringUtils.hasText(token)) {
            return null;
        }

        // 1. 查数据库白名单（token 上有唯一索引，selectOne 安全）
        UserToken userToken = userTokenMapper.selectOne(
                new LambdaQueryWrapper<UserToken>().eq(UserToken::getToken, token));
        if (userToken == null) {
            // 从未签发过、或用户已主动登出
            return null;
        }

        // 2. 惰性清理过期 token，省去额外的定时任务
        if (userToken.getExpireAt() != null && userToken.getExpireAt() < System.currentTimeMillis()) {
            userTokenMapper.deleteById(userToken.getId());
            log.debug("token 已过期并被移除, userId={}", userToken.getUserId());
            return null;
        }

        // 3. 回查用户，拿最新的角色与状态
        User user = userMapper.selectById(userToken.getUserId());
        if (user == null) {
            userTokenMapper.deleteById(userToken.getId());
            return null;
        }
        if (User.STATUS_DISABLED.equals(user.getStatus())) {
            // 账号被禁用 -> token 一并作废，强制下线
            userTokenMapper.delete(new LambdaQueryWrapper<UserToken>()
                    .eq(UserToken::getUserId, user.getId()));
            return null;
        }

        return new LoginUser(user.getId(), user.getUsername(), user.getNickname(),
                user.getAvatar(), user.getRole(), token, userToken.getExpireAt());
    }

    /** 取出请求头里的 token（去掉 Bearer 前缀） */
    private String resolveToken(HttpServletRequest request) {
        String value = request.getHeader(headerName);
        if (!StringUtils.hasText(value)) {
            return null;
        }
        if (StringUtils.hasText(tokenPrefix) && value.startsWith(tokenPrefix)) {
            return value.substring(tokenPrefix.length()).trim();
        }
        // 兼容前端直接传裸 token 的情况
        return value.trim();
    }

    /**
     * 先找方法上的注解，再找类上的注解。
     * <p>方法级声明优先，便于在「整个 Controller 需要管理员」的前提下单独放开某个接口。</p>
     */
    private <A extends Annotation> A findAnnotation(HandlerMethod handlerMethod, Class<A> type) {
        A annotation = handlerMethod.getMethodAnnotation(type);
        if (annotation != null) {
            return annotation;
        }
        return handlerMethod.getBeanType().getAnnotation(type);
    }
}
