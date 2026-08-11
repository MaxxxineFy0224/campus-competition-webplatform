package com.jingdui.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jingdui.common.BusinessException;
import com.jingdui.common.Result;
import com.jingdui.entity.Notification;
import com.jingdui.security.CurrentUser;
import com.jingdui.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 消息通知 API（全部需登录）
 *
 * - GET  /api/notifications             → 我的通知列表
 * - GET  /api/notifications/unread-count → 未读数量
 * - PUT  /api/notifications/{id}/read    → 标记已读
 * - PUT  /api/notifications/read-all     → 全部已读
 */
@Tag(name = "消息通知", description = "站内通知的查询、已读标记")
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(summary = "通知列表", description = "获取当前登录用户的通知列表（按时间倒序，分页）")
    @GetMapping
    public Result<Page<Notification>> list(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "20") int size) {
        Long userId = requireLogin();
        return Result.success(notificationService.listMyNotifications(userId, page, size));
    }

    @Operation(summary = "未读数量", description = "获取当前用户未读通知的数量（用于红点/角标展示）")
    @GetMapping("/unread-count")
    public Result<Map<String, Integer>> unreadCount() {
        Long userId = requireLogin();
        return Result.success(Map.of("unreadCount", notificationService.countUnread(userId)));
    }

    @Operation(summary = "标记已读", description = "将指定通知标记为已读（仅通知所有者可操作）")
    @PutMapping("/{id}/read")
    public Result<Void> markAsRead(@Parameter(description = "通知 ID") @PathVariable Long id) {
        Long userId = requireLogin();
        notificationService.markAsRead(id, userId);
        return Result.success(null);
    }

    @Operation(summary = "全部已读", description = "将当前用户所有未读通知一次性标记为已读")
    @PutMapping("/read-all")
    public Result<Void> markAllAsRead() {
        Long userId = requireLogin();
        notificationService.markAllAsRead(userId);
        return Result.success(null);
    }

    private Long requireLogin() {
        Long userId = CurrentUser.getUserId();
        if (userId == null) throw new BusinessException(401, "请先登录");
        return userId;
    }
}
