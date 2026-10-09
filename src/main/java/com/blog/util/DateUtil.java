package com.blog.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 时间格式化工具。
 *
 * <p>实体里用 {@code LocalDateTime} 存储，VO 里统一输出
 * {@code yyyy-MM-dd HH:mm:ss} 字符串，避免前端还要处理时区与格式。</p>
 *
 * <p>{@link DateTimeFormatter} 是线程安全的（与 SimpleDateFormat 不同），
 * 因此可以安全地作为静态常量复用。</p>
 */
public final class DateUtil {

    private static final DateTimeFormatter NORMAL = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DATE_ONLY = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private DateUtil() {
    }

    /** 输出 yyyy-MM-dd HH:mm:ss；入参为 null 时返回 null */
    public static String format(LocalDateTime time) {
        return time == null ? null : time.format(NORMAL);
    }

    /** 输出 yyyy-MM-dd */
    public static String formatDate(LocalDateTime time) {
        return time == null ? null : time.format(DATE_ONLY);
    }
}
