package com.blog.controller;

import com.blog.annotation.RequireAdmin;
import com.blog.annotation.RequireLogin;
import com.blog.common.PageResult;
import com.blog.common.Result;
import com.blog.dto.CommentDTO;
import com.blog.service.CommentService;
import com.blog.vo.CommentVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

/**
 * 评论接口。
 *
 * <pre>
 * GET    /api/comments?articleId=1     某篇文章的评论树（公开）
 * POST   /api/comments                 发表评论 / 回复（需登录）
 * DELETE /api/comments/{id}            删除评论（本人或管理员）
 * GET    /api/admin/comments           后台分页查询全部评论（管理员）
 * </pre>
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    /** 查询某篇文章的评论（两级树结构，顶级评论的 children 里是回复） */
    @GetMapping("/comments")
    public Result<List<CommentVO>> list(@RequestParam Long articleId) {
        return Result.success(commentService.listByArticle(articleId));
    }

    /** 发表评论；parentId 不为空时表示回复某条评论 */
    @PostMapping("/comments")
    @RequireLogin
    public Result<CommentVO> create(@RequestBody @Valid CommentDTO dto) {
        return Result.success("评论成功", commentService.create(dto));
    }

    /** 删除评论：权限在 Service 里判断（本人或管理员） */
    @DeleteMapping("/comments/{id}")
    @RequireLogin
    public Result<Void> delete(@PathVariable Long id) {
        commentService.delete(id);
        return Result.success("评论已删除", null);
    }

    /** 后台评论管理：分页 + 关键词搜索 */
    @GetMapping("/admin/comments")
    @RequireAdmin
    public Result<PageResult<CommentVO>> pageForAdmin(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword) {
        return Result.success(commentService.pageForAdmin(page, size, keyword));
    }
}
