package com.blog.service;

import com.blog.common.PageResult;
import com.blog.dto.LoginDTO;
import com.blog.dto.ProfileDTO;
import com.blog.dto.RegisterDTO;
import com.blog.entity.User;
import com.blog.vo.LoginVO;
import com.blog.vo.UserVO;

/**
 * 用户服务。
 *
 * <p>声明成接口是 Spring 项目的常见分层习惯：Controller 只依赖抽象，
 * 后续想换实现（比如接数据库）时不必改调用方。</p>
 */
public interface UserService {

    /** 注册并自动登录，返回 token */
    LoginVO register(RegisterDTO dto);

    /** 登录，返回 token 与用户信息 */
    LoginVO login(LoginDTO dto);

    /** 退出登录：把当前 token 从内存白名单移除 */
    void logout(String token);

    /** 查询当前登录用户 */
    UserVO getCurrentUser();

    /** 修改当前登录用户的资料 */
    UserVO updateProfile(ProfileDTO dto);

    /** 后台：分页查询用户 */
    PageResult<UserVO> page(int page, int size, String keyword);

    /** 后台：启用 / 禁用用户 */
    UserVO updateStatus(Long id, Integer status);

    /** 后台：删除用户 */
    void delete(Long id);

    /** 实体 -> VO */
    UserVO toVO(User user);

    /** 按 ID 取实体，不存在时抛业务异常（供其它服务复用） */
    User getEntity(Long id);
}
