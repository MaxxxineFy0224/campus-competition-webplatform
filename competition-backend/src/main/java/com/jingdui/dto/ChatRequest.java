package com.jingdui.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * AI 聊天请求体
 */
@Data
public class ChatRequest {

    /** 用户消息，最长 2000 字符 */
    @Size(max = 2000, message = "消息长度不能超过2000个字符")
    private String message;

    /** 用户 ID 由后端从 JWT 提取，前端无需传此字段 */
}
