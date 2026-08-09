package com.jingdui.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * HTTP 客户端配置 —— OkHttp + Jackson
 *
 * 用于调用硅基流动 API（OpenAI 兼容协议），不引入 Spring AI 重型 SDK。
 */
@Configuration
public class ChatClientConfig {

    @Bean
    public OkHttpClient okHttpClient() {
        return new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(120, TimeUnit.SECONDS)   // 流式响应超时较长
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
