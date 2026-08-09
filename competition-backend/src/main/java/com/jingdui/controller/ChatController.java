package com.jingdui.controller;

import com.jingdui.common.BusinessException;
import com.jingdui.config.AiProperties;
import com.jingdui.dto.ChatRequest;
import com.jingdui.security.CurrentUser;
import com.jingdui.service.ChatService;
import com.jingdui.service.SiliconFlowStreamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

/**
 * AI 聊天接口 —— SSE 流式响应
 *
 * - /api/ai/stream/chat  → SSE 流式（新版），OkHttp 直连硅基流动 → SseEmitter 推送，真正的打字机效果
 * - /api/chat             → SSE 流式（旧版兼容），与新版走同一条链路
 * - /api/ai-match/chat    → JSON 同步
 *
 * 两种模式由 app.ai.mock 控制：
 * - mock=true  → 本地关键词匹配，逐字延迟输出模拟打字效果
 * - mock=false → OkHttp 调用硅基流动 API（stream:true），解析 SSE delta.content 实时推送
 */
@Slf4j
@Tag(name = "AI 聊天", description = "AI 竞赛推荐助手的聊天接口，支持 SSE 流式和非流式两种模式")
@RestController
@RequestMapping("/api")
public class ChatController {

    private static final long SSE_TIMEOUT_MS = 60_000L;

    /** 同用户活跃流注册表 */
    private final ConcurrentHashMap<Long, SseEmitter> activeStreams = new ConcurrentHashMap<>();

    private final ChatService chatService;
    private final SiliconFlowStreamService siliconFlowStreamService;
    private final AiProperties aiProperties;
    private final ExecutorService aiExecutor;

    public ChatController(ChatService chatService,
                          SiliconFlowStreamService siliconFlowStreamService,
                          AiProperties aiProperties,
                          @Qualifier("aiExecutor") ExecutorService aiExecutor) {
        this.chatService = chatService;
        this.siliconFlowStreamService = siliconFlowStreamService;
        this.aiProperties = aiProperties;
        this.aiExecutor = aiExecutor;
    }

    // ===================================================================
    // SSE 流式端点（新版）
    // ===================================================================

    @Operation(summary = "SSE 流式聊天",
               description = "OkHttp 直连硅基流动 API（stream:true），解析 delta.content 逐块推送，真实打字机效果。需登录")
    @PostMapping(value = "/ai/stream/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter aiStreamChat(@Valid @RequestBody ChatRequest request) {
        Long userId = CurrentUser.getUserId();
        if (userId == null) {
            throw new BusinessException(401, "请先登录");
        }
        String message = request.getMessage();
        if (message == null || message.isBlank()) {
            return emitErrorOnly("消息不能为空");
        }

        log.info("[AI-Stream] userId={} messageLength={} mock={}", userId, message.length(), aiProperties.isMock());
        return createStreamEmitter(userId, message);
    }

    // ===================================================================
    // SSE 流式端点（旧版兼容）
    // ===================================================================

    @Operation(summary = "SSE 流式聊天（旧版兼容）", description = "与 /api/ai/stream/chat 走同一条链路。需登录")
    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chatStream(@Valid @RequestBody ChatRequest request) {
        Long userId = CurrentUser.getUserId();
        if (userId == null) {
            throw new BusinessException(401, "请先登录");
        }
        String message = request.getMessage();
        if (message == null || message.isBlank()) {
            return emitErrorOnly("消息不能为空");
        }

        log.info("[AI-Chat] userId={} messageLength={} mock={}", userId, message.length(), aiProperties.isMock());
        return createStreamEmitter(userId, message);
    }

    // ===================================================================
    // JSON 同步端点
    // ===================================================================

    @Operation(summary = "JSON 聊天", description = "AI 竞赛推荐助手，非流式 JSON 返回聊天结果")
    @PostMapping("/ai-match/chat")
    public Object chatJson(@Valid @RequestBody ChatRequest request) {
        log.info("[AI-JSON] messageLength={}", request.getMessage() != null ? request.getMessage().length() : 0);
        String reply = chatService.chat(request);
        return com.jingdui.common.Result.success(java.util.Map.of("reply", reply));
    }

    // ===================================================================
    // 统一流式处理：mock 模式逐字模拟 / 真实模式 OkHttp SSE 推送
    // ===================================================================

    private SseEmitter createStreamEmitter(Long userId, String message) {
        // 并发控制：取消同用户旧流
        SseEmitter oldEmitter = activeStreams.remove(userId);
        if (oldEmitter != null) {
            log.info("[AI] userId={} 取消旧流", userId);
            try { oldEmitter.completeWithError(new RuntimeException("新请求覆盖")); } catch (Exception ignored) {}
        }

        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MS);
        activeStreams.put(userId, emitter);
        long startTime = System.currentTimeMillis();

        Runnable cleanup = () -> {
            activeStreams.remove(userId, emitter);
            log.info("[AI] userId={} 流关闭, 耗时={}ms", userId, System.currentTimeMillis() - startTime);
        };
        emitter.onCompletion(cleanup);
        emitter.onTimeout(() -> {
            log.warn("[AI] userId={} 超时", userId);
            cleanup.run();
        });
        emitter.onError(ex -> {
            log.error("[AI] userId={} 连接异常: {}", userId, ex.getMessage());
            cleanup.run();
        });

        if (aiProperties.isMock()) {
            // Mock 模式：本地关键词匹配 → 逐字延迟输出，模拟打字效果
            aiExecutor.execute(() -> mockStreamToEmitter(message, emitter));
        } else {
            // 真实模式：OkHttp 直连硅基流动 SSE → SseEmitter 推送
            aiExecutor.execute(() -> {
                log.info("[AI] userId={} 开始调用硅基流动 SSE", userId);
                siliconFlowStreamService.streamToEmitter(message, emitter);
            });
        }

        return emitter;
    }

    // ===================================================================
    // Mock 流式输出：将完整回复逐字推送给前端，模拟打字机效果
    // ===================================================================

    private void mockStreamToEmitter(String message, SseEmitter emitter) {
        try {
            com.jingdui.dto.ChatRequest request = new com.jingdui.dto.ChatRequest();
            request.setMessage(message);
            String fullReply = chatService.chat(request);

            for (int i = 0; i < fullReply.length(); i++) {
                if (isClosed(emitter)) {
                    log.info("[AI-Mock] 客户端已断开，停止输出");
                    return;
                }
                String ch = String.valueOf(fullReply.charAt(i));
                sendData(emitter, "{\"content\":\"" + escapeJson(ch) + "\"}");
                Thread.sleep(25 + (long) (Math.random() * 15));
            }

            if (!isClosed(emitter)) {
                sendEvent(emitter, "done", "{}");
                emitter.complete();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("[AI-Mock] 线程中断");
            safeComplete(emitter);
        } catch (IOException e) {
            log.error("[AI-Mock] SSE 发送异常: {}", e.getMessage());
            safeComplete(emitter);
        }
    }

    // ===================================================================
    // SSE 工具方法
    // ===================================================================

    private void sendData(SseEmitter emitter, String data) throws IOException {
        emitter.send(SseEmitter.event().data(data));
    }

    private void sendEvent(SseEmitter emitter, String event, String data) throws IOException {
        emitter.send(SseEmitter.event().name(event).data(data));
    }

    private void sendError(SseEmitter emitter, String errorMessage) throws IOException {
        emitter.send(SseEmitter.event().name("error")
                .data("{\"message\":\"" + escapeJson(errorMessage) + "\"}"));
    }

    private SseEmitter emitErrorOnly(String errorMessage) {
        SseEmitter emitter = new SseEmitter(3000L);
        try { sendError(emitter, errorMessage); emitter.complete(); }
        catch (IOException e) { emitter.completeWithError(e); }
        return emitter;
    }

    private boolean isClosed(SseEmitter emitter) {
        try { emitter.send(SseEmitter.event().comment("ping").build()); return false; }
        catch (Exception e) { return true; }
    }

    private void safeComplete(SseEmitter emitter) {
        try { emitter.complete(); } catch (Exception ignored) {}
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"':  sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\n': sb.append("\\n");  break;
                case '\r': sb.append("\\r");  break;
                case '\t': sb.append("\\t");  break;
                default:   sb.append(c);
            }
        }
        return sb.toString();
    }
}
