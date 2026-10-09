package com.blog.common;

import com.blog.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 当前登录用户信息（登录成功后写入内存 token 白名单）。
 *
 * <p>它同时承担两个职责：</p>
 * <ol>
 *   <li>作为 JWT 里的负载（payload）来源；</li>
 *   <li>作为服务端内存 token 白名单的 value，实现「登出即失效」。</li>
 * </ol>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginUser implements Serializable {

    /** 用户 ID */
    private Long userId;

    /** 用户名 */
    private String username;

    /** 昵称 */
    private String nickname;

    /** 头像 */
    private String avatar;

    /** 角色：ADMIN / USER */
    private String role;

    /** 本次登录签发的 token 原文 */
    private String token;

    /** token 过期时间（毫秒时间戳） */
    private Long expireAt;

    /** 是否为管理员 */
    public boolean isAdmin() {
        return User.ROLE_ADMIN.equals(this.role);
    }
}
