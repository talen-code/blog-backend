package com.blog.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 登录入参。
 *
 * <p>注意：Spring Boot 2.7 处于 javax 体系，校验注解包名是
 * {@code javax.validation.constraints.*}；升级到 Spring Boot 3 时需整体替换为 jakarta。</p>
 */
@Data
public class LoginDTO {

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;
}
