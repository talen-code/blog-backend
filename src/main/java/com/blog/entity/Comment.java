package com.blog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 评论实体 —— 对应数据库表 {@code t_comment}。
 *
 * <p>支持两级评论（楼中楼）：
 * parentId 为 null 表示顶级评论，否则表示对某条评论的回复。
 * 这里只做两级，不做无限嵌套，前端渲染更简单。</p>
 */
@Data
@TableName("t_comment")
public class Comment implements Serializable {

    /** 状态常量：已显示 */
    public static final Integer STATUS_VISIBLE = 1;
    /** 状态常量：已屏蔽 */
    public static final Integer STATUS_HIDDEN = 0;

    /** 主键 ID，数据库自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属文章 ID（外键 -> t_article.id） */
    private Long articleId;

    /** 评论人 ID（外键 -> t_user.id） */
    private Long userId;

    /** 评论内容（纯文本，不做 Markdown 渲染，防止 XSS） */
    private String content;

    /** 父评论 ID，null 表示顶级评论 */
    private Long parentId;

    /** 被回复人昵称（冗余字段，仅用于展示 "@某某"，避免每次都要多查一次用户表） */
    private String replyToNickname;

    /** 状态：1 显示，0 屏蔽 */
    private Integer status = STATUS_VISIBLE;

    /** 评论时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
