package com.blog.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置：跨域（CORS）+ 拦截器注册。
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final JwtInterceptor jwtInterceptor;

    /**
     * 跨域配置。
     *
     * <p>说明：</p>
     * <ul>
     *   <li>开发期前端通过 Vite 代理访问后端，其实同源、用不到 CORS；
     *       但为了「前端直连 8080」也能跑通，这里仍然开放跨域。</li>
     *   <li>{@code allowedOriginPatterns("*")} 与 {@code allowCredentials(true)} 搭配使用；
     *       不能用 {@code allowedOrigins("*")}，Spring 会直接抛异常。</li>
     *   <li>预检请求缓存 1 小时，减少 OPTIONS 请求数量。</li>
     * </ul>
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                .allowedHeaders("*")
                // 让前端能读到自定义响应头
                .exposedHeaders("Authorization", "Content-Disposition")
                .allowCredentials(true)
                .maxAge(3600);
    }

    /**
     * 注册 JWT 拦截器。
     *
     * <p>只拦截 {@code /api/**}，并且放行登录、注册这两个公开接口
     * （其实不放行也没关系，因为拦截器只对带 {@code @RequireLogin/@RequireAdmin}
     * 的接口做鉴权，其余接口即使未登录也照常放行）。</p>
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/auth/login", "/api/auth/register", "/error");
    }
}
