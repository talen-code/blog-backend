package com.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.common.BusinessException;
import com.blog.common.LoginUser;
import com.blog.common.PageResult;
import com.blog.config.LoginUserContext;
import com.blog.dto.CommentDTO;
import com.blog.entity.Article;
import com.blog.entity.Comment;
import com.blog.entity.User;
import com.blog.mapper.ArticleMapper;
import com.blog.mapper.CommentMapper;
import com.blog.mapper.UserMapper;
import com.blog.service.ArticleService;
import com.blog.service.CommentService;
import com.blog.util.DateUtil;
import com.blog.vo.CommentVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 评论服务实现 —— 操作 MySQL。
 *
 * <p>评论在数据库里是<b>平铺</b>存储的（每条记录带 parent_id），
 * 返回给前端时才组装成两级树。这样做的好处是删除某条评论时不用递归改结构。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentMapper commentMapper;
    private final ArticleMapper articleMapper;
    private final UserMapper userMapper;
    private final ArticleService articleService;

    // ==================================================================
    //  前台
    // ==================================================================

    @Override
    public List<CommentVO> listByArticle(Long articleId) {
        // 文章必须存在
        articleService.getEntity(articleId);

        // 1. 取出该文章下所有「显示中」的评论，按时间正序
        List<Comment> comments = commentMapper.selectList(new LambdaQueryWrapper<Comment>()
                .eq(Comment::getArticleId, articleId)
                .eq(Comment::getStatus, Comment.STATUS_VISIBLE)
                .orderByAsc(Comment::getCreateTime)
                .orderByAsc(Comment::getId));
        if (comments.isEmpty()) {
            return new ArrayList<>();
        }

        // 2. 批量把评论人昵称/头像查出来（1 次查询，而不是每条评论查一次）
        Map<Long, User> userMap = loadUsers(comments);

        // 3. 全部转成 VO，放进 Map 方便按 ID 找父节点
        Map<Long, CommentVO> voMap = comments.stream()
                .map(c -> toVO(c, userMap, null))
                .collect(Collectors.toMap(CommentVO::getId, Function.identity(), (a, b) -> a));

        // 4. 组装两级树
        List<CommentVO> roots = new ArrayList<>();
        for (Comment comment : comments) {
            CommentVO vo = voMap.get(comment.getId());
            if (comment.getParentId() == null) {
                roots.add(vo);
                continue;
            }
            CommentVO parent = voMap.get(comment.getParentId());
            if (parent != null) {
                parent.getChildren().add(vo);
            } else {
                // 父评论已被删除（或状态被屏蔽）：把这条降级成顶级评论，避免评论"消失"
                roots.add(vo);
            }
        }
        return roots;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommentVO create(CommentDTO dto) {
        Long userId = requireLoginUserId();
        Article article = articleService.getEntity(dto.getArticleId());
        if (!Article.STATUS_PUBLISHED.equals(article.getStatus())) {
            throw BusinessException.notFound("文章不存在或已下架，无法评论");
        }

        Comment comment = new Comment();
        comment.setArticleId(dto.getArticleId());
        comment.setUserId(userId);
        comment.setContent(dto.getContent().trim());

        // ---------- 处理回复关系 ----------
        if (dto.getParentId() != null) {
            Comment parent = commentMapper.selectById(dto.getParentId());
            if (parent == null || !parent.getArticleId().equals(dto.getArticleId())) {
                throw BusinessException.paramError("要回复的评论不存在");
            }
            // 只支持两级：回复"回复"时，挂到顶级评论下，
            // 同时记录被回复人昵称，前端展示成「回复 @某某」
            Long rootId = parent.getParentId() == null ? parent.getId() : parent.getParentId();
            comment.setParentId(rootId);
            comment.setReplyToNickname(nicknameOf(parent.getUserId()));
        }

        comment.setStatus(Comment.STATUS_VISIBLE);
        comment.setCreateTime(LocalDateTime.now());
        commentMapper.insert(comment);

        // 维护文章上的评论数：用 SQL 自增，避免并发丢更新
        articleMapper.update(null, new LambdaUpdateWrapper<Article>()
                .eq(Article::getId, dto.getArticleId())
                .setSql("comment_count = comment_count + 1"));

        log.info("新增评论: articleId={}, userId={}, parentId={}",
                dto.getArticleId(), userId, comment.getParentId());

        // 回查一次拿到最终入库数据
        Comment saved = commentMapper.selectById(comment.getId());
        return toVO(saved, loadUsers(Collections.singletonList(saved)), null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        Comment comment = getEntity(id);
        LoginUser loginUser = LoginUserContext.get();
        if (loginUser == null) {
            throw BusinessException.unauthorized("请先登录");
        }
        // 权限：本人或管理员
        boolean isOwner = comment.getUserId() != null && comment.getUserId().equals(loginUser.getUserId());
        if (!isOwner && !loginUser.isAdmin()) {
            throw BusinessException.forbidden("只能删除自己发表的评论");
        }

        // 级联删除它的所有回复
        int childRows = commentMapper.delete(new LambdaQueryWrapper<Comment>()
                .eq(Comment::getParentId, id));
        commentMapper.deleteById(id);

        // 重新统计该文章的评论数，保证绝对一致（比 += 增减更不容易出错）
        Long count = commentMapper.selectCount(new LambdaQueryWrapper<Comment>()
                .eq(Comment::getArticleId, comment.getArticleId())
                .eq(Comment::getStatus, Comment.STATUS_VISIBLE));
        articleMapper.update(null, new LambdaUpdateWrapper<Article>()
                .eq(Article::getId, comment.getArticleId())
                .set(Article::getCommentCount, count == null ? 0 : count.intValue()));

        log.info("删除评论: id={}, 连带删除 {} 条回复", id, childRows);
    }

    // ==================================================================
    //  后台
    // ==================================================================

    @Override
    public PageResult<CommentVO> pageForAdmin(int page, int size, String keyword) {
        Page<Comment> mpPage = new Page<>(page < 1 ? 1 : page, size < 1 ? 10 : size);

        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            // 关键词要同时匹配「评论内容」和「评论人昵称」。
            // 昵称在 t_user 表里，这里先查出匹配的用户 ID 再用 IN，避免拼 SQL 造成注入风险
            List<Long> userIds = userMapper.selectList(new LambdaQueryWrapper<User>()
                            .select(User::getId)
                            .like(User::getNickname, keyword))
                    .stream().map(User::getId).collect(Collectors.toList());

            wrapper.and(w -> {
                w.like(Comment::getContent, keyword);
                if (!userIds.isEmpty()) {
                    w.or().in(Comment::getUserId, userIds);
                }
            });
        }
        wrapper.orderByDesc(Comment::getCreateTime).orderByDesc(Comment::getId);

        IPage<Comment> result = commentMapper.selectPage(mpPage, wrapper);
        List<Comment> records = result.getRecords();

        // 批量补上评论人和文章标题
        Map<Long, User> userMap = loadUsers(records);
        Map<Long, String> articleTitleMap = loadArticleTitles(records);
        List<CommentVO> voList = records.stream()
                .map(c -> toVO(c, userMap, articleTitleMap))
                .collect(Collectors.toList());

        return new PageResult<>(voList, result.getTotal(), (int) result.getCurrent(), (int) result.getSize());
    }

    // ==================================================================
    //  私有工具
    // ==================================================================

    private Comment getEntity(Long id) {
        Comment comment = id == null ? null : commentMapper.selectById(id);
        if (comment == null) {
            throw BusinessException.notFound("评论不存在，id=" + id);
        }
        return comment;
    }

    /** 批量查询评论人，返回 userId -> User */
    private Map<Long, User> loadUsers(List<Comment> comments) {
        Set<Long> userIds = comments.stream().map(Comment::getUserId)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        if (userIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity(), (a, b) -> a));
    }

    /**
     * 批量查询文章标题（只 select id 和 title 两列）。
     * <p>如果直接 selectBatchIds 会把 LONGTEXT 正文也查出来，非常浪费。</p>
     */
    private Map<Long, String> loadArticleTitles(List<Comment> comments) {
        Set<Long> articleIds = comments.stream().map(Comment::getArticleId)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        if (articleIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return articleMapper.selectList(new LambdaQueryWrapper<Article>()
                        .select(Article::getId, Article::getTitle)
                        .in(Article::getId, articleIds))
                .stream()
                .collect(Collectors.toMap(Article::getId, Article::getTitle, (a, b) -> a));
    }

    /**
     * 实体 -> VO。
     *
     * @param userMap          userId -> User 的批量查询结果，可为 null（内部会单查）
     * @param articleTitleMap 文章 ID -> 标题，为 null 时不下发 articleTitle
     */
    private CommentVO toVO(Comment comment, Map<Long, User> userMap, Map<Long, String> articleTitleMap) {
        CommentVO vo = new CommentVO();
        vo.setId(comment.getId());
        vo.setArticleId(comment.getArticleId());
        vo.setUserId(comment.getUserId());
        vo.setContent(comment.getContent());
        vo.setParentId(comment.getParentId());
        vo.setReplyToNickname(comment.getReplyToNickname());
        vo.setStatus(comment.getStatus());
        vo.setCreateTime(DateUtil.format(comment.getCreateTime()));

        User user = null;
        if (userMap != null) {
            user = comment.getUserId() == null ? null : userMap.get(comment.getUserId());
        }
        if (user == null && userMap == null && comment.getUserId() != null) {
            user = userMapper.selectById(comment.getUserId());
        }
        vo.setNickname(user == null ? "已注销用户" : user.getNickname());
        vo.setAvatar(user == null ? null : user.getAvatar());

        if (articleTitleMap != null) {
            vo.setArticleTitle(articleTitleMap.getOrDefault(comment.getArticleId(), "文章已删除"));
        }
        return vo;
    }

    private String nicknameOf(Long userId) {
        User user = userId == null ? null : userMapper.selectById(userId);
        return user == null ? "已注销用户" : user.getNickname();
    }

    private Long requireLoginUserId() {
        Long userId = LoginUserContext.getUserId();
        if (userId == null) {
            throw BusinessException.unauthorized("请先登录后再评论");
        }
        return userId;
    }
}
