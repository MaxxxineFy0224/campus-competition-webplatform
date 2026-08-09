package com.jingdui.controller;

import com.jingdui.common.BusinessException;
import com.jingdui.common.Result;
import com.jingdui.dto.UserUpdateRequest;
import com.jingdui.entity.TeamPost;
import com.jingdui.entity.User;
import com.jingdui.security.CurrentUser;
import com.jingdui.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户 API
 */
@Tag(name = "用户", description = "用户信息查询、更新、发布的帖子与收藏")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "查看用户信息", description = "查看任意用户的公开信息（密码哈希被隐藏）")
    @GetMapping("/{id}")
    public Result<User> getById(@PathVariable Long id) {
        User user = userService.getById(id);
        // 隐藏密码哈希
        user.setPasswordHash(null);
        return Result.success(user);
    }

    @Operation(summary = "获取当前用户", description = "获取当前登录用户的完整信息（需登录）")
    @GetMapping("/me")
    public Result<User> me() {
        Long userId = requireLogin();
        User user = userService.getById(userId);
        user.setPasswordHash(null);
        return Result.success(user);
    }

    @Operation(summary = "更新个人信息", description = "更新用户个人信息（需登录，仅可修改自己的信息）")
    @PutMapping("/{id}")
    public Result<User> update(@PathVariable Long id, @Valid @RequestBody UserUpdateRequest request) {
        Long currentUserId = requireLogin();
        if (!currentUserId.equals(id)) {
            throw new BusinessException(403, "只能修改自己的信息");
        }
        return Result.success("个人信息已更新", userService.update(id, request));
    }

    @Operation(summary = "用户发布的帖子", description = "获取指定用户发布的组队帖列表")
    @GetMapping("/{id}/posts")
    public Result<List<TeamPost>> getUserPosts(@Parameter(description = "用户 ID") @PathVariable Long id) {
        return Result.success(userService.getUserPosts(id));
    }

    @Operation(summary = "用户收藏列表", description = "获取指定用户的收藏列表（按类型筛选）")
    @GetMapping("/{id}/favorites")
    public Result<List<?>> getUserFavorites(
            @Parameter(description = "用户 ID") @PathVariable Long id,
            @Parameter(description = "收藏类型：competition / team")
            @RequestParam(defaultValue = "competition") String type) {
        return Result.success(userService.getUserFavorites(id, type));
    }

    private Long requireLogin() {
        Long userId = CurrentUser.getUserId();
        if (userId == null) throw new BusinessException(401, "请先登录");
        return userId;
    }
}
