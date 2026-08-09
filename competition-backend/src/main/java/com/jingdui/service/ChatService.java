package com.jingdui.service;

import com.jingdui.dto.ChatRequest;

public interface ChatService {

    /**
     * 同步聊天（返回完整回复），供 /api/ai-match/chat JSON 端点使用
     */
    String chat(ChatRequest request);
}
