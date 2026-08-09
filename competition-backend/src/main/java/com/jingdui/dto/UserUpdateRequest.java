package com.jingdui.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 更新用户信息请求体
 */
@Data
public class UserUpdateRequest {

    @NotBlank(message = "昵称不能为空")
    @Size(max = 50, message = "昵称不能超过50个字")
    private String name;

    @Size(max = 255, message = "头像URL过长")
    private String avatar;

    @Size(max = 100, message = "学校名称过长")
    private String school;

    @Size(max = 100, message = "专业名称过长")
    private String major;

    @Size(max = 20, message = "年级格式错误")
    private String grade;

    @Size(max = 500, message = "个人简介不能超过500字")
    private String bio;

    @Size(max = 500, message = "技能标签过长")
    private String skills;
}
