package com.blog.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 分类新增 / 修改入参（管理员）。
 */
@Data
public class CategoryDTO {

    @NotBlank(message = "分类名称不能为空")
    @Size(max = 20, message = "分类名称最长 20 个字符")
    private String name;

    @Size(max = 100, message = "分类描述最长 100 个字符")
    private String description;

    /** 排序值，越小越靠前 */
    private Integer sort = 0;
}
