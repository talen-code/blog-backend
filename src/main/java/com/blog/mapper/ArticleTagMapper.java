package com.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.entity.ArticleTag;
import com.blog.vo.CountItemVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 文章-标签关联 Mapper（多对多中间表）。
 */
@Mapper
public interface ArticleTagMapper extends BaseMapper<ArticleTag> {

    /** 一次查出所有标签各自的文章数，避免逐个 COUNT 的 N+1 */
    @Select("SELECT tag_id AS id, COUNT(*) AS cnt FROM t_article_tag GROUP BY tag_id")
    List<CountItemVO> countGroupByTag();
}
