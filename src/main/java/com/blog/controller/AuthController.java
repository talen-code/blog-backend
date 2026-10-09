package com.blog.controller;

import com.blog.annotation.RequireLogin;
import com.blog.common.Result;
import com.blog.dto.LoginDTO;
import com.blog.dto.RegisterDTO;
import com.blog.service.UserService;
import com.blog.vo.LoginVO;
import com.blog.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * 认证接口。
 *
 * <pre>
 * POST /api/auth/register   注册（自动登录）
 * POST /api/auth/login      登录
 * POST /api/auth/logout     退出登录
 * GET  /api/auth/me         查询当前登录用户
 * </pre>
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    /** 注册。@Valid 触发 DTO 上的校验注解，失败由全局异常处理器统一返回 */
    @PostMapping("/register")
    public Result<LoginVO> register(@RequestBody @Valid RegisterDTO dto) {
        return Result.success("注册成功，已自动登录", userService.register(dto));
    }

    /** 登录 */
    @PostMapping("/login")
    public Result<LoginVO> login(@RequestBody @Valid LoginDTO dto) {
        return Result.success("登录成功", userService.login(dto));
    }

    /**
     * 退出登录。
     *
     * <p>需要把当前 token 从内存白名单里摘掉，所以从请求头里再取一次。</p>
     */
    @PostMapping("/logout")
    @RequireLogin
    public Result<Void> logout(@RequestHeader(value = "Authorization", required = false) String authorization) {
        userService.logout(stripPrefix(authorization));
        return Result.success("已退出登录", null);
    }

    /** 查询当前登录用户信息（前端刷新页面后用它恢复登录态） */
    @GetMapping("/me")
    @RequireLogin
    public Result<UserVO> me() {
        return Result.success(userService.getCurrentUser());
    }

    /** 去掉 "Bearer " 前缀，取到裸 token */
    private String stripPrefix(String authorization) {
        if (!StringUtils.hasText(authorization)) {
            return null;
        }
        String prefix = "Bearer ";
        return authorization.startsWith(prefix)
                ? authorization.substring(prefix.length()).trim()
                : authorization.trim();
    }
}
