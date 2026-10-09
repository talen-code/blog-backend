package com.blog.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 评论出参对象（两级树结构）。
 *
 * <p>顶级评论的 {@code children} 里是它的回复；回复的 {@code children} 恒为空列表。</p>
 */
@Data
public class CommentVO implements Serializable {

    private Long id;
    private Long articleId;

    /** 文章标题：后台评论管理列表需要展示，前台评论列表可为 null */
    private String articleTitle;

    private Long userId;

    /** 评论人昵称 / 头像（内存 join 自 users 表） */
    private String nickname;
    private String avatar;

    private String content;

    /** 父评论 ID，null 表示顶级评论 */
    private Long parentId;

    /** 被回复人昵称，用于展示「回复 @某某」 */
    private String replyToNickname;

    /** 1 显示 / 0 屏蔽 */
    private Integer status;

    private String createTime;

    /** 子回复列表 */
    private List<CommentVO> children = new ArrayList<>();
}
