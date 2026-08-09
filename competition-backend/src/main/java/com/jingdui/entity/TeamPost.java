package com.jingdui.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 组队帖实体
 */
@Data
@TableName("team_posts")
public class TeamPost {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联竞赛 ID */
    private Long competitionId;

    /** 发布者用户 ID */
    private Long authorId;

    private String title;
    private String description;

    /** 所需技能，逗号分隔 */
    private String requiredSkills;

    private String contact;

    /** 组队截止日期 */
    private LocalDate deadline;

    /** 需要人数 */
    private Integer needCount;

    /** 当前人数 */
    private Integer currentCount;

    /** 0-招募中 1-已过期 */
    private Integer status;

    private LocalDateTime createdAt;

    /* ---- 非数据库字段（联表查询填充） ---- */

    @TableField(exist = false)
    private String competitionTitle;

    @TableField(exist = false)
    private String authorName;

    @TableField(exist = false)
    private String competitionCategory;

    @TableField(exist = false)
    private Boolean expired;
}
