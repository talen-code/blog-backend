package com.blog;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 个人博客系统 - 后端启动类。
 *
 * <p>运行环境：JDK 11 + Spring Boot 2.7.18（javax 体系）+ MyBatis-Plus 3.5.5 + MySQL 8。</p>
 *
 * <p>{@code @MapperScan} 指定 Mapper 接口所在包，这样就不必给每个 Mapper 都加 {@code @Mapper}。
 * 该注解来自 mybatis-spring（MyBatis-Plus 复用了它），包名是
 * {@code org.mybatis.spring.annotation.MapperScan}，
 * 不要和 {@code org.apache.ibatis.annotations.Mapper} 搞混。</p>
 *
 * <p>启动前请确认已经执行过 {@code sql/01-schema.sql} 与 {@code sql/02-data.sql}，
 * 创建了 {@code blog_db} 数据库及其表结构和初始数据。</p>
 *
 * @author blog-team
 */
@SpringBootApplication
@MapperScan("com.blog.mapper")
public class BlogApplication {

    public static void main(String[] args) {
        SpringApplication.run(BlogApplication.class, args);

        // 注意：Java 11 不支持文本块（三引号语法从 Java 15 才正式转正），这里用普通字符串拼接
        System.out.println("\n"
                + "==========================================================\n"
                + "  博客后端启动成功！（JDK 11 + Spring Boot 2.7.18）\n"
                + "  接口地址  : http://localhost:8082/api\n"
                + "  数据库    : mysql://127.0.0.1:3306/blog_db\n"
                + "  管理员账号: admin    / 123456\n"
                + "  普通用户  : zhangsan / 123456\n"
                + "  数据已持久化到 MySQL，重启不再丢失\n"
                + "==========================================================\n");
    }
}
