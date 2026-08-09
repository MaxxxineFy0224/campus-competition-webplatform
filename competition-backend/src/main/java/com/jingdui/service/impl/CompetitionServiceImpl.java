package com.jingdui.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jingdui.common.BusinessException;
import com.jingdui.entity.Competition;
import com.jingdui.entity.Favorite;
import com.jingdui.entity.TeamPost;
import com.jingdui.mapper.CompetitionMapper;
import com.jingdui.mapper.FavoriteMapper;
import com.jingdui.mapper.TeamPostMapper;
import com.jingdui.service.CompetitionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CompetitionServiceImpl implements CompetitionService {

    private final CompetitionMapper competitionMapper;
    private final TeamPostMapper teamPostMapper;
    private final FavoriteMapper favoriteMapper;

    @Override
    public Page<Competition> listCompetitions(int page, int size, String keyword, String category) {
        LambdaQueryWrapper<Competition> wrapper = new LambdaQueryWrapper<>();

        // 关键词模糊搜索标题
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Competition::getTitle, keyword);
        }

        // 按分类筛选
        if (StringUtils.hasText(category)) {
            wrapper.eq(Competition::getCategory, category);
        }

        // 按截止日期升序
        wrapper.orderByAsc(Competition::getDeadline);

        Page<Competition> result = competitionMapper.selectPage(new Page<>(page, size), wrapper);

        // 动态计算 statusText
        result.getRecords().forEach(this::computeStatusText);

        return result;
    }

    @Override
    public Competition getById(Long id) {
        Competition comp = competitionMapper.selectById(id);
        if (comp == null) {
            throw new BusinessException(404, "竞赛不存在");
        }
        computeStatusText(comp);
        return comp;
    }

    @Override
    public List<TeamPost> getTeamPostsByCompetitionId(Long competitionId) {
        // 确保竞赛存在
        if (competitionMapper.selectById(competitionId) == null) {
            throw new BusinessException(404, "竞赛不存在");
        }
        return teamPostMapper.findByCompetitionId(competitionId);
    }

    @Override
    @Transactional
    public boolean toggleFavorite(Long competitionId, Long userId) {
        if (competitionMapper.selectById(competitionId) == null) {
            throw new BusinessException(404, "竞赛不存在");
        }

        Favorite existing = favoriteMapper.findByUserAndItem(userId, competitionId, "competition");
        if (existing != null) {
            // 已收藏 → 取消
            favoriteMapper.deleteById(existing.getId());
            return false;
        } else {
            // 未收藏 → 添加
            Favorite fav = new Favorite();
            fav.setUserId(userId);
            fav.setItemId(competitionId);
            fav.setItemType("competition");
            favoriteMapper.insert(fav);
            return true;
        }
    }

    @Override
    public Page<Competition> listFavorites(Long userId, int page, int size) {
        // 先查出所有收藏的竞赛ID，再分页（简单实现）
        List<Competition> allFavs = competitionMapper.findFavoritesByUserId(userId);
        allFavs.forEach(this::computeStatusText);

        // 手动分页
        int total = allFavs.size();
        int start = (page - 1) * size;
        int end = Math.min(start + size, total);

        Page<Competition> result = new Page<>(page, size);
        result.setTotal(total);
        result.setRecords(start < total ? allFavs.subList(start, end) : List.of());
        return result;
    }

    /* ---- 工具方法 ---- */

    /**
     * 根据 deadline 动态计算 statusText
     */
    private void computeStatusText(Competition comp) {
        if (comp.getDeadline() == null) {
            comp.setStatusText("未知");
            return;
        }
        long days = ChronoUnit.DAYS.between(LocalDate.now(), comp.getDeadline());
        if (days < 0) {
            comp.setStatusText("已截止");
        } else if (days <= 7) {
            comp.setStatusText("即将截止");
        } else {
            comp.setStatusText("报名中");
        }
    }
}
