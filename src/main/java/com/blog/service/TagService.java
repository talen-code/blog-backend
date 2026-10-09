package com.blog.service;

import com.blog.dto.TagDTO;
import com.blog.entity.Tag;
import com.blog.vo.TagVO;

import java.util.List;

/**
 * 标签服务。
 */
public interface TagService {

    /** 标签列表（按文章数倒序），带文章数统计 */
    List<TagVO> list();

    /** 新增标签 */
    TagVO create(TagDTO dto);

    /** 修改标签 */
    TagVO update(Long id, TagDTO dto);

    /** 删除标签：会把该标签从所有文章上摘掉 */
    void delete(Long id);

    /** 实体 -> VO */
    TagVO toVO(Tag tag);

    /** 按 ID 取实体，不存在时抛业务异常 */
    Tag getEntity(Long id);
}
