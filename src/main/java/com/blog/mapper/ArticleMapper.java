package com.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.entity.Article;
import com.blog.vo.CountItemVO;
import com.blog.vo.NameValueVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 文章 Mapper。
 *
 * <p>BaseMapper 已经覆盖了全部单表 CRUD，这里额外用 {@code @Select} 写了三个
 * <b>聚合查询</b> —— MyBatis-Plus 解决的是单表 CRUD，多表/聚合场景写注解 SQL
 * 反而比硬凑 Wrapper 更清晰（这是实际项目里最常见的混合用法）。</p>
 */
@Mapper
public interface ArticleMapper extends BaseMapper<Article> {

    /** 所有文章浏览量之和（统计面板用） */
    @Select("SELECT IFNULL(SUM(views), 0) FROM t_article")
    Long sumViews();

    /** 所有文章点赞数之和 */
    @Select("SELECT IFNULL(SUM(likes), 0) FROM t_article")
    Long sumLikes();

    /**
     * 按分类统计文章数，一次查询拿到全部分类的数量，
     * 避免"遍历分类逐个 COUNT"的 N+1 问题。
     */
    @Select("SELECT category_id AS id, COUNT(*) AS cnt FROM t_article "
            + "WHERE category_id IS NOT NULL GROUP BY category_id")
    List<CountItemVO> countGroupByCategory();

    /**
     * 分类文章占比（统计面板的饼图数据）。
     * <p>LEFT JOIN 保证"文章数为 0 的分类"也会出现在结果里。</p>
     */
    @Select("SELECT c.name AS name, COUNT(a.id) AS value "
            + "FROM t_category c LEFT JOIN t_article a ON a.category_id = c.id "
            + "GROUP BY c.id, c.name, c.sort ORDER BY c.sort")
    List<NameValueVO> countGroupByCategoryName();
}
