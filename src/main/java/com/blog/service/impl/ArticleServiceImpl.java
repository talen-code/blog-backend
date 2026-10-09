package com.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.common.BusinessException;
import com.blog.common.PageResult;
import com.blog.common.ResultCode;
import com.blog.config.LoginUserContext;
import com.blog.dto.ArticleDTO;
import com.blog.dto.ArticleQueryDTO;
import com.blog.entity.Article;
import com.blog.entity.ArticleLike;
import com.blog.entity.ArticleTag;
import com.blog.entity.Category;
import com.blog.entity.Comment;
import com.blog.entity.Tag;
import com.blog.entity.User;
import com.blog.mapper.ArticleLikeMapper;
import com.blog.mapper.ArticleMapper;
import com.blog.mapper.ArticleTagMapper;
import com.blog.mapper.CategoryMapper;
import com.blog.mapper.CommentMapper;
import com.blog.mapper.TagMapper;
import com.blog.mapper.UserMapper;
import com.blog.service.ArticleService;
import com.blog.util.DateUtil;
import com.blog.vo.ArticleVO;
import com.blog.vo.TagVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 文章服务实现 —— 操作 MySQL。
 *
 * <p>内存版的 {@code stream().filter().sorted()} 现在变成了真正的 SQL：</p>
 * <ul>
 *   <li>{@code LambdaQueryWrapper.eq/like/in} → WHERE</li>
 *   <li>{@code orderByDesc} → ORDER BY</li>
 *   <li>{@code selectPage} → LIMIT offset, size（需要 PaginationInnerInterceptor）</li>
 *   <li>{@code assemble(...)} → 用 ID 批量回查分类/标签/作者，等价于 JOIN</li>
 * </ul>
 *
 * <p><b>刻意规避 N+1</b>：一页 10 篇文章，如果每篇都去查一次分类、标签、作者，
 * 就是 40 次查询。这里统一改成"先收集 ID，再批量查，最后在内存里拼装"，
 * 整页只需要固定的 5 次查询。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleServiceImpl implements ArticleService {

    /** 新建文章时的默认封面 */
    private static final String COVER_TPL = "https://picsum.photos/seed/blog";
    /** 列表默认每页条数 */
    private static final int DEFAULT_SIZE = 6;

    private final ArticleMapper articleMapper;
    private final CategoryMapper categoryMapper;
    private final TagMapper tagMapper;
    private final ArticleTagMapper articleTagMapper;
    private final ArticleLikeMapper articleLikeMapper;
    private final CommentMapper commentMapper;
    private final UserMapper userMapper;

    // ==================================================================
    //  前台接口
    // ==================================================================

    @Override
    public PageResult<ArticleVO> pagePublished(ArticleQueryDTO query) {
        ArticleQueryDTO q = query == null ? new ArticleQueryDTO() : query;
        int current = q.getPage() == null || q.getPage() < 1 ? 1 : q.getPage();
        int size = q.getSize() == null || q.getSize() < 1 ? DEFAULT_SIZE : q.getSize();

        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        // 前台永远只能看到已发布的文章
        wrapper.eq(Article::getStatus, Article.STATUS_PUBLISHED);
        if (q.getCategoryId() != null) {
            wrapper.eq(Article::getCategoryId, q.getCategoryId());
        }
        // 按标签筛选：先查中间表拿到文章 ID 集合，再用 IN 过滤
        if (q.getTagId() != null) {
            List<Long> articleIds = articleTagMapper.selectList(
                            new LambdaQueryWrapper<ArticleTag>().eq(ArticleTag::getTagId, q.getTagId()))
                    .stream().map(ArticleTag::getArticleId).collect(Collectors.toList());
            if (articleIds.isEmpty()) {
                // 该标签下没有文章，直接返回空页，省掉一次无意义的查询
                return new PageResult<>(new ArrayList<>(), 0L, current, size);
            }
            wrapper.in(Article::getId, articleIds);
        }
        if (StringUtils.hasText(q.getKeyword())) {
            String kw = q.getKeyword().trim();
            // and(...) 保证 or 条件被括号包住：(title LIKE ? OR summary LIKE ?)
            wrapper.and(w -> w.like(Article::getTitle, kw).or().like(Article::getSummary, kw));
        }
        // 列表页不下发 Markdown 正文（LONGTEXT），显著减少网络与内存开销
        wrapper.select(Article.class, field -> !"content".equals(field.getColumn()));
        // 置顶优先，其次按创建时间倒序
        wrapper.orderByDesc(Article::getTop).orderByDesc(Article::getCreateTime);

        IPage<Article> result = articleMapper.selectPage(new Page<>(current, size), wrapper);
        return new PageResult<>(assemble(result.getRecords(), false),
                result.getTotal(), (int) result.getCurrent(), (int) result.getSize());
    }

    @Override
    public List<ArticleVO> listHot(int limit) {
        return listByOrder(limit, false);
    }

    @Override
    public List<ArticleVO> listLatest(int limit) {
        return listByOrder(limit, true);
    }

    /** 按浏览量或创建时间取前 N 篇（内部复用） */
    private List<ArticleVO> listByOrder(int limit, boolean byCreateTime) {
        int safeLimit = limit <= 0 ? 5 : Math.min(limit, 100);
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<Article>()
                .eq(Article::getStatus, Article.STATUS_PUBLISHED)
                .select(Article.class, field -> !"content".equals(field.getColumn()));
        if (byCreateTime) {
            wrapper.orderByDesc(Article::getCreateTime);
        } else {
            wrapper.orderByDesc(Article::getViews);
        }
        IPage<Article> page = articleMapper.selectPage(new Page<>(1, safeLimit), wrapper);
        return assemble(page.getRecords(), false);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ArticleVO getDetail(Long id) {
        Article article = getEntity(id);
        if (!Article.STATUS_PUBLISHED.equals(article.getStatus())) {
            throw BusinessException.notFound("文章不存在或已下架");
        }
        // 用 SQL 自增而不是"读出来 +1 再写回"：
        // 后者在并发下会丢更新（两个请求都读到 100，各自写回 101）
        articleMapper.update(null, new LambdaUpdateWrapper<Article>()
                .eq(Article::getId, id)
                .setSql("views = views + 1"));
        // 同步内存中的值，保证本次响应返回的就是最新浏览量
        article.setViews((article.getViews() == null ? 0 : article.getViews()) + 1);
        return assemble(Collections.singletonList(article), true).get(0);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ArticleVO toggleLike(Long id) {
        Long userId = requireLoginUserId();
        Article article = getEntity(id);
        if (!Article.STATUS_PUBLISHED.equals(article.getStatus())) {
            throw BusinessException.notFound("文章不存在或已下架");
        }

        // 查 t_article_like 判断当前用户是否已点赞
        ArticleLike existing = articleLikeMapper.selectOne(new LambdaQueryWrapper<ArticleLike>()
                .eq(ArticleLike::getArticleId, id)
                .eq(ArticleLike::getUserId, userId));

        if (existing != null) {
            articleLikeMapper.deleteById(existing.getId());
            // CASE WHEN 防止并发下 likes 被减成负数
            articleMapper.update(null, new LambdaUpdateWrapper<Article>()
                    .eq(Article::getId, id)
                    .setSql("likes = CASE WHEN likes > 0 THEN likes - 1 ELSE 0 END"));
            log.debug("用户 {} 取消点赞文章 {}", userId, id);
        } else {
            ArticleLike like = new ArticleLike();
            like.setArticleId(id);
            like.setUserId(userId);
            like.setCreateTime(LocalDateTime.now());
            articleLikeMapper.insert(like);
            articleMapper.update(null, new LambdaUpdateWrapper<Article>()
                    .eq(Article::getId, id)
                    .setSql("likes = likes + 1"));
            log.debug("用户 {} 点赞文章 {}", userId, id);
        }
        // 重新查询，返回数据库里的最新状态（liked 由 assemble 内部查表得出）
        return assemble(Collections.singletonList(getEntity(id)), false).get(0);
    }

    // ==================================================================
    //  后台接口
    // ==================================================================

    @Override
    public PageResult<ArticleVO> pageForAdmin(ArticleQueryDTO query) {
        ArticleQueryDTO q = query == null ? new ArticleQueryDTO() : query;
        int current = q.getPage() == null || q.getPage() < 1 ? 1 : q.getPage();
        int size = q.getSize() == null || q.getSize() < 1 ? 10 : q.getSize();

        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        // 后台不限制状态：传了 status 才按状态过滤（0 草稿 / 1 已发布）
        if (q.getStatus() != null) {
            wrapper.eq(Article::getStatus, q.getStatus());
        }
        if (q.getCategoryId() != null) {
            wrapper.eq(Article::getCategoryId, q.getCategoryId());
        }
        if (StringUtils.hasText(q.getKeyword())) {
            String kw = q.getKeyword().trim();
            wrapper.and(w -> w.like(Article::getTitle, kw).or().like(Article::getSummary, kw));
        }
        wrapper.select(Article.class, field -> !"content".equals(field.getColumn()));
        wrapper.orderByDesc(Article::getTop).orderByDesc(Article::getCreateTime);

        IPage<Article> result = articleMapper.selectPage(new Page<>(current, size), wrapper);
        return new PageResult<>(assemble(result.getRecords(), false),
                result.getTotal(), (int) result.getCurrent(), (int) result.getSize());
    }

    @Override
    public ArticleVO getDetailForAdmin(Long id) {
        // 编辑页回填用：不累加浏览量，草稿也能查
        return assemble(Collections.singletonList(getEntity(id)), true).get(0);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ArticleVO create(ArticleDTO dto) {
        validateCategory(dto.getCategoryId());

        Article article = new Article();
        article.setTitle(dto.getTitle());
        article.setSummary(resolveSummary(dto));
        article.setContent(dto.getContent());
        article.setCategoryId(dto.getCategoryId());
        article.setAuthorId(requireLoginUserId());
        article.setViews(0);
        article.setLikes(0);
        article.setCommentCount(0);
        article.setStatus(dto.getStatus() == null ? Article.STATUS_DRAFT : dto.getStatus());
        article.setTop(dto.getTop() == null ? 0 : dto.getTop());
        article.setCreateTime(LocalDateTime.now());
        article.setUpdateTime(LocalDateTime.now());
        articleMapper.insert(article);
        // 插入后才能拿到自增主键，用它生成唯一封面
        if (!StringUtils.hasText(dto.getCover())) {
            article.setCover(COVER_TPL + article.getId() + "/800/450");
            articleMapper.updateById(article);
        } else {
            article.setCover(dto.getCover());
        }

        // 维护多对多中间表
        saveArticleTags(article.getId(), sanitizeTagIds(dto.getTagIds()));

        log.info("新增文章: {} (id={}, status={})", article.getTitle(), article.getId(), article.getStatus());
        return assemble(Collections.singletonList(getEntity(article.getId())), true).get(0);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ArticleVO update(Long id, ArticleDTO dto) {
        getEntity(id);
        validateCategory(dto.getCategoryId());

        Article update = new Article();
        update.setId(id);
        update.setTitle(dto.getTitle());
        update.setSummary(resolveSummary(dto));
        update.setContent(dto.getContent());
        update.setCategoryId(dto.getCategoryId());
        update.setStatus(dto.getStatus());
        update.setTop(dto.getTop());
        update.setUpdateTime(LocalDateTime.now());
        if (StringUtils.hasText(dto.getCover())) {
            update.setCover(dto.getCover());
        }
        articleMapper.updateById(update);

        // 标签关系整体重建：先删后插，逻辑最简单也最不容易出错
        saveArticleTags(id, sanitizeTagIds(dto.getTagIds()));

        log.info("修改文章: {} (id={})", dto.getTitle(), id);
        return assemble(Collections.singletonList(getEntity(id)), true).get(0);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        Article article = getEntity(id);
        articleMapper.deleteById(id);
        // 级联清理三张关联表，避免留下孤儿数据
        int comments = commentMapper.delete(new LambdaQueryWrapper<Comment>().eq(Comment::getArticleId, id));
        articleTagMapper.delete(new LambdaQueryWrapper<ArticleTag>().eq(ArticleTag::getArticleId, id));
        articleLikeMapper.delete(new LambdaQueryWrapper<ArticleLike>().eq(ArticleLike::getArticleId, id));
        log.info("删除文章: {} (id={})，同时清理了 {} 条评论及其标签/点赞记录",
                article.getTitle(), id, comments);
    }

    // ==================================================================
    //  通用
    // ==================================================================

    @Override
    public Article getEntity(Long id) {
        Article article = id == null ? null : articleMapper.selectById(id);
        if (article == null) {
            throw BusinessException.notFound("文章不存在，id=" + id);
        }
        return article;
    }

    // ==================================================================
    //  VO 组装（批量查询，避免 N+1）
    // ==================================================================

    /**
     * 把文章实体列表组装成 VO 列表。
     *
     * <p>固定只发 4 个附加查询，与文章数量无关：</p>
     * <ol>
     *   <li>批量查分类</li>
     *   <li>批量查作者</li>
     *   <li>批量查标签关联 + 批量查标签</li>
     *   <li>批量查当前用户的点赞记录</li>
     * </ol>
     *
     * @param withContent 是否带上 Markdown 正文（仅详情/编辑页需要）
     */
    private List<ArticleVO> assemble(List<Article> articles, boolean withContent) {
        List<ArticleVO> resultList = new ArrayList<>();
        if (articles == null || articles.isEmpty()) {
            return resultList;
        }

        List<Long> articleIds = articles.stream().map(Article::getId).collect(Collectors.toList());

        // ---------- 1. 分类 ----------
        Set<Long> categoryIds = articles.stream().map(Article::getCategoryId)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, Category> categoryMap = categoryIds.isEmpty() ? Collections.emptyMap()
                : categoryMapper.selectBatchIds(categoryIds).stream()
                .collect(Collectors.toMap(Category::getId, Function.identity(), (a, b) -> a));

        // ---------- 2. 作者 ----------
        Set<Long> authorIds = articles.stream().map(Article::getAuthorId)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, User> authorMap = authorIds.isEmpty() ? Collections.emptyMap()
                : userMapper.selectBatchIds(authorIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity(), (a, b) -> a));

        // ---------- 3. 标签（中间表 + 标签表） ----------
        List<ArticleTag> relations = articleTagMapper.selectList(
                new LambdaQueryWrapper<ArticleTag>().in(ArticleTag::getArticleId, articleIds));
        Set<Long> tagIds = relations.stream().map(ArticleTag::getTagId).collect(Collectors.toSet());
        Map<Long, Tag> tagMap = tagIds.isEmpty() ? Collections.emptyMap()
                : tagMapper.selectBatchIds(tagIds).stream()
                .collect(Collectors.toMap(Tag::getId, Function.identity(), (a, b) -> a));
        Map<Long, List<Long>> tagIdsByArticle = relations.stream()
                .collect(Collectors.groupingBy(ArticleTag::getArticleId,
                        Collectors.mapping(ArticleTag::getTagId, Collectors.toList())));

        // ---------- 4. 当前登录用户在本页点过赞的文章 ----------
        Set<Long> likedArticleIds = new HashSet<>();
        Long currentUserId = LoginUserContext.getUserId();
        if (currentUserId != null) {
            articleLikeMapper.selectList(new LambdaQueryWrapper<ArticleLike>()
                            .eq(ArticleLike::getUserId, currentUserId)
                            .in(ArticleLike::getArticleId, articleIds))
                    .forEach(like -> likedArticleIds.add(like.getArticleId()));
        }

        // ---------- 5. 逐个组装 ----------
        for (Article article : articles) {
            ArticleVO vo = new ArticleVO();
            vo.setId(article.getId());
            vo.setTitle(article.getTitle());
            vo.setSummary(article.getSummary());
            vo.setCover(article.getCover());
            if (withContent) {
                vo.setContent(article.getContent());
            }

            Category category = article.getCategoryId() == null ? null : categoryMap.get(article.getCategoryId());
            vo.setCategoryId(article.getCategoryId());
            vo.setCategoryName(category == null ? "未分类" : category.getName());

            List<Long> voTagIds = tagIdsByArticle.getOrDefault(article.getId(), new ArrayList<>());
            vo.setTagIds(voTagIds);
            vo.setTags(voTagIds.stream()
                    .map(tagMap::get)
                    .filter(Objects::nonNull)
                    .map(this::toSimpleTagVO)
                    .collect(Collectors.toList()));

            User author = article.getAuthorId() == null ? null : authorMap.get(article.getAuthorId());
            vo.setAuthorId(article.getAuthorId());
            vo.setAuthorName(author == null ? "匿名" : author.getNickname());
            vo.setAuthorAvatar(author == null ? null : author.getAvatar());

            vo.setViews(nullToZero(article.getViews()));
            vo.setLikes(nullToZero(article.getLikes()));
            vo.setCommentCount(nullToZero(article.getCommentCount()));
            vo.setStatus(article.getStatus());
            vo.setTop(nullToZero(article.getTop()));
            vo.setCreateTime(DateUtil.format(article.getCreateTime()));
            vo.setUpdateTime(DateUtil.format(article.getUpdateTime()));
            // 未登录时恒为 false
            vo.setLiked(likedArticleIds.contains(article.getId()));

            resultList.add(vo);
        }
        return resultList;
    }

    /**
     * 文章里内嵌的标签：只需要 id/name/color 用于展示，
     * 不查 articleCount（那是标签列表页才需要的统计值），避免每篇文章都多打几次 COUNT。
     */
    private TagVO toSimpleTagVO(Tag tag) {
        TagVO vo = new TagVO();
        vo.setId(tag.getId());
        vo.setName(tag.getName());
        vo.setColor(tag.getColor());
        return vo;
    }

    // ==================================================================
    //  私有工具
    // ==================================================================

    /** 整体重建某篇文章的标签关联（先删后插） */
    private void saveArticleTags(Long articleId, List<Long> tagIds) {
        articleTagMapper.delete(new LambdaQueryWrapper<ArticleTag>()
                .eq(ArticleTag::getArticleId, articleId));
        if (tagIds == null || tagIds.isEmpty()) {
            return;
        }
        for (Long tagId : tagIds) {
            ArticleTag relation = new ArticleTag();
            relation.setArticleId(articleId);
            relation.setTagId(tagId);
            articleTagMapper.insert(relation);
        }
    }

    /** 未填简介时，自动截取正文前 100 个字符兜底 */
    private String resolveSummary(ArticleDTO dto) {
        if (StringUtils.hasText(dto.getSummary())) {
            return dto.getSummary();
        }
        String plain = dto.getContent()
                .replaceAll("```[\\s\\S]*?```", " ")   // 去掉代码块
                .replaceAll("[#>*`\\-\\[\\]()]", " ")    // 去掉 Markdown 标记
                .replaceAll("\\s+", " ")
                .trim();
        return plain.length() <= 100 ? plain : plain.substring(0, 100) + "...";
    }

    /** 校验分类是否存在（给出明确提示，而不是静默存一个不存在的分类 ID） */
    private void validateCategory(Long categoryId) {
        if (categoryId != null && categoryMapper.selectById(categoryId) == null) {
            throw BusinessException.paramError("分类不存在，id=" + categoryId);
        }
    }

    /** 过滤掉不存在的标签 ID 并去重 */
    private List<Long> sanitizeTagIds(List<Long> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            return new ArrayList<>();
        }
        List<Long> distinct = tagIds.stream()
                .filter(Objects::nonNull).distinct().collect(Collectors.toList());
        if (distinct.isEmpty()) {
            return distinct;
        }
        // 只保留数据库里真实存在的标签
        return tagMapper.selectBatchIds(distinct).stream()
                .map(Tag::getId).collect(Collectors.toList());
    }

    private Long requireLoginUserId() {
        Long userId = LoginUserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "请先登录");
        }
        return userId;
    }

    private static int nullToZero(Integer value) {
        return value == null ? 0 : value;
    }
}
