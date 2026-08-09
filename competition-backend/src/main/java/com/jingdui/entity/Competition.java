package com.jingdui.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 竞赛实体
 */
@Data
@TableName("competitions")
public class Competition {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;
    private String category;
    private String level;
    private String organizer;

    /** 报名截止日期 */
    private LocalDate deadline;

    /** 竞赛日期 */
    private LocalDate eventDate;

    private String location;

    private Integer minTeamSize;
    private Integer maxTeamSize;

    private String description;
    private String website;
    private String prize;
    private String imageUrl;

    /** 冗余状态：0-报名中 1-即将截止 2-已截止 */
    private Integer status;

    private LocalDateTime createdAt;

    /* ---- 非数据库字段 ---- */

    /** 动态计算的状态文本 */
    @TableField(exist = false)
    private String statusText;
}
