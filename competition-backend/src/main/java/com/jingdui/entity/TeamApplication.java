package com.jingdui.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 组队申请实体
 *
 * status: 0=待审核  1=已通过  2=已拒绝
 */
@Data
@TableName("team_applications")
public class TeamApplication {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 组队帖 ID */
    private Long teamPostId;

    /** 申请人用户 ID */
    private Long applicantId;

    /** 申请留言 */
    private String message;

    /** 0=待审核 1=已通过 2=已拒绝 */
    private Integer status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    /* ---- 非数据库字段（联表查询填充） ---- */

    /** 申请人名称 */
    @TableField(exist = false)
    private String applicantName;

    /** 组队帖标题 */
    @TableField(exist = false)
    private String teamPostTitle;
}
