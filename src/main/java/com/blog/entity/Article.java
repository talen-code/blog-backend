package com.blog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 文章实体 —— 对应数据库表 {@code t_article}。
 *
 * <p><b>与内存版的重要区别</b>：</p>
 * <ol>
 *   <li>内存版把标签 ID 列表和点赞用户集合塞在实体里；改用数据库后，
 *       这两者分别落到关联表 {@code t_article_tag} 与 {@code t_article_like}，
 *       实体只保留真正的列字段 —— 这才是规范的关系型建模。</li>
 *   <li>分类名、标签名、作者昵称依然<b>不冗余存储</b>，
 *       由 Service 层用 ID 回查表并组装成 VO（等价于 JOIN）。</li>
 * </ol>
 */
@Data
@TableName("t_article")
public class Article implements Serializable {

    /** 状态常量：草稿（前台不可见） */
    public static final Integer STATUS_DRAFT = 0;
    /** 状态常量：已发布 */
    public static final Integer STATUS_PUBLISHED = 1;

    /** 主键 ID，数据库自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 文章标题 */
    private String title;

    /** 文章简介 / 摘要，用于列表页展示 */
    private String summary;

    /** 封面图地址 */
    private String cover;

    /** 正文，Markdown 原文（列类型 LONGTEXT） */
    private String content;

    /** 分类 ID（外键 -> t_category.id），可为 null 表示未分类 */
    private Long categoryId;

    /** 作者用户 ID（外键 -> t_user.id） */
    private Long authorId;

    /** 浏览量，每次访问详情页 +1 */
    private Integer views = 0;

    /** 点赞数 */
    private Integer likes = 0;

    /** 评论数，发布/删除评论时同步维护，避免列表页重复统计 */
    private Integer commentCount = 0;

    /** 状态：0 草稿，1 已发布 */
    private Integer status = STATUS_DRAFT;

    /**
     * 是否置顶：1 置顶，0 普通。
     * <p>列名故意起成 is_top：{@code top} 在部分数据库方言里是保留字，
     * 用 @TableField 显式指定列名，避免生成 SQL 时踩坑。</p>
     */
    @TableField("is_top")
    private Integer top = 0;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
