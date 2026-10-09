package com.blog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 文章标签实体 —— 对应数据库表 {@code t_tag}。
 * 标签与文章是多对多：关联关系存放在 {@code t_article_tag} 中间表。
 */
@Data
@TableName("t_tag")
public class Tag implements Serializable {

    /** 主键 ID，数据库自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 标签名称，唯一索引 uk_name */
    private String name;

    /** 标签颜色（十六进制），前端 el-tag 直接用 */
    private String color = "#409EFF";

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
