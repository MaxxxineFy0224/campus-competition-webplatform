package com.jingdui.service;

import com.jingdui.dto.UserUpdateRequest;
import com.jingdui.entity.TeamPost;
import com.jingdui.entity.User;

import java.util.List;

public interface UserService {

    /**
     * 获取用户信息
     */
    User getById(Long id);

    /**
     * 更新用户信息
     */
    User update(Long id, UserUpdateRequest request);

    /**
     * 获取用户发布的组队帖
     */
    List<TeamPost> getUserPosts(Long userId);

    /**
     * 获取用户收藏列表（按类型过滤）
     */
    List<?> getUserFavorites(Long userId, String type);
}
