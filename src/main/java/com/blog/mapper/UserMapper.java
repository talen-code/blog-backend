package com.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户 Mapper。
 *
 * <p>继承 {@code BaseMapper<User>} 后，单表增删改查全部由 MyBatis-Plus 提供，
 * 不需要写任何 SQL：</p>
 * <pre>
 * insert(user)                  INSERT INTO t_user ...
 * deleteById(id)                DELETE FROM t_user WHERE id = ?
 * updateById(user)              UPDATE t_user SET ... WHERE id = ?
 * selectById(id)                SELECT ... FROM t_user WHERE id = ?
 * selectList(wrapper)           SELECT ... FROM t_user WHERE ...
 * selectPage(page, wrapper)     SELECT ... LIMIT ?, ?（需要分页插件）
 * selectCount(wrapper)          SELECT COUNT(*) FROM t_user WHERE ...
 * </pre>
 *
 * <p>{@code @Mapper} 与启动类上的 {@code @MapperScan} 二者留一个即可，
 * 这里保留注解是为了单测或单独引用该接口时也能被扫描到。</p>
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}
