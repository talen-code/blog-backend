package com.blog.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * 标签新增 / 修改入参（管理员）。
 */
@Data
public class TagDTO {

    @NotBlank(message = "标签名称不能为空")
    @Size(max = 20, message = "标签名称最长 20 个字符")
    private String name;

    /** 标签颜色，必须形如 #409EFF */
    @Pattern(regexp = "^#[0-9a-fA-F]{6}$", message = "标签颜色必须是 #RRGGBB 格式")
    private String color = "#409EFF";
}
