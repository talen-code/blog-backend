package com.blog.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 通用「ID - 数量」统计结果。
 *
 * <p>用于接收自定义 SQL 的 GROUP BY 结果，例如：</p>
 * <pre>
 * SELECT category_id AS id, COUNT(*) AS cnt FROM t_article GROUP BY category_id
 * </pre>
 */
@Data
public class CountItemVO implements Serializable {

    /** 分组键（分类 ID / 标签 ID） */
    private Long id;

    /** 数量 */
    private Long cnt;
}
