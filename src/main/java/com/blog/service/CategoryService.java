package com.blog.service;

import com.blog.dto.CategoryDTO;
import com.blog.entity.Category;
import com.blog.vo.CategoryVO;

import java.util.List;

/**
 * 分类服务。
 */
public interface CategoryService {

    /** 分类列表（按 sort 升序），带文章数统计 */
    List<CategoryVO> list();

    /** 新增分类 */
    CategoryVO create(CategoryDTO dto);

    /** 修改分类 */
    CategoryVO update(Long id, CategoryDTO dto);

    /** 删除分类：分类下还有文章时拒绝删除 */
    void delete(Long id);

    /** 实体 -> VO */
    CategoryVO toVO(Category category);

    /** 按 ID 取实体，不存在时抛业务异常 */
    Category getEntity(Long id);
}
