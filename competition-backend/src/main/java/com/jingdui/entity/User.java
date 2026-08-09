package com.jingdui.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户实体
 */
@Data
@TableName("users")
public class User {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    /** 密码哈希，不对外暴露 */
    @JsonIgnore
    private String passwordHash;

    private String avatar;
    private String school;
    private String major;
    private String grade;
    private String bio;

    /** 技能标签，逗号分隔 */
    private String skills;

    private LocalDateTime createdAt;
}
