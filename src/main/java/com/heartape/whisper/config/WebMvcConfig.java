package com.heartape.whisper.config;

import com.heartape.whisper.interceptor.TokenInterceptor;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.CommonsRequestLoggingFilter;
import org.springframework.web.servlet.config.annotation.*;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;

@AllArgsConstructor
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final TokenInterceptor tokenInterceptor;

    private final UploadProperties uploadProperties;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        final String avatarLocation = toLocation(uploadProperties.getAvatar().getDir());
        registry.addResourceHandler(uploadProperties.getAvatar().getPrefix() + "/**")
                .addResourceLocations(avatarLocation);

        final String iconLocation = toLocation(uploadProperties.getIcon().getDir());
        registry.addResourceHandler(uploadProperties.getIcon().getPrefix() + "/**")
                .addResourceLocations(iconLocation);
    }

    private String toLocation(String dir) {
        String pathStr = Paths.get(dir)
                .toAbsolutePath()
                .normalize()
                .toString()
                .replace("\\", "/");

        if (!pathStr.endsWith("/")) {
            pathStr += "/";
        }
        return "file:" + pathStr;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(tokenInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/login",
                        "/register",
                        "/upload",
                        "/avatar/**",
                        "/icon/**",
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
