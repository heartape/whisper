package com.heartape.whisper.config;

import com.heartape.whisper.common.JwtUtils;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@EnableConfigurationProperties(JwtProperties.class)
public class JwtInitializer implements ApplicationRunner {

    private final JwtProperties properties;

    public JwtInitializer(JwtProperties properties) {
        this.properties = properties;
    }

    @Override
    public void run(ApplicationArguments args) {
        JwtUtils.init(
                properties.getSecret(),
                properties.getExpire(),
                properties.getIssuer(),
                properties.getAudience()
        );
    }
}

