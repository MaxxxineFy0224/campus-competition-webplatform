package com.jingdui.config;

import okhttp3.OkHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * HTTP 客户端配置 —— OkHttp
 *
 * 用于调用硅基流动 API（OpenAI 兼容协议）。
 * ObjectMapper 由 Spring Boot 自动配置（包含 JavaTimeModule），此处不再覆盖。
 */
@Configuration
public class ChatClientConfig {

    @Bean
    public OkHttpClient okHttpClient() {
        return new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(120, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
    }
}
