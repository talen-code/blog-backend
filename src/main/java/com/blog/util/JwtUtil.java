package com.blog.util;

import com.blog.common.LoginUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 工具类（基于 jjwt 0.12.x）。
 *
 * <p>核心职责只有两个：<b>签发 token</b> 和 <b>解析 token</b>。
 * 「token 是否仍然有效」这件事由数据库白名单表 {@code t_user_token} 负责，
 * 见 {@code JwtInterceptor#resolveLoginUser}。</p>
 *
 * <p>JWT 结构：{@code header.payload.signature}，本工程使用 HS256 对称加密算法。
 * 注意 payload 只是 Base64 编码而非加密，<b>不要放敏感信息</b>。</p>
 */
@Component
public class JwtUtil {

    /** 签名密钥，HS256 要求长度 >= 256 bit（32 字节），否则 jjwt 会抛 WeakKeyException */
    private final SecretKey key;

    /** token 有效期（毫秒） */
    @Getter
    private final long expireMillis;

    public JwtUtil(@Value("${blog.jwt.secret}") String secret,
                   @Value("${blog.jwt.expire-hours:24}") long expireHours) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expireMillis = expireHours * 60 * 60 * 1000L;
    }

    /**
     * 签发 token。
     *
     * @param loginUser 登录用户信息，其中的 userId / nickname / role 会写入 payload
     * @return JWT 字符串
     */
    public String createToken(LoginUser loginUser) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + expireMillis);
        return Jwts.builder()
                // sub 放用户名
                .subject(loginUser.getUsername())
                // 自定义声明：用户 ID、昵称、角色
                .claim("userId", loginUser.getUserId())
                .claim("nickname", loginUser.getNickname())
                .claim("role", loginUser.getRole())
                .issuedAt(now)
                .expiration(expiration)
                // HS256 签名
                .signWith(key)
                .compact();
    }

    /**
     * 解析并校验 token（校验签名与过期时间）。
     *
     * @param token JWT 字符串
     * @return payload
     * @throws io.jsonwebtoken.JwtException 签名不合法或已过期时抛出
     */
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 把 payload 还原成 {@link LoginUser}。
     *
     * <p>注意：JSON 反序列化时小整数会变成 Integer，
     * 所以这里用 {@code Number#longValue()} 而不是强转 Long，避免 ClassCastException。</p>
     */
    public LoginUser toLoginUser(String token, Claims claims) {
        LoginUser loginUser = new LoginUser();
        Object userId = claims.get("userId");
        loginUser.setUserId(userId == null ? null : ((Number) userId).longValue());
        loginUser.setUsername(claims.getSubject());
        loginUser.setNickname((String) claims.get("nickname"));
        loginUser.setRole((String) claims.get("role"));
        loginUser.setToken(token);
        loginUser.setExpireAt(claims.getExpiration() == null ? null : claims.getExpiration().getTime());
        return loginUser;
    }
}
