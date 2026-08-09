package com.jingdui.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jingdui.dto.TeamPostRequest;
import com.jingdui.entity.TeamPost;

public interface TeamPostService {

    /**
     * 分页查询组队帖，支持按分类筛选
     */
    Page<TeamPost> listTeamPosts(int page, int size, String category);

    /**
     * 查询组队帖详情（含作者、竞赛信息）
     */
    TeamPost getById(Long id);

    /**
     * 发布组队帖
     */
    TeamPost create(TeamPostRequest request);

    /**
     * 删除组队帖（仅作者可删）
     */
    void delete(Long postId, Long userId);

    /**
     * 切换收藏状态
     */
    boolean toggleFavorite(Long postId, Long userId);

    /**
     * 获取用户收藏的组队帖
     */
    Page<TeamPost> listFavorites(Long userId, int page, int size);
}
