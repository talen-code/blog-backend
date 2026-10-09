package com.blog.controller;

import com.blog.annotation.RequireAdmin;
import com.blog.common.Result;
import com.blog.dto.TagDTO;
import com.blog.service.TagService;
import com.blog.vo.TagVO;
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
 * 标签接口。
 *
 * <pre>
 * GET    /api/tags        标签列表（公开）
 * POST   /api/tags        新增（管理员）
 * PUT    /api/tags/{id}   修改（管理员）
 * DELETE /api/tags/{id}   删除（管理员，会从所有文章上摘除该标签）
 * </pre>
 */
@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    @GetMapping
    public Result<List<TagVO>> list() {
        return Result.success(tagService.list());
    }

    @PostMapping
    @RequireAdmin
    public Result<TagVO> create(@RequestBody @Valid TagDTO dto) {
        return Result.success("标签已创建", tagService.create(dto));
    }

    @PutMapping("/{id}")
    @RequireAdmin
    public Result<TagVO> update(@PathVariable Long id, @RequestBody @Valid TagDTO dto) {
        return Result.success("标签已更新", tagService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @RequireAdmin
    public Result<Void> delete(@PathVariable Long id) {
        tagService.delete(id);
        return Result.success("标签已删除", null);
    }
}
