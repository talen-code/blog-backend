package com.blog.util;

import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;

/**
 * 密码工具类。
 *
 * <p><b>注意</b>：这里用 MD5 只是为了「不存明文」这个演示目的。
 * 真实项目请使用 BCrypt / Argon2 等带盐慢哈希
 * （引入 spring-security-crypto 后一行 {@code new BCryptPasswordEncoder()} 即可）。</p>
 */
public final class PasswordUtil {

    /** 工具类禁止实例化 */
    private PasswordUtil() {
    }

    /**
     * 对明文密码做摘要。
     *
     * @param rawPassword 明文密码
     * @return 32 位小写十六进制摘要
     */
    public static String encrypt(String rawPassword) {
        if (rawPassword == null) {
            return null;
        }
        return DigestUtils.md5DigestAsHex(rawPassword.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 校验明文密码与摘要是否匹配。
     *
     * @param rawPassword     前端传入的明文
     * @param hashedPassword  内存中保存的摘要
     */
    public static boolean matches(String rawPassword, String hashedPassword) {
        if (rawPassword == null || hashedPassword == null) {
            return false;
        }
        return hashedPassword.equals(encrypt(rawPassword));
    }
}
