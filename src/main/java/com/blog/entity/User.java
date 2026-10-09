package com.blog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户实体 —— 对应数据库表 {@code t_user}。
 *
 * <p>MyBatis-Plus 注解说明：</p>
 * <ul>
 *   <li>{@code @TableName("t_user")}：实体与表的映射（表名不用 t_ 前缀时也可省略，默认按类名转下划线）</li>
 *   <li>{@code @TableId(type = IdType.AUTO)}：主键为数据库自增，插入后 MP 会把自增 ID 回填到实体</li>
 *   <li>字段名 createTime 与列名 create_time 的映射靠 {@code map-underscore-to-camel-case: true} 自动完成</li>
 * </ul>
 */
@Data
@TableName("t_user")
public class User implements Serializable {

    /** 用户角色常量：管理员 */
    public static final String ROLE_ADMIN = "ADMIN";
    /** 用户角色常量：普通用户 */
    public static final String ROLE_USER = "USER";

    /** 状态常量：正常 */
    public static final Integer STATUS_NORMAL = 1;
    /** 状态常量：禁用 */
    public static final Integer STATUS_DISABLED = 0;

    /** 主键 ID，数据库自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 登录用户名，唯一索引 uk_username */
    private String username;

    /** 密码摘要（MD5 十六进制），绝不返回给前端 */
    @JsonIgnore
    private String password;

    /** 昵称，页面上展示的名字 */
    private String nickname;

    /** 头像地址（使用在线占位图，无需本地静态资源） */
    private String avatar;

    /** 邮箱 */
    private String email;

    /** 个人简介 */
    private String signature;

    /** 角色：ADMIN / USER */
    private String role;

    /** 状态：1 正常，0 禁用（禁用后无法登录） */
    private Integer status;

    /** 注册时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /** 最近登录时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastLoginTime;

    /** 是否为管理员（封装判断，避免各处硬编码字符串比较） */
    public boolean isAdmin() {
        return ROLE_ADMIN.equals(this.role);
    }
}
