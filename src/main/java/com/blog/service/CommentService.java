package com.blog.service;

import com.blog.common.PageResult;
import com.blog.dto.CommentDTO;
import com.blog.vo.CommentVO;

import java.util.List;

/**
 * 评论服务。
 */
public interface CommentService {

    /** 前台：查询某篇文章的评论（两级树结构） */
    List<CommentVO> listByArticle(Long articleId);

    /** 发表评论 / 回复（需登录） */
    CommentVO create(CommentDTO dto);

    /** 删除评论：仅本人或管理员可删 */
    void delete(Long id);

    /** 后台：分页查询全部评论 */
    PageResult<CommentVO> pageForAdmin(int page, int size, String keyword);
}
