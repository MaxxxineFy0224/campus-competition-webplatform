package com.jingdui.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 消息通知实体
 *
 * type: system=系统通知  application=组队申请通知  comment=评论通知
 */
@Data
@TableName("notifications")
public class Notification {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 接收用户 ID */
    private Long userId;

    /** 通知类型：system / application / comment */
    private String type;

    /** 通知标题 */
    private String title;

    /** 通知内容 */
    private String content;

    /** 0=未读 1=已读 */
    private Integer isRead;

    /** 关联业务 ID */
    private Long relatedId;

    /** 关联业务类型 */
    private String relatedType;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
