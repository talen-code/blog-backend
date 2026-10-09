package com.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.common.BusinessException;
import com.blog.common.LoginUser;
import com.blog.common.PageResult;
import com.blog.common.ResultCode;
import com.blog.config.LoginUserContext;
import com.blog.dto.LoginDTO;
import com.blog.dto.ProfileDTO;
import com.blog.dto.RegisterDTO;
import com.blog.entity.Comment;
import com.blog.entity.User;
import com.blog.entity.UserToken;
import com.blog.mapper.CommentMapper;
import com.blog.mapper.UserMapper;
import com.blog.mapper.UserTokenMapper;
import com.blog.service.UserService;
import com.blog.util.DateUtil;
import com.blog.util.JwtUtil;
import com.blog.util.PasswordUtil;
import com.blog.vo.LoginVO;
import com.blog.vo.UserVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户服务实现 —— 全部改为操作 MySQL。
 *
 * <p>与内存版的差别：原来的 {@code Map.values().stream().filter(...)}
 * 全部替换成了 MyBatis-Plus 的 {@code LambdaQueryWrapper}，
 * 最终由 MyBatis 生成真正的 SQL 交给 MySQL 执行。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    /** 新注册用户的默认头像 */
    private static final String AVATAR_TPL = "https://api.dicebear.com/7.x/identicon/svg?seed=";

    /** 内置超管 ID（sql/02-data.sql 里第一个用户），禁止被禁用或删除 */
    private static final Long SUPER_ADMIN_ID = 1L;

    private final UserMapper userMapper;
    private final UserTokenMapper userTokenMapper;
    private final CommentMapper commentMapper;
    private final JwtUtil jwtUtil;

    // ==================================================================
    //  认证相关
    // ==================================================================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginVO register(RegisterDTO dto) {
        // 唯一性校验。注意：这里只是"预检查"，真正的唯一性由表上的 uk_username 索引保证，
        // 并发下即使两个请求同时通过预检查，数据库也会拒绝其中一条插入
        Long exists = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getUsername, dto.getUsername()));
        if (exists != null && exists > 0) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "用户名「" + dto.getUsername() + "」已被注册");
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        // 存摘要而不是明文
        user.setPassword(PasswordUtil.encrypt(dto.getPassword()));
        user.setNickname(dto.getNickname());
        user.setAvatar(AVATAR_TPL + dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setSignature("这个人很懒，什么都没留下~");
        // 自助注册一律是普通用户
        user.setRole(User.ROLE_USER);
        user.setStatus(User.STATUS_NORMAL);
        user.setCreateTime(LocalDateTime.now());
        userMapper.insert(user);
        // insert 之后 MyBatis-Plus 会把数据库自增主键回填到 user.id
        log.info("新用户注册成功: id={}, username={}", user.getId(), user.getUsername());

        return buildLoginVO(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginVO login(LoginDTO dto) {
        User user = findByUsername(dto.getUsername());
        // 用户名不存在和密码错误返回同一句提示，避免账号枚举攻击
        if (user == null || !PasswordUtil.matches(dto.getPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "用户名或密码错误");
        }
        if (User.STATUS_DISABLED.equals(user.getStatus())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "该账号已被禁用，请联系管理员");
        }

        // 记录最近登录时间（updateById 只更新非 null 字段）
        User update = new User();
        update.setId(user.getId());
        update.setLastLoginTime(LocalDateTime.now());
        userMapper.updateById(update);
        user.setLastLoginTime(update.getLastLoginTime());

        log.info("用户登录成功: id={}, username={}, role={}", user.getId(), user.getUsername(), user.getRole());
        return buildLoginVO(user);
    }

    @Override
    public void logout(String token) {
        if (StringUtils.hasText(token)) {
            // 从数据库白名单删除后，该 token 立即失效（即使 JWT 自身还没过期）
            int rows = userTokenMapper.delete(
                    new LambdaQueryWrapper<UserToken>().eq(UserToken::getToken, token));
            log.info("用户退出登录，清理 token 记录 {} 条", rows);
        }
    }

    // ==================================================================
    //  当前用户
    // ==================================================================

    @Override
    public UserVO getCurrentUser() {
        LoginUser loginUser = LoginUserContext.get();
        if (loginUser == null) {
            throw BusinessException.unauthorized("请先登录");
        }
        // 每次都回源查询，保证资料修改后立即生效
        return toVO(getEntity(loginUser.getUserId()));
    }

    @Override
    public UserVO updateProfile(ProfileDTO dto) {
        User user = getEntity(requireLoginUserId());

        // 只更新「传了值」的字段，避免把没填的字段清空
        User update = new User();
        update.setId(user.getId());
        if (StringUtils.hasText(dto.getNickname())) {
            update.setNickname(dto.getNickname());
        }
        if (StringUtils.hasText(dto.getAvatar())) {
            update.setAvatar(dto.getAvatar());
        }
        if (dto.getEmail() != null) {
            update.setEmail(dto.getEmail());
        }
        if (dto.getSignature() != null) {
            update.setSignature(dto.getSignature());
        }
        if (StringUtils.hasText(dto.getPassword())) {
            update.setPassword(PasswordUtil.encrypt(dto.getPassword()));
            log.info("用户 {} 修改了密码", user.getUsername());
        }
        userMapper.updateById(update);

        // 重新查一次，保证返回给前端的是数据库里的最终值
        return toVO(getEntity(user.getId()));
    }

    // ==================================================================
    //  后台用户管理
    // ==================================================================

    @Override
    public PageResult<UserVO> page(int page, int size, String keyword) {
        Page<User> mpPage = new Page<>(page < 1 ? 1 : page, size < 1 ? 10 : size);

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            // and(...) 的作用是给 or 条件加括号：
            //   WHERE (username LIKE ? OR nickname LIKE ?)
            // 不加括号会变成 ... AND status = 1 OR nickname LIKE ?，逻辑就错了
            wrapper.and(w -> w.like(User::getUsername, keyword)
                    .or().like(User::getNickname, keyword));
        }
        wrapper.orderByAsc(User::getId);

        IPage<User> result = userMapper.selectPage(mpPage, wrapper);
        List<UserVO> records = result.getRecords().stream()
                .map(this::toVO)
                .collect(Collectors.toList());
        return new PageResult<>(records, result.getTotal(), (int) result.getCurrent(), (int) result.getSize());
    }

    @Override
    public UserVO updateStatus(Long id, Integer status) {
        User user = getEntity(id);
        if (SUPER_ADMIN_ID.equals(id) && User.STATUS_DISABLED.equals(status)) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "内置超级管理员不允许被禁用");
        }
        if (id.equals(LoginUserContext.getUserId())) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "不能修改自己的账号状态");
        }
        User update = new User();
        update.setId(id);
        update.setStatus(status);
        userMapper.updateById(update);

        // 被禁用时顺手清掉该用户所有 token，强制下线
        if (User.STATUS_DISABLED.equals(status)) {
            userTokenMapper.delete(new LambdaQueryWrapper<UserToken>().eq(UserToken::getUserId, id));
        }
        log.info("用户 {} 状态被修改为 {}", user.getUsername(), status);
        return toVO(getEntity(id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        User user = getEntity(id);
        if (SUPER_ADMIN_ID.equals(id)) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "内置超级管理员不允许被删除");
        }
        if (id.equals(LoginUserContext.getUserId())) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "不能删除自己的账号");
        }
        userMapper.deleteById(id);
        // 级联清理：评论、登录令牌（等价于外键 ON DELETE CASCADE）
        commentMapper.delete(new LambdaQueryWrapper<Comment>().eq(Comment::getUserId, id));
        userTokenMapper.delete(new LambdaQueryWrapper<UserToken>().eq(UserToken::getUserId, id));
        log.info("用户 {} 已被删除，并清理其评论与登录令牌", user.getUsername());
    }

    // ==================================================================
    //  通用
    // ==================================================================

    @Override
    public User getEntity(Long id) {
        User user = id == null ? null : userMapper.selectById(id);
        if (user == null) {
            throw BusinessException.notFound("用户不存在，id=" + id);
        }
        return user;
    }

    @Override
    public UserVO toVO(User user) {
        if (user == null) {
            return null;
        }
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setAvatar(user.getAvatar());
        vo.setEmail(user.getEmail());
        vo.setSignature(user.getSignature());
        vo.setRole(user.getRole());
        vo.setStatus(user.getStatus());
        vo.setCreateTime(DateUtil.format(user.getCreateTime()));
        vo.setLastLoginTime(DateUtil.format(user.getLastLoginTime()));
        return vo;
    }

    // ==================================================================
    //  私有工具
    // ==================================================================

    /**
     * 签发 token + 写白名单 + 组装 LoginVO。
     * <p>白名单落到 {@code t_user_token} 表，因此服务重启后登录态不丢失。</p>
     */
    private LoginVO buildLoginVO(User user) {
        // 1. 先清理该用户的历史过期 token，避免表无限增长
        userTokenMapper.delete(new LambdaQueryWrapper<UserToken>()
                .eq(UserToken::getUserId, user.getId())
                .lt(UserToken::getExpireAt, System.currentTimeMillis()));

        // 2. 签发 JWT
        String token = jwtUtil.createToken(new LoginUser(
                user.getId(), user.getUsername(), user.getNickname(),
                user.getAvatar(), user.getRole(), null, null));

        // 3. 写入数据库白名单
        UserToken userToken = new UserToken();
        userToken.setToken(token);
        userToken.setUserId(user.getId());
        userToken.setExpireAt(System.currentTimeMillis() + jwtUtil.getExpireMillis());
        userToken.setCreateTime(LocalDateTime.now());
        userTokenMapper.insert(userToken);

        LoginVO vo = new LoginVO();
        vo.setToken(token);
        vo.setTokenType("Bearer");
        vo.setExpiresIn(jwtUtil.getExpireMillis());
        vo.setUser(toVO(user));
        return vo;
    }

    private User findByUsername(String username) {
        // username 上有唯一索引，selectOne 不会命中多条
        return userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, username));
    }

    private Long requireLoginUserId() {
        Long userId = LoginUserContext.getUserId();
        if (userId == null) {
            throw BusinessException.unauthorized("请先登录");
        }
        return userId;
    }
}
