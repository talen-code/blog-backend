package com.blog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 文章分类实体 —— 对应数据库表 {@code t_category}。
 * 分类与文章是一对多：一个分类下可有多篇文章。
 */
@Data
@TableName("t_category")
public class Category implements Serializable {

    /** 主键 ID，数据库自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 分类名称，唯一索引 uk_name */
    private String name;

    /** 分类描述 */
    private String description;

    /** 排序值，越小越靠前 */
    private Integer sort = 0;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
