package com.jingdui.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jingdui.entity.Competition;
import com.jingdui.entity.TeamPost;

import java.util.List;

public interface CompetitionService {

    /**
     * 分页查询竞赛列表，支持关键词搜索和分类筛选
     */
    Page<Competition> listCompetitions(int page, int size, String keyword, String category);

    /**
     * 查询竞赛详情
     */
    Competition getById(Long id);

    /**
     * 查询某竞赛下的组队帖列表
     */
    List<TeamPost> getTeamPostsByCompetitionId(Long competitionId);

    /**
     * 切换收藏状态，返回操作后是否已收藏
     */
    boolean toggleFavorite(Long competitionId, Long userId);

    /**
     * 获取用户收藏的竞赛列表
     */
    Page<Competition> listFavorites(Long userId, int page, int size);
}
