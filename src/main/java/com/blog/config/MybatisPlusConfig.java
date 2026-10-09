package com.blog.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus 配置。
 *
 * <p><b>这个类不能省</b>：MyBatis-Plus 的分页是"物理分页"，
 * 靠 {@link PaginationInnerInterceptor} 拦截 SQL 后自动拼接 {@code LIMIT ?, ?}。
 * 不注册这个插件，{@code selectPage} 会退化成查全部再内存分页（甚至 total 为 0）。</p>
 */
@Configuration
public class MybatisPlusConfig {

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        // 分页插件：必须指定数据库类型，否则无法生成正确的分页方言
        PaginationInnerInterceptor pagination = new PaginationInnerInterceptor(DbType.MYSQL);
        // 单页条数上限，防止前端传 size=999999 把数据库打挂
        pagination.setMaxLimit(100L);
        // 溢出总页数后是否进行处理（true：请求第 999 页时回到第一页，而不是返回空）
        pagination.setOverflow(false);

        interceptor.addInnerInterceptor(pagination);
        return interceptor;
    }
}
