package com.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blog.common.BusinessException;
import com.blog.common.ResultCode;
import com.blog.dto.CategoryDTO;
import com.blog.entity.Article;
import com.blog.entity.Category;
import com.blog.mapper.ArticleMapper;
import com.blog.mapper.CategoryMapper;
import com.blog.service.CategoryService;
import com.blog.util.DateUtil;
import com.blog.vo.CategoryVO;
import com.blog.vo.CountItemVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 分类服务实现 —— 操作 MySQL。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryMapper categoryMapper;
    private final ArticleMapper articleMapper;

    @Override
    public List<CategoryVO> list() {
        List<Category> categories = categoryMapper.selectList(
                new LambdaQueryWrapper<Category>()
                        .orderByAsc(Category::getSort)
                        .orderByAsc(Category::getId));

        // 一次 GROUP BY 查出所有分类的文章数，避免逐个分类 COUNT 的 N+1 问题
        Map<Long, Long> countMap = articleMapper.countGroupByCategory().stream()
                .collect(Collectors.toMap(CountItemVO::getId, CountItemVO::getCnt, (a, b) -> a));

        return categories.stream()
                .map(c -> toVO(c, countMap.getOrDefault(c.getId(), 0L)))
                .collect(Collectors.toList());
    }

    @Override
    public CategoryVO create(CategoryDTO dto) {
        checkNameUnique(dto.getName(), null);
        Category category = new Category();
        category.setName(dto.getName());
        category.setDescription(dto.getDescription());
        category.setSort(dto.getSort() == null ? 0 : dto.getSort());
        category.setCreateTime(LocalDateTime.now());
        categoryMapper.insert(category);
        log.info("新增分类: {} (id={})", category.getName(), category.getId());
        return toVO(category, 0L);
    }

    @Override
    public CategoryVO update(Long id, CategoryDTO dto) {
        Category category = getEntity(id);
        checkNameUnique(dto.getName(), id);

        Category update = new Category();
        update.setId(id);
        update.setName(dto.getName());
        update.setDescription(dto.getDescription());
        update.setSort(dto.getSort());
        categoryMapper.updateById(update);

        log.info("修改分类: {} (id={})", dto.getName(), id);
        return toVO(getEntity(id));
    }

    @Override
    public void delete(Long id) {
        Category category = getEntity(id);
        // 业务规则：分类下还有文章时不允许删除，
        // 否则那些文章的 category_id 会变成悬空外键。
        // 建表时故意没有加物理外键约束（互联网项目常见做法：约束放在应用层，
        // 避免分库分表 / 数据迁移时被外键卡住），所以这里必须显式判断。
        Long count = articleMapper.selectCount(
                new LambdaQueryWrapper<Article>().eq(Article::getCategoryId, id));
        if (count != null && count > 0) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR,
                    "该分类下还有 " + count + " 篇文章，请先移动或删除这些文章");
        }
        categoryMapper.deleteById(id);
        log.info("删除分类: {} (id={})", category.getName(), id);
    }

    @Override
    public Category getEntity(Long id) {
        Category category = id == null ? null : categoryMapper.selectById(id);
        if (category == null) {
            throw BusinessException.notFound("分类不存在，id=" + id);
        }
        return category;
    }

    @Override
    public CategoryVO toVO(Category category) {
        if (category == null) {
            return null;
        }
        Long count = articleMapper.selectCount(
                new LambdaQueryWrapper<Article>().eq(Article::getCategoryId, category.getId()));
        return toVO(category, count == null ? 0L : count);
    }

    /** 组装 VO，文章数由调用方传入，避免重复查询 */
    private CategoryVO toVO(Category category, Long articleCount) {
        CategoryVO vo = new CategoryVO();
        vo.setId(category.getId());
        vo.setName(category.getName());
        vo.setDescription(category.getDescription());
        vo.setSort(category.getSort());
        vo.setArticleCount(articleCount == null ? 0 : articleCount.intValue());
        vo.setCreateTime(DateUtil.format(category.getCreateTime()));
        return vo;
    }

    /** 分类名唯一性校验，excludeId 用于「修改时排除自己」 */
    private void checkNameUnique(String name, Long excludeId) {
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<Category>()
                .eq(Category::getName, name);
        if (excludeId != null) {
            wrapper.ne(Category::getId, excludeId);
        }
        Long count = categoryMapper.selectCount(wrapper);
        if (count != null && count > 0) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "分类「" + name + "」已存在");
        }
    }

    /** 预留：空 Map 常量（避免每次新建） */
    @SuppressWarnings("unused")
    private static final Map<Long, Long> EMPTY_MAP = Collections.emptyMap();
}
