package com.blog.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * 发表评论入参（需登录）。
 */
@Data
public class CommentDTO {

    @NotNull(message = "文章 ID 不能为空")
    private Long articleId;

    @NotBlank(message = "评论内容不能为空")
    @Size(max = 500, message = "评论内容最长 500 个字符")
    private String content;

    /** 父评论 ID：为 null 表示发表顶级评论，否则表示回复某条评论 */
    private Long parentId;
}
