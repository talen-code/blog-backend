package com.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blog.entity.Article;
import com.blog.entity.Comment;
import com.blog.mapper.ArticleMapper;
import com.blog.mapper.CategoryMapper;
import com.blog.mapper.CommentMapper;
import com.blog.mapper.TagMapper;
import com.blog.mapper.UserMapper;
import com.blog.service.ArticleService;
import com.blog.service.StatisticsService;
import com.blog.vo.StatisticsVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 数据统计服务实现 —— 全部由 MySQL 聚合得出。
 *
 * <p>内存版靠遍历 Map 累加；数据库版交给 SQL：
 * {@code COUNT(*)}、{@code SUM(views)}、{@code GROUP BY category_id}。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final UserMapper userMapper;
    private final ArticleMapper articleMapper;
    private final CategoryMapper categoryMapper;
    private final TagMapper tagMapper;
    private final CommentMapper commentMapper;
    private final ArticleService articleService;

    @Override
    public StatisticsVO overview() {
        StatisticsVO vo = new StatisticsVO();

        // ---------- 基础计数：SELECT COUNT(*) FROM ... ----------
        vo.setUserCount(toInt(userMapper.selectCount(null)));
        vo.setArticleCount(toInt(articleMapper.selectCount(null)));
        vo.setCategoryCount(toInt(categoryMapper.selectCount(null)));
        vo.setTagCount(toInt(tagMapper.selectCount(null)));
        vo.setCommentCount(toInt(commentMapper.selectCount(null)));

        // ---------- 文章状态分布：WHERE status = 1 ----------
        int published = toInt(articleMapper.selectCount(
                new LambdaQueryWrapper<Article>().eq(Article::getStatus, Article.STATUS_PUBLISHED)));
        vo.setPublishedCount(published);
        vo.setDraftCount(vo.getArticleCount() - published);

        // ---------- 求和：SELECT SUM(views), SUM(likes) ----------
        Long totalViews = articleMapper.sumViews();
        Long totalLikes = articleMapper.sumLikes();
        vo.setTotalViews(totalViews == null ? 0L : totalViews);
        vo.setTotalLikes(totalLikes == null ? 0L : totalLikes);

        // ---------- 分类文章占比（GROUP BY + LEFT JOIN） ----------
        vo.setCategoryStats(articleMapper.countGroupByCategoryName());

        // ---------- 最新 5 篇文章 ----------
        vo.setLatestArticles(articleService.listLatest(5));

        // 派生指标示例：今日新增评论
        Long todayComments = commentMapper.selectCount(new LambdaQueryWrapper<Comment>()
                .ge(Comment::getCreateTime, LocalDateTime.now().toLocalDate().atStartOfDay()));
        log.debug("统计数据生成完成，今日新增评论 {} 条", todayComments);

        return vo;
    }

    private int toInt(Long value) {
        return value == null ? 0 : value.intValue();
    }
}
