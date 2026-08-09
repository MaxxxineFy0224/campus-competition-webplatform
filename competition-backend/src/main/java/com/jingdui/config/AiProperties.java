package com.jingdui.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * AI 配置属性 — 映射 application.yml 中 app.ai.*
 *
 * 对接硅基流动（SiliconFlow）OpenAI 兼容 API
 */
@Data
@Component
@ConfigurationProperties(prefix = "app.ai")
public class AiProperties {

    /** true=Mock 模式（本地关键词），false=真实 AI 调用 */
    private boolean mock = false;

    /** 硅基流动 API Key */
    private String apiKey;

    /** API 基础地址 */
    private String baseUrl = "https://api.siliconflow.cn";

    /** 模型名称 */
    private String model = "deepseek-ai/DeepSeek-R1-0528-Qwen3-8B";

    /** 温度参数 (0-2) */
    private double temperature = 0.7;

    /** 最大输出 token 数 */
    private int maxTokens = 1024;

    /** 系统提示词 */
    private String systemPrompt;
}
