package com.jingdui.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jingdui.entity.Notification;

/**
 * 消息通知服务（站内信）
 */
public interface NotificationService {

    /**
     * 发送一条通知
     *
     * @param userId  接收用户 ID
     * @param type    通知类型（system / application / comment）
     * @param content 通知内容
     * @param bizId   关联业务 ID（可为 null）
     * @return 创建的 Notification 对象
     */
    Notification send(Long userId, String type, String content, Long bizId);

    /**
     * 标记单条通知为已读
     *
     * @param notificationId 通知 ID
     * @param userId         当前用户（校验是否为通知所有者）
     */
    void markAsRead(Long notificationId, Long userId);

    /**
     * 标记所有未读通知为已读
     *
     * @param userId 当前用户
     */
    void markAllAsRead(Long userId);

    /**
     * 统计未读通知数量
     *
     * @param userId 当前用户
     */
    int countUnread(Long userId);

    /**
     * 我的通知列表（分页、按时间倒序）
     *
     * @param userId 当前用户
     * @param page   页码
     * @param size   每页条数
     */
    Page<Notification> listMyNotifications(Long userId, int page, int size);
}
