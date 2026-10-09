package com.blog.service;

import com.blog.common.PageResult;
import com.blog.dto.ArticleDTO;
import com.blog.dto.ArticleQueryDTO;
import com.blog.entity.Article;
import com.blog.vo.ArticleVO;

import java.util.List;

/**
 * 文章服务。
 */
public interface ArticleService {

    /** 前台：分页查询「已发布」文章，支持分类 / 标签 / 关键词筛选 */
    PageResult<ArticleVO> pagePublished(ArticleQueryDTO query);

    /** 前台：热门文章（按浏览量倒序） */
    List<ArticleVO> listHot(int limit);

    /** 后台：最新文章（按创建时间倒序，统计面板用） */
    List<ArticleVO> listLatest(int limit);

    /** 前台：文章详情，会累加浏览量；草稿或不存在时抛 404 */
    ArticleVO getDetail(Long id);

    /** 点赞 / 取消点赞（需登录），返回最新文章信息 */
    ArticleVO toggleLike(Long id);

    /** 后台：分页查询文章（含草稿），支持状态筛选 */
    PageResult<ArticleVO> pageForAdmin(ArticleQueryDTO query);

    /** 后台：按 ID 查询详情（不累加浏览量，草稿也能查） */
    ArticleVO getDetailForAdmin(Long id);

    /** 后台：新增文章 */
    ArticleVO create(ArticleDTO dto);

    /** 后台：修改文章 */
    ArticleVO update(Long id, ArticleDTO dto);

    /** 后台：删除文章（连带删除其评论、标签关联、点赞记录） */
    void delete(Long id);

    /** 按 ID 取实体，不存在时抛业务异常（供其它服务复用） */
    Article getEntity(Long id);
}
