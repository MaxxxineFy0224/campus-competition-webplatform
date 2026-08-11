package com.jingdui.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jingdui.common.BusinessException;
import com.jingdui.entity.Notification;
import com.jingdui.mapper.NotificationMapper;
import com.jingdui.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 消息通知服务实现（站内信）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationMapper notificationMapper;

    // ================================================================
    // 发送通知
    // ================================================================
    @Override
    public Notification send(Long userId, String type, String content, Long bizId) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setType(type);
        notification.setTitle(deriveTitle(type));
        notification.setContent(content);
        notification.setIsRead(0);
        notification.setRelatedId(bizId);
        notification.setRelatedType(type);
        notification.setCreatedAt(LocalDateTime.now());
        notification.setUpdatedAt(LocalDateTime.now());

        notificationMapper.insert(notification);
        log.info("[通知] userId={} type={} content={}", userId, type, content);
        return notification;
    }

    // ================================================================
    // 标记已读
    // ================================================================
    @Override
    public void markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationMapper.selectById(notificationId);
        if (notification == null) {
            throw new BusinessException(404, "通知不存在");
        }
        if (!notification.getUserId().equals(userId)) {
            throw new BusinessException(403, "只能操作自己的通知");
        }
        notificationMapper.markAsRead(notificationId);
    }

    // ================================================================
    // 全部标记已读
    // ================================================================
    @Override
    public void markAllAsRead(Long userId) {
        int count = notificationMapper.markAllAsRead(userId);
        log.info("[通知] userId={} 全部已读，共 {} 条", userId, count);
    }

    // ================================================================
    // 未读数量
    // ================================================================
    @Override
    public int countUnread(Long userId) {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, userId)
               .eq(Notification::getIsRead, 0);
        return notificationMapper.selectCount(wrapper).intValue();
    }

    // ================================================================
    // 通知列表
    // ================================================================
    @Override
    public Page<Notification> listMyNotifications(Long userId, int page, int size) {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, userId)
               .orderByDesc(Notification::getCreatedAt);
        return notificationMapper.selectPage(new Page<>(page, size), wrapper);
    }

    // ================================================================
    // 工具方法
    // ================================================================

    /** 根据通知类型派生默认标题 */
    private String deriveTitle(String type) {
        return switch (type) {
            case "application" -> "组队申请通知";
            case "comment" -> "评论通知";
            default -> "系统通知";
        };
    }
}
