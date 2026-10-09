package com.blog.dto;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.Size;

/**
 * 修改个人资料入参（需登录）。
 *
 * <p>所有字段都是可选的：只提交要改的字段即可。
 * password 不传表示不修改密码。</p>
 */
@Data
public class ProfileDTO {

    @Size(max = 20, message = "昵称最长 20 个字符")
    private String nickname;

    private String avatar;

    @Email(message = "邮箱格式不正确")
    private String email;

    @Size(max = 100, message = "个人简介最长 100 个字符")
    private String signature;

    /** 新密码，为空表示不修改；不为空时长度必须 6~20 */
    @Size(min = 6, max = 20, message = "密码长度必须是 6~20 位")
    private String password;
}
