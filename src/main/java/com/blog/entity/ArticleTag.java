package com.blog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 文章-标签关联实体 —— 对应数据库表 {@code t_article_tag}。
 *
 * <p>这是标准的<b>多对多中间表</b>：内存版用一个 {@code List<Long> tagIds}
 * 塞在 Article 里，改用数据库后必须拆出来，否则无法建索引、也无法做关联查询。</p>
 *
 * <p>表上有唯一索引 {@code uk_article_tag(article_id, tag_id)}，
 * 因此同一篇文章不会重复打同一个标签。</p>
 */
@Data
@TableName("t_article_tag")
public class ArticleTag implements Serializable {

    /** 主键 ID，数据库自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 文章 ID */
    private Long articleId;

    /** 标签 ID */
    private Long tagId;
}
