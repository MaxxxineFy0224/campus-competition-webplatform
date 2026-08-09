package com.jingdui.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 收藏实体
 */
@Data
@TableName("favorites")
public class Favorite {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private Long itemId;

    /** 收藏类型：competition / team */
    private String itemType;

    private LocalDateTime createdAt;
}
