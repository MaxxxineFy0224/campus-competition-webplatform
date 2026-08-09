package com.jingdui.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jingdui.common.BusinessException;
import com.jingdui.common.Result;
import com.jingdui.dto.TeamPostRequest;
import com.jingdui.entity.TeamPost;
import com.jingdui.security.CurrentUser;
import com.jingdui.service.TeamPostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 组队帖 API（GET 公开，写操作需登录）
 */
@Tag(name = "组队帖", description = "组队帖的发布、浏览、删除、收藏")
@RestController
@RequestMapping("/api/team-posts")
@RequiredArgsConstructor
public class TeamPostController {

    private final TeamPostService teamPostService;

    @Operation(summary = "组队帖列表", description = "分页查询组队帖列表，支持分类筛选")
    @GetMapping
    public Result<Page<TeamPost>> list(
            @Parameter(description = "分类筛选")
            @RequestParam(required = false) String category,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "12") int size) {
        return Result.success(teamPostService.listTeamPosts(page, size, category));
    }

    @Operation(summary = "组队帖详情", description = "获取单个组队帖的详细信息")
    @GetMapping("/{id}")
    public Result<TeamPost> detail(@Parameter(description = "组队帖 ID") @PathVariable Long id) {
        return Result.success(teamPostService.getById(id));
    }

    @Operation(summary = "发布组队帖", description = "发布新的组队招募帖（需登录）")
    @PostMapping
    public Result<TeamPost> create(@Valid @RequestBody TeamPostRequest request) {
        Long userId = requireLogin();
        request.setAuthorId(userId);
        return Result.success("发布成功", teamPostService.create(request));
    }

    @Operation(summary = "删除组队帖", description = "删除自己发布的组队帖（仅作者可操作，需登录）")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        Long userId = requireLogin();
        teamPostService.delete(id, userId);
        return Result.success("删除成功", null);
    }

    @Operation(summary = "切换收藏", description = "切换组队帖收藏状态（需登录），返回当前是否已收藏")
    @PostMapping("/{id}/favorite")
    public Result<Map<String, Boolean>> toggleFavorite(@PathVariable Long id) {
        Long userId = requireLogin();
        boolean favorited = teamPostService.toggleFavorite(id, userId);
        return Result.success(Map.of("favorited", favorited));
    }

    @Operation(summary = "我收藏的组队帖", description = "获取当前登录用户收藏的组队帖列表（需登录，分页）")
    @GetMapping("/favorites")
    public Result<Page<TeamPost>> listFavorites(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "12") int size) {
        Long userId = requireLogin();
        return Result.success(teamPostService.listFavorites(userId, page, size));
    }

    private Long requireLogin() {
        Long userId = CurrentUser.getUserId();
        if (userId == null) throw new BusinessException(401, "请先登录");
        return userId;
    }
}
