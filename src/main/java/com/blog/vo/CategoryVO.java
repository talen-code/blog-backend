package com.blog.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 分类出参对象。
 */
@Data
public class CategoryVO implements Serializable {

    private Long id;
    private String name;
    private String description;
    private Integer sort;

    /** 该分类下的文章数（仅统计已发布） */
    private Integer articleCount;

    private String createTime;
}
