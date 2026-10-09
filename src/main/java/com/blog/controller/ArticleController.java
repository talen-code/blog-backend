package com.blog.controller;

import com.blog.annotation.RequireAdmin;
import com.blog.annotation.RequireLogin;
import com.blog.common.PageResult;
import com.blog.common.Result;
import com.blog.dto.ArticleDTO;
import com.blog.dto.ArticleQueryDTO;
import com.blog.service.ArticleService;
import com.blog.vo.ArticleVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

/**
 * 文章接口。
 *
 * <pre>
 * GET    /api/articles                 分页查询已发布文章（公开）
 * GET    /api/articles/hot             热门文章（公开）
 * GET    /api/articles/{id}            文章详情，浏览量 +1（公开）
 * POST   /api/articles/{id}/like       点赞 / 取消点赞（需登录）
 * POST   /api/articles                 新增（管理员）
 * PUT    /api/articles/{id}            修改（管理员）
 * DELETE /api/articles/{id}            删除（管理员）
 * GET    /api/admin/articles           后台分页查询，含草稿（管理员）
 * GET    /api/admin/articles/{id}      后台详情，不累加浏览量（管理员）
 * </pre>
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleService articleService;

    // ==================== 前台（公开） ====================

    /**
     * 分页查询文章列表。
     * <p>查询条件通过 URL 参数绑定到 {@link ArticleQueryDTO}，例如
     * {@code /api/articles?page=1&size=6&categoryId=1&keyword=Vue}。</p>
     */
    @GetMapping("/articles")
    public Result<PageResult<ArticleVO>> page(ArticleQueryDTO query) {
        return Result.success(articleService.pagePublished(query));
    }

    /** 热门文章：按浏览量倒序 */
    @GetMapping("/articles/hot")
    public Result<List<ArticleVO>> hot(@RequestParam(defaultValue = "5") Integer limit) {
        return Result.success(articleService.listHot(limit == null ? 5 : limit));
    }

    /** 文章详情：每次访问浏览量 +1 */
    @GetMapping("/articles/{id}")
    public Result<ArticleVO> detail(@PathVariable Long id) {
        return Result.success(articleService.getDetail(id));
    }

    /** 点赞 / 取消点赞：同一个接口，后端根据当前用户是否已点赞自动切换 */
    @PostMapping("/articles/{id}/like")
    @RequireLogin
    public Result<ArticleVO> like(@PathVariable Long id) {
        return Result.success(articleService.toggleLike(id));
    }

    // ==================== 后台（管理员） ====================

    /** 后台文章列表：可以看到草稿，支持按状态筛选 */
    @GetMapping("/admin/articles")
    @RequireAdmin
    public Result<PageResult<ArticleVO>> adminPage(ArticleQueryDTO query) {
        return Result.success(articleService.pageForAdmin(query));
    }

    /** 后台文章详情：编辑页回填用，不累加浏览量 */
    @GetMapping("/admin/articles/{id}")
    @RequireAdmin
    public Result<ArticleVO> adminDetail(@PathVariable Long id) {
        return Result.success(articleService.getDetailForAdmin(id));
    }

    /** 新增文章 */
    @PostMapping("/articles")
    @RequireAdmin
    public Result<ArticleVO> create(@RequestBody @Valid ArticleDTO dto) {
        return Result.success("文章已保存", articleService.create(dto));
    }

    /** 修改文章：ID 以路径变量为准，请求体里的 id 会被忽略 */
    @PutMapping("/articles/{id}")
    @RequireAdmin
    public Result<ArticleVO> update(@PathVariable Long id, @RequestBody @Valid ArticleDTO dto) {
        return Result.success("文章已更新", articleService.update(id, dto));
    }

    /** 删除文章（连带删除其评论） */
    @DeleteMapping("/articles/{id}")
    @RequireAdmin
    public Result<Void> delete(@PathVariable Long id) {
        articleService.delete(id);
        return Result.success("文章已删除", null);
    }
}
