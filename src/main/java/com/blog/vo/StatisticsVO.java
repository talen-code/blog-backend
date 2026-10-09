package com.blog.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 后台数据统计面板出参。
 */
@Data
public class StatisticsVO implements Serializable {

    /** 用户总数 */
    private Integer userCount;

    /** 文章总数（含草稿） */
    private Integer articleCount;

    /** 已发布文章数 */
    private Integer publishedCount;

    /** 草稿数 */
    private Integer draftCount;

    /** 分类数 */
    private Integer categoryCount;

    /** 标签数 */
    private Integer tagCount;

    /** 评论总数 */
    private Integer commentCount;

    /** 总浏览量 */
    private Long totalViews;

    /** 总点赞数 */
    private Long totalLikes;

    /** 分类文章占比：[{name:"前端开发", value:4}, ...] */
    private List<NameValueVO> categoryStats = new ArrayList<>();

    /** 最新 5 篇文章（按创建时间倒序） */
    private List<ArticleVO> latestArticles = new ArrayList<>();
}
