package com.jingdui.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 审核申请请求体
 *
 * status: 1=通过  2=拒绝
 */
@Data
public class ReviewRequest {

    /** 审核结果：1=通过  2=拒绝 */
    @NotNull(message = "审核结果不能为空")
    @Min(value = 1, message = "审核结果必须为 1 或 2")
    @Max(value = 2, message = "审核结果必须为 1 或 2")
    private Integer status;

    /** 审核回复（拒绝时建议填写理由） */
    @Size(max = 500, message = "审核回复不能超过500字")
    private String reply;
}
