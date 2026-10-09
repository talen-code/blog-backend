package com.blog.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 登录 / 注册成功后的返回体。
 */
@Data
public class LoginVO implements Serializable {

    /** JWT 字符串 */
    private String token;

    /** 令牌类型，固定 Bearer，前端拼 Authorization 头时使用 */
    private String tokenType = "Bearer";

    /** 有效期（毫秒），前端据此决定是否要提前续期 */
    private Long expiresIn;

    /** 登录用户信息 */
    private UserVO user;
}
