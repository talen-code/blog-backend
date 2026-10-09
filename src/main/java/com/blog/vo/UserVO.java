package com.blog.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户出参对象。
 *
 * <p>与实体 {@code User} 的区别：<b>绝不包含 password 字段</b>。
 * 这是对外输出与内部存储解耦的关键，即使实体注解漏了 {@code @JsonIgnore}，
 * 也不会因为在 Controller 里返回实体而泄露密码。</p>
 */
@Data
public class UserVO implements Serializable {

    private Long id;
    private String username;
    private String nickname;
    private String avatar;
    private String email;
    private String signature;

    /** ADMIN / USER */
    private String role;

    /** 1 正常 / 0 禁用 */
    private Integer status;

    private String createTime;
    private String lastLoginTime;
}
