package com.blog.controller;

import com.blog.annotation.RequireAdmin;
import com.blog.common.Result;
import com.blog.dto.CategoryDTO;
import com.blog.service.CategoryService;
import com.blog.vo.CategoryVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

/**
 * 分类接口。
 *
 * <pre>
 * GET    /api/categories        分类列表（公开）
 * POST   /api/categories        新增（管理员）
 * PUT    /api/categories/{id}   修改（管理员）
 * DELETE /api/categories/{id}   删除（管理员，分类下有文章时会被拒绝）
 * </pre>
 */
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public Result<List<CategoryVO>> list() {
        return Result.success(categoryService.list());
    }

    @PostMapping
    @RequireAdmin
    public Result<CategoryVO> create(@RequestBody @Valid CategoryDTO dto) {
        return Result.success("分类已创建", categoryService.create(dto));
    }

    @PutMapping("/{id}")
    @RequireAdmin
    public Result<CategoryVO> update(@PathVariable Long id, @RequestBody @Valid CategoryDTO dto) {
        return Result.success("分类已更新", categoryService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @RequireAdmin
    public Result<Void> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return Result.success("分类已删除", null);
    }
}
