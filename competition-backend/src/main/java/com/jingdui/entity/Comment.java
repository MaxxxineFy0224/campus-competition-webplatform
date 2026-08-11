package com.jingdui.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 评论实体
 *
 * parent_id 为 NULL 表示顶级评论，非 NULL 表示回复某条评论。
 */
@Data
@TableName("comments")
public class Comment {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属组队帖 ID */
    private Long teamPostId;

    /** 评论用户 ID */
    private Long userId;

    /** 父评论 ID（NULL = 顶级评论） */
    private Long parentId;

    /** 评论内容 */
    private String content;

    /** 点赞数 */
    private Integer likeCount;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    /* ---- 非数据库字段（联表查询填充） ---- */

    /** 评论用户名称 */
    @TableField(exist = false)
    private String userName;

    /** 评论用户头像 */
    @TableField(exist = false)
    private String userAvatar;

    /** 子回复列表 */
    @TableField(exist = false)
    private List<Comment> replies;
}
