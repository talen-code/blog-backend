package com.blog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 文章点赞记录 —— 对应数据库表 {@code t_article_like}。
 *
 * <p>内存版用一个 {@code Set<Long> likedUserIds} 记录谁点过赞，
 * 数据库版必须落成一张表，才能支撑"查当前用户是否点过赞"和唯一性约束。</p>
 *
 * <p>表上有唯一索引 {@code uk_article_user(article_id, user_id)}，
 * 从数据库层面保证同一用户对同一篇文章只能有一条点赞记录
 * （即使代码有并发 Bug，也不会产生重复数据）。</p>
 */
@Data
@TableName("t_article_like")
public class ArticleLike implements Serializable {

    /** 主键 ID，数据库自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 文章 ID */
    private Long articleId;

    /** 点赞用户 ID */
    private Long userId;

    /** 点赞时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
