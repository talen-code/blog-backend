package com.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.entity.UserToken;
import org.apache.ibatis.annotations.Mapper;

/**
 * 登录令牌 Mapper（JWT 白名单落库，支持登出即失效 + 重启不掉登录态）。
 */
@Mapper
public interface UserTokenMapper extends BaseMapper<UserToken> {
}
