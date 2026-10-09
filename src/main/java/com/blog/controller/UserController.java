package com.blog.controller;

import com.blog.annotation.RequireAdmin;
import com.blog.annotation.RequireLogin;
import com.blog.common.PageResult;
import com.blog.common.Result;
import com.blog.dto.ProfileDTO;
import com.blog.dto.UserStatusDTO;
import com.blog.service.UserService;
import com.blog.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * 用户接口。
 *
 * <pre>
 * PUT    /api/users/profile              修改个人资料（需登录）
 * GET    /api/admin/users                后台分页查询用户（管理员）
 * PUT    /api/admin/users/{id}/status    启用 / 禁用（管理员）
 * DELETE /api/admin/users/{id}           删除用户（管理员）
 * </pre>
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /** 修改当前登录用户的资料（昵称、头像、邮箱、简介、密码） */
    @PutMapping("/users/profile")
    @RequireLogin
    public Result<UserVO> updateProfile(@RequestBody @Valid ProfileDTO dto) {
        return Result.success("资料已更新", userService.updateProfile(dto));
    }

    /** 后台用户列表 */
    @GetMapping("/admin/users")
    @RequireAdmin
    public Result<PageResult<UserVO>> pageForAdmin(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword) {
        return Result.success(userService.page(page, size, keyword));
    }

    /** 启用 / 禁用用户 */
    @PutMapping("/admin/users/{id}/status")
    @RequireAdmin
    public Result<UserVO> updateStatus(@PathVariable Long id, @RequestBody @Valid UserStatusDTO dto) {
        return Result.success("状态已更新", userService.updateStatus(id, dto.getStatus()));
    }

    /** 删除用户（会连带删除其评论） */
    @DeleteMapping("/admin/users/{id}")
    @RequireAdmin
    public Result<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return Result.success("用户已删除", null);
    }
}
