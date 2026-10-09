package com.blog.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 标签出参对象。
 */
@Data
public class TagVO implements Serializable {

    private Long id;
    private String name;

    /** 十六进制颜色，前端 el-tag 直接绑定 */
    private String color;

    /** 该标签下的文章数（仅统计已发布） */
    private Integer articleCount;

    private String createTime;
}
