package com.blog.dto;

import lombok.Data;

/**
 * 文章列表查询条件（前台 / 后台共用）。
 *
 * <p>这是 GET 请求的查询参数，用普通对象接收即可，不需要 {@code @RequestBody}。</p>
 */
@Data
public class ArticleQueryDTO {

    /** 页码，从 1 开始 */
    private Integer page = 1;

    /** 每页条数 */
    private Integer size = 6;

    /** 按分类筛选 */
    private Long categoryId;

    /** 按标签筛选 */
    private Long tagId;

    /** 关键词，匹配标题或简介（大小写不敏感） */
    private String keyword;

    /**
     * 状态筛选：0 草稿 / 1 已发布 / null 全部。
     * <p>只有后台接口会用到它，前台接口强制只查已发布。</p>
     */
    private Integer status;
}
