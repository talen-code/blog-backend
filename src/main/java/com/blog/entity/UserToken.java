package com.blog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * JWT 登录令牌 —— 对应数据库表 {@code t_user_token}。
 *
 * <p>JWT 本身是无状态的，服务端本可以不存。这里额外落一张表是为了实现
 * <b>"退出登录后 token 立即失效"</b> 以及"重启服务后登录态不丢失"。</p>
 *
 * <p>对比最初的纯内存版本：那时白名单放在一个 Map 里，服务一重启就全部掉线；
 * 落库之后重启也能保持登录态。</p>
 *
 * <p><b>生产建议</b>：这张表会随登录量快速增长，
 * 真实项目通常改用 Redis（设置 TTL 自动过期）而不是 MySQL 表。</p>
 */
@Data
@TableName("t_user_token")
public class UserToken implements Serializable {

    /** 主键 ID，数据库自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** JWT 原文，唯一索引 uk_token */
    private String token;

    /** 所属用户 ID */
    private Long userId;

    /** 过期时间（毫秒时间戳），比直接存 DATETIME 更容易做比较 */
    private Long expireAt;

    /** 签发时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
