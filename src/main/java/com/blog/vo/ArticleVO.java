package com.blog.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 文章出参对象。
 *
 * <p>这是「内存 join」的结果：分类名、标签、作者昵称/头像都是在 Service 层
 * 用 ID 去其它 Map 里查出来拼上的，等价于数据库的多表关联查询。</p>
 */
@Data
public class ArticleVO implements Serializable {

    private Long id;
    private String title;
    private String summary;
    private String cover;

    /** Markdown 正文：列表接口为 null，详情接口才有值，避免列表响应过大 */
    private String content;

    private Long categoryId;
    private String categoryName;

    private List<Long> tagIds = new ArrayList<>();
    private List<TagVO> tags = new ArrayList<>();

    private Long authorId;
    private String authorName;
    private String authorAvatar;

    private Integer views;
    private Integer likes;
    private Integer commentCount;

    /** 0 草稿 / 1 已发布 */
    private Integer status;

    /** 1 置顶 / 0 普通 */
    private Integer top;

    /** 当前登录用户是否已点赞；未登录恒为 false */
    private Boolean liked = false;

    private String createTime;
    private String updateTime;
}
