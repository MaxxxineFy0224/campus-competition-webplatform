package com.jingdui.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 组队申请请求体
 */
@Data
public class ApplicationRequest {

    /** 组队帖 ID */
    @NotNull(message = "帖子 ID 不能为空")
    private Long postId;

    /** 申请留言 */
    @Size(max = 500, message = "申请留言不能超过500字")
    private String message;
}
