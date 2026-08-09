package com.jingdui.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jingdui.config.AiProperties;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 硅基流动（SiliconFlow）流式调用服务
 *
 * 使用 OkHttp 直连硅基流动 OpenAI 兼容 API，解析 SSE 流，
 * 提取 delta.content 逐块推送给 SseEmitter，实现打字机效果。
 *
 * 关键点：
 * - 请求时 stream: true
 * - 返回 SSE 流，逐行读取 data: 行
 * - 提取 choices[0].delta.content（不是 message.content）
 * - 读到 [DONE] 代表大模型输出结束
 */
@Slf4j
@Service
public class SiliconFlowStreamService {

    private final OkHttpClient httpClient;
    private final AiProperties aiProperties;
    private final ObjectMapper objectMapper;

    private static final MediaType JSON_MEDIA_TYPE = MediaType.get("application/json; charset=utf-8");

    public SiliconFlowStreamService(OkHttpClient httpClient,
                                     AiProperties aiProperties,
                                     ObjectMapper objectMapper) {
        this.httpClient = httpClient;
        this.aiProperties = aiProperties;
        this.objectMapper = objectMapper;
    }

    /**
     * 流式聊天 —— 将硅基流动 SSE 流逐块写入 SseEmitter
     *
     * 此方法在 aiExecutor 线程中调用，全程阻塞直到流结束或异常。
     *
     * @param userMessage 用户消息
     * @param emitter     SseEmitter 实例
     */
    public void streamToEmitter(String userMessage, SseEmitter emitter) {
        String apiUrl = aiProperties.getBaseUrl() + "/v1/chat/completions";
        String model = aiProperties.getModel();
        String systemPrompt = aiProperties.getSystemPrompt();

        // 构建请求体
        Map<String, Object> requestBody = buildRequestBody(model, systemPrompt, userMessage, true);

        String requestJson;
        try {
            requestJson = objectMapper.writeValueAsString(requestBody);
        } catch (Exception e) {
            log.error("[SiliconFlow] 序列化请求体失败: {}", e.getMessage(), e);
            safeEmitError(emitter, "请求序列化失败：" + e.getMessage());
            safeComplete(emitter);
            return;
        }

        log.info("[SiliconFlow] → POST {} model={} stream=true messageLength={}",
                apiUrl, model, userMessage != null ? userMessage.length() : 0);
        log.debug("[SiliconFlow] 请求体: {}", requestJson);

        Request request = new Request.Builder()
                .url(apiUrl)
                .header("Authorization", "Bearer " + aiProperties.getApiKey())
                .header("Content-Type", "application/json")
                .post(RequestBody.create(requestJson, JSON_MEDIA_TYPE))
                .build();

        Response response = null;
        try {
            response = httpClient.newCall(request).execute();

            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "(empty)";
                log.error("[SiliconFlow] API 返回错误 status={} body={}", response.code(), errorBody);
                safeEmitError(emitter, "AI 服务返回错误 " + response.code() + "：" + errorBody);
                safeComplete(emitter);
                return;
            }

            // 逐行读取 SSE 流
            ResponseBody body = response.body();
            if (body == null) {
                log.error("[SiliconFlow] 响应体为空");
                safeEmitError(emitter, "AI 服务返回空响应");
                safeComplete(emitter);
                return;
            }

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(body.byteStream(), StandardCharsets.UTF_8))) {

                String line;
                int chunkCount = 0;
                StringBuilder fullContent = new StringBuilder();

                while ((line = reader.readLine()) != null) {
                    // SSE 只处理 data: 行
                    if (!line.startsWith("data: ")) {
                        continue;
                    }

                    String data = line.substring(6).trim();

                    // [DONE] 标记 — 大模型输出结束
                    if ("[DONE]".equals(data)) {
                        log.info("[SiliconFlow] ← 收到 [DONE]，共 {} 个 chunk，总长度={}",
                                chunkCount, fullContent.length());
                        log.debug("[SiliconFlow] 完整内容: {}", fullContent);
                        safeEmitEvent(emitter, "done", "{}");
                        safeComplete(emitter);
                        return;
                    }

                    // 解析 JSON 提取 delta.content
                    try {
                        JsonNode root = objectMapper.readTree(data);
                        JsonNode choices = root.path("choices");
                        if (choices.isArray() && choices.size() > 0) {
                            JsonNode delta = choices.get(0).path("delta");
                            JsonNode contentNode = delta.path("content");

                            if (!contentNode.isMissingNode() && !contentNode.isNull()) {
                                String content = contentNode.asText();
                                if (!content.isEmpty()) {
                                    chunkCount++;
                                    fullContent.append(content);

                                    log.debug("[SiliconFlow] chunk#{} content=\"{}\"", chunkCount,
                                            content.replace("\n", "\\n").replace("\r", "\\r"));

                                    // 推送 chunk 到前端
                                    String sseData = objectMapper.writeValueAsString(
                                            Map.of("content", content));
                                    safeEmitData(emitter, sseData);
                                }
                            }
                        }
                    } catch (Exception e) {
                        log.warn("[SiliconFlow] 解析 chunk JSON 失败, data={}: {}", data, e.getMessage());
                        // 跳过解析失败的行，不中断流程
                    }
                }

                // 正常读到流末尾（没有 [DONE] 的情况，兜底）
                log.info("[SiliconFlow] 流结束（无 [DONE]），共 {} 个 chunk", chunkCount);
                safeEmitEvent(emitter, "done", "{}");
                safeComplete(emitter);

            }
        } catch (IOException e) {
            log.error("[SiliconFlow] 网络异常: {}", e.getMessage(), e);
            safeEmitError(emitter, "连接 AI 服务失败：" + e.getMessage());
            safeComplete(emitter);
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }

    /**
     * 同步聊天（非流式）—— 等待完整回复后返回
     *
     * @param userMessage 用户消息
     * @return AI 完整回复文本
     */
    public String chatSync(String userMessage) {
        String apiUrl = aiProperties.getBaseUrl() + "/v1/chat/completions";
        String model = aiProperties.getModel();
        String systemPrompt = aiProperties.getSystemPrompt();

        Map<String, Object> requestBody = buildRequestBody(model, systemPrompt, userMessage, false);

        String requestJson;
        try {
            requestJson = objectMapper.writeValueAsString(requestBody);
        } catch (Exception e) {
            log.error("[SiliconFlow] 序列化请求体失败: {}", e.getMessage(), e);
            return "抱歉，请求序列化失败：" + e.getMessage();
        }

        log.info("[SiliconFlow-Sync] → POST {} model={} stream=false messageLength={}",
                apiUrl, model, userMessage != null ? userMessage.length() : 0);

        Request request = new Request.Builder()
                .url(apiUrl)
                .header("Authorization", "Bearer " + aiProperties.getApiKey())
                .header("Content-Type", "application/json")
                .post(RequestBody.create(requestJson, JSON_MEDIA_TYPE))
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "(empty)";
                log.error("[SiliconFlow-Sync] API 返回错误 status={} body={}", response.code(), errorBody);
                return "抱歉，AI 服务返回错误 " + response.code() + "：" + errorBody;
            }

            String responseBody = response.body() != null ? response.body().string() : "";
            log.debug("[SiliconFlow-Sync] 响应: {}", responseBody);

            // 解析 OpenAI 格式：choices[0].message.content
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode choices = root.path("choices");
            if (choices.isArray() && choices.size() > 0) {
                String content = choices.get(0).path("message").path("content").asText();
                log.info("[SiliconFlow-Sync] ← 回复长度={}", content != null ? content.length() : 0);
                return content != null ? content : "";
            }

            return "";
        } catch (IOException e) {
            log.error("[SiliconFlow-Sync] 网络异常: {}", e.getMessage(), e);
            return "抱歉，连接 AI 服务失败：" + e.getMessage();
        }
    }

    // ======================== 请求体构建 ========================

    /**
     * 构建 OpenAI 兼容的 Chat Completions 请求体
     *
     * @param model        模型名称
     * @param systemPrompt 系统提示词
     * @param userMessage  用户消息
     * @param stream       是否流式
     */
    private Map<String, Object> buildRequestBody(String model, String systemPrompt,
                                                  String userMessage, boolean stream) {
        Map<String, Object> body = new HashMap<>();
        body.put("model", model);
        body.put("stream", stream);
        body.put("temperature", aiProperties.getTemperature());
        body.put("max_tokens", aiProperties.getMaxTokens());

        // messages 数组
        List<Map<String, String>> messages = new ArrayList<>();

        if (systemPrompt != null && !systemPrompt.isBlank()) {
            Map<String, String> sysMsg = new HashMap<>();
            sysMsg.put("role", "system");
            sysMsg.put("content", systemPrompt);
            messages.add(sysMsg);
        }

        Map<String, String> userMsg = new HashMap<>();
        userMsg.put("role", "user");
        userMsg.put("content", userMessage != null ? userMessage : "");
        messages.add(userMsg);

        body.put("messages", messages);
        return body;
    }

    // ======================== SseEmitter 安全操作 ========================

    private void safeEmitData(SseEmitter emitter, String data) {
        try {
            emitter.send(SseEmitter.event().data(data));
        } catch (IOException e) {
            log.warn("[SiliconFlow] SseEmitter send data 失败: {}", e.getMessage());
        }
    }

    private void safeEmitEvent(SseEmitter emitter, String event, String data) {
        try {
            emitter.send(SseEmitter.event().name(event).data(data));
        } catch (IOException e) {
            log.warn("[SiliconFlow] SseEmitter send event 失败: {}", e.getMessage());
        }
    }

    private void safeEmitError(SseEmitter emitter, String errorMessage) {
        try {
            Map<String, String> errorData = Map.of("message", errorMessage);
            String json = objectMapper.writeValueAsString(errorData);
            emitter.send(SseEmitter.event().name("error").data(json));
        } catch (IOException e) {
            log.warn("[SiliconFlow] SseEmitter send error 失败: {}", e.getMessage());
        }
    }

    private void safeComplete(SseEmitter emitter) {
        try {
            emitter.complete();
        } catch (Exception e) {
            log.warn("[SiliconFlow] SseEmitter complete 失败: {}", e.getMessage());
        }
    }
}
