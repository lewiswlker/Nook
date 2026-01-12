package com.hku.nook.service.config;

import ai.z.openapi.ZhipuAiClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class ZhipuClientConfig {

    @Value("${ai.zhipu.api-key:}")
    private String apiKey;

    @Bean
    public ZhipuAiClient zhipuAiClient() {
        return ZhipuAiClient.builder()
                .ofZHIPU()
                .apiKey(apiKey)
                // ⭐ 连接池优化：保持50个空闲连接，连接存活5分钟
                .connectionPool(50, 5, TimeUnit.MINUTES)
                // ⭐ 网络超时配置：requestTimeout=0表示无总超时限制（由read/write控制）
                // connectTimeout=10s, readTimeout=120s（AI响应慢）, writeTimeout=30s
                .networkConfig(0, 10, 120, 30, TimeUnit.SECONDS)
                // ⭐ 启用Token缓存，减少每次请求的认证开销
                .enableTokenCache()
                .build();
    }
}
