package com.heartape.whisper.config;

import com.heartape.whisper.interceptor.TokenInterceptor;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.CommonsRequestLoggingFilter;
import org.springframework.web.servlet.config.annotation.*;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Resource
    private TokenInterceptor tokenInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(tokenInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/login",
                        "/register",
                        "/upload",
                        "/static/**",
                        "/",
                        "/**.ico",
                        "/**.html",
                        "/**.js",
                        "/**.css"
                );
    }

    @Bean
    public CommonsRequestLoggingFilter logFilter() {
        CommonsRequestLoggingFilter filter = new CommonsRequestLoggingFilter();
        filter.setIncludeQueryString(true); // 包含查询参数
        filter.setIncludePayload(true);      // 包含请求体
        filter.setMaxPayloadLength(10000);   // 设置请求体最大长度
        filter.setIncludeHeaders(false);     // 是否包含请求头
        filter.setAfterMessagePrefix("REQUEST DATA: ");
        return filter;
    }
}
