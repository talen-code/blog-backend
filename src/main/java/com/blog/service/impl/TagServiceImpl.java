package com.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blog.common.BusinessException;
import com.blog.common.ResultCode;
import com.blog.dto.TagDTO;
import com.blog.entity.ArticleTag;
import com.blog.entity.Tag;
import com.blog.mapper.ArticleTagMapper;
import com.blog.mapper.TagMapper;
import com.blog.service.TagService;
import com.blog.util.DateUtil;
import com.blog.vo.CountItemVO;
import com.blog.vo.TagVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 标签服务实现 —— 操作 MySQL。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TagServiceImpl implements TagService {

    private final TagMapper tagMapper;
    private final ArticleTagMapper articleTagMapper;

    @Override
    public List<TagVO> list() {
        List<Tag> tags = tagMapper.selectList(null);

        // 一次 GROUP BY 查全部标签的文章数
        Map<Long, Long> countMap = articleTagMapper.countGroupByTag().stream()
                .collect(Collectors.toMap(CountItemVO::getId, CountItemVO::getCnt, (a, b) -> a));

        return tags.stream()
                .map(t -> toVO(t, countMap.getOrDefault(t.getId(), 0L)))
                // 热门标签靠前（文章数倒序），数量相同按 ID 升序，保证顺序稳定
                .sorted(Comparator.comparing(TagVO::getArticleCount,
                                Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(TagVO::getId))
                .collect(Collectors.toList());
    }

    @Override
    public TagVO create(TagDTO dto) {
        checkNameUnique(dto.getName(), null);
        Tag tag = new Tag();
        tag.setName(dto.getName());
        tag.setColor(dto.getColor());
        tag.setCreateTime(LocalDateTime.now());
        tagMapper.insert(tag);
        log.info("新增标签: {} (id={})", tag.getName(), tag.getId());
        return toVO(tag, 0L);
    }

    @Override
    public TagVO update(Long id, TagDTO dto) {
        getEntity(id);
        checkNameUnique(dto.getName(), id);

        Tag update = new Tag();
        update.setId(id);
        update.setName(dto.getName());
        update.setColor(dto.getColor());
        tagMapper.updateById(update);

        log.info("修改标签: {} (id={})", dto.getName(), id);
        return toVO(getEntity(id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        Tag tag = getEntity(id);
        // 标签与文章是多对多：删除标签必须同时清理中间表，
        // 否则 t_article_tag 里会留下指向不存在标签的脏数据，
        // 文章详情页就会渲染出一个「空白标签」
        int rows = articleTagMapper.delete(
                new LambdaQueryWrapper<ArticleTag>().eq(ArticleTag::getTagId, id));
        tagMapper.deleteById(id);
        log.info("删除标签: {} (id={})，同时清理了 {} 条文章关联", tag.getName(), id, rows);
    }

    @Override
    public Tag getEntity(Long id) {
        Tag tag = id == null ? null : tagMapper.selectById(id);
        if (tag == null) {
            throw BusinessException.notFound("标签不存在，id=" + id);
        }
        return tag;
    }

    @Override
    public TagVO toVO(Tag tag) {
        if (tag == null) {
            return null;
        }
        Long count = articleTagMapper.selectCount(
                new LambdaQueryWrapper<ArticleTag>().eq(ArticleTag::getTagId, tag.getId()));
        return toVO(tag, count == null ? 0L : count);
    }

    /** 组装 VO，文章数由调用方传入 */
    private TagVO toVO(Tag tag, Long articleCount) {
        TagVO vo = new TagVO();
        vo.setId(tag.getId());
        vo.setName(tag.getName());
        vo.setColor(tag.getColor());
        vo.setArticleCount(articleCount == null ? 0 : articleCount.intValue());
        vo.setCreateTime(DateUtil.format(tag.getCreateTime()));
        return vo;
    }

    private void checkNameUnique(String name, Long excludeId) {
        LambdaQueryWrapper<Tag> wrapper = new LambdaQueryWrapper<Tag>().eq(Tag::getName, name);
        if (excludeId != null) {
            wrapper.ne(Tag::getId, excludeId);
        }
        Long count = tagMapper.selectCount(wrapper);
        if (count != null && count > 0) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "标签「" + name + "」已存在");
        }
    }
}
