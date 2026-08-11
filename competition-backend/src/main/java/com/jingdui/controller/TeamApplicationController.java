package com.jingdui.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jingdui.common.BusinessException;
import com.jingdui.common.Result;
import com.jingdui.dto.ApplicationRequest;
import com.jingdui.dto.ReviewRequest;
import com.jingdui.entity.TeamApplication;
import com.jingdui.security.CurrentUser;
import com.jingdui.service.TeamApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 组队申请 API（全部需登录）
 *
 * - POST   /api/team-applications                  → 提交申请
 * - POST   /api/team-applications/{id}/review       → 审核申请
 * - DELETE /api/team-applications/{id}               → 取消申请
 * - GET    /api/team-applications/team-posts/{postId}  → 帖子申请列表
 * - GET    /api/team-applications/my                   → 我的申请记录
 */
@Tag(name = "组队申请", description = "组队帖的申请提交、审核、取消")
@RestController
@RequestMapping("/api/team-applications")
@RequiredArgsConstructor
public class TeamApplicationController {

    private final TeamApplicationService applicationService;

    @Operation(summary = "提交组队申请", description = "向指定组队帖提交入队申请（需登录）。不能申请自己的帖子，同一帖子不可重复申请。")
    @PostMapping
    public Result<TeamApplication> apply(@Valid @RequestBody ApplicationRequest request) {
        Long userId = requireLogin();
        TeamApplication application = applicationService.apply(request.getPostId(), userId, request.getMessage());
        return Result.success("申请已提交", application);
    }

    @Operation(summary = "审核申请", description = "通过或拒绝组队申请（需登录，仅帖子作者可操作）。status=1 通过，status=2 拒绝。")
    @PostMapping("/{id}/review")
    public Result<TeamApplication> review(
            @Parameter(description = "申请记录 ID") @PathVariable Long id,
            @Valid @RequestBody ReviewRequest request) {
        Long userId = requireLogin();
        TeamApplication application = applicationService.review(id, userId, request.getStatus(), request.getReply());
        String msg = request.getStatus() == 1 ? "已通过申请" : "已拒绝申请";
        return Result.success(msg, application);
    }

    @Operation(summary = "取消申请", description = "取消自己提交的组队申请（需登录，仅申请人本人可操作）")
    @DeleteMapping("/{id}")
    public Result<Void> cancel(@Parameter(description = "申请记录 ID") @PathVariable Long id) {
        Long userId = requireLogin();
        applicationService.cancel(id, userId);
        return Result.success("已取消申请", null);
    }

    @Operation(summary = "帖子申请列表", description = "查看指定组队帖的所有申请记录（需登录，仅帖子作者可看）")
    @GetMapping("/team-posts/{postId}")
    public Result<Page<TeamApplication>> listByPost(
            @Parameter(description = "组队帖 ID") @PathVariable Long postId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "12") int size) {
        requireLogin();
        return Result.success(applicationService.listByPost(postId, page, size));
    }

    @Operation(summary = "我的申请记录", description = "查看当前登录用户的所有组队申请记录（需登录，分页）")
    @GetMapping("/my")
    public Result<Page<TeamApplication>> listMyApplications(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "12") int size) {
        Long userId = requireLogin();
        return Result.success(applicationService.listMyApplications(userId, page, size));
    }

    private Long requireLogin() {
        Long userId = CurrentUser.getUserId();
        if (userId == null) throw new BusinessException(401, "请先登录");
        return userId;
    }
}
