package com.jingdui.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

/**
 * 发布组队帖请求体
 */
@Data
public class TeamPostRequest {

    /** 关联竞赛 ID */
    @NotNull(message = "请选择竞赛")
    private Long competitionId;

    /** 发布者用户 ID（后端从 JWT 提取，前端无需传） */
    private Long authorId;

    /** 帖子标题（可为空，后端自动生成） */
    @Size(max = 100, message = "标题不能超过100个字")
    private String title;

    /** 队伍介绍 */
    @NotBlank(message = "请输入队伍介绍")
    @Size(max = 500, message = "队伍介绍不能超过500字")
    private String description;

    /** 所需技能，逗号分隔 */
    @Size(max = 500, message = "技能标签过长")
    private String requiredSkills;

    /** 联系方式 */
    @NotBlank(message = "请输入联系方式")
    @Size(min = 2, max = 30, message = "联系方式长度需在2-30字之间")
    private String contact;

    /** 组队截止日期 */
    @NotNull(message = "请选择组队截止日期")
    @Future(message = "请选择未来的日期")
    private LocalDate deadline;

    /** 需要人数 */
    private Integer needCount = 1;
}
