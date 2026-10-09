package com.blog.dto;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * 启用 / 禁用用户入参（管理员）。
 */
@Data
public class UserStatusDTO {

    @NotNull(message = "状态值不能为空")
    @Min(value = 0, message = "状态值只能是 0（禁用）或 1（正常）")
    @Max(value = 1, message = "状态值只能是 0（禁用）或 1（正常）")
    private Integer status;
}
