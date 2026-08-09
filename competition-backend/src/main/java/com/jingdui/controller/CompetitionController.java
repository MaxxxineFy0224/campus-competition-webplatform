package com.jingdui.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jingdui.common.BusinessException;
import com.jingdui.common.Result;
import com.jingdui.entity.Competition;
import com.jingdui.entity.TeamPost;
import com.jingdui.security.CurrentUser;
import com.jingdui.service.CompetitionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 竞赛相关 API（GET 公开，收藏需登录）
 */
@Tag(name = "竞赛", description = "竞赛信息浏览、搜索、收藏")
@RestController
@RequestMapping("/api/competitions")
@RequiredArgsConstructor
public class CompetitionController {

    private final CompetitionService competitionService;

    @GetMapping
    public Result<Page<Competition>> list(
            @RequestParam(required = false) String keyword,
            @Parameter(description = "分类筛选（如：数学建模、编程算法、创新创业等）")
            @RequestParam(required = false) String category,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "12") int size) {
        return Result.success(competitionService.listCompetitions(page, size, keyword, category));
    }

    @Operation(summary = "竞赛详情", description = "获取竞赛详细信息及关联的组队帖列表")
    @GetMapping("/{id}")
    public Result<Map<String, Object>> detail(@Parameter(description = "竞赛 ID") @PathVariable Long id) {
        Competition comp = competitionService.getById(id);
        List<TeamPost> posts = competitionService.getTeamPostsByCompetitionId(id);
        Map<String, Object> data = new HashMap<>();
        data.put("competition", comp);
        data.put("teamPosts", posts);
        return Result.success(data);
    }

    @Operation(summary = "切换收藏", description = "切换竞赛收藏状态（需登录），返回当前是否已收藏")
    @PostMapping("/{id}/favorite")
    public Result<Map<String, Boolean>> toggleFavorite(@PathVariable Long id) {
        Long userId = requireLogin();
        boolean favorited = competitionService.toggleFavorite(id, userId);
        return Result.success(Map.of("favorited", favorited));
    }

    @Operation(summary = "我的收藏", description = "获取当前登录用户收藏的竞赛列表（需登录，分页）")
    @GetMapping("/favorites")
    public Result<Page<Competition>> listFavorites(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "12") int size) {
        Long userId = requireLogin();
        return Result.success(competitionService.listFavorites(userId, page, size));
    }

    private Long requireLogin() {
        Long userId = CurrentUser.getUserId();
        if (userId == null) throw new BusinessException(401, "请先登录");
        return userId;
    }
}
