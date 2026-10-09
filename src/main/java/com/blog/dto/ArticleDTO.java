package com.blog.dto;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

/**
 * 文章新增 / 修改入参（管理员使用）。
 */
@Data
public class ArticleDTO {

    /** 修改时由路径变量提供，请求体中可不传 */
    private Long id;

    @NotBlank(message = "文章标题不能为空")
    @Size(max = 100, message = "标题最长 100 个字符")
    private String title;

    @Size(max = 300, message = "简介最长 300 个字符")
    private String summary;

    /** 封面图地址，不传时由后端用占位图补齐 */
    private String cover;

    @NotBlank(message = "文章正文不能为空")
    private String content;

    /** 分类 ID，可为空表示未分类 */
    private Long categoryId;

    /** 标签 ID 列表 */
    private List<Long> tagIds = new ArrayList<>();

    /** 状态：0 草稿，1 已发布；默认草稿，避免误发布 */
    @Min(value = 0, message = "状态值只能是 0（草稿）或 1（已发布）")
    @Max(value = 1, message = "状态值只能是 0（草稿）或 1（已发布）")
    private Integer status = 0;

    /** 是否置顶：1 是，0 否 */
    @Min(value = 0, message = "置顶值只能是 0 或 1")
    @Max(value = 1, message = "置顶值只能是 0 或 1")
    private Integer top = 0;
}
