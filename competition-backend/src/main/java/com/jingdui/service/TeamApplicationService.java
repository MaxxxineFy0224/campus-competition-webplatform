package com.jingdui.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jingdui.entity.TeamApplication;

/**
 * 组队申请服务
 */
public interface TeamApplicationService {

    /**
     * 提交组队申请
     *
     * @param postId  组队帖 ID
     * @param userId  申请人用户 ID
     * @param message 申请留言
     * @return 创建的申请记录
     */
    TeamApplication apply(Long postId, Long userId, String message);

    /**
     * 审核申请（通过或拒绝）
     *
     * @param applicationId 申请记录 ID
     * @param authorId      审核者（帖子作者）ID
     * @param status        1=通过  2=拒绝
     * @param reply         审核回复/理由
     * @return 更新后的申请记录
     */
    TeamApplication review(Long applicationId, Long authorId, Integer status, String reply);

    /**
     * 取消申请
     *
     * @param applicationId 申请记录 ID
     * @param userId        操作者（仅限申请人本人）
     */
    void cancel(Long applicationId, Long userId);

    /**
     * 查看某个帖子的所有申请（仅帖子作者可看）
     *
     * @param postId 组队帖 ID
     * @param page   页码
     * @param size   每页条数
     * @return 分页申请列表
     */
    Page<TeamApplication> listByPost(Long postId, int page, int size);

    /**
     * 查看我的申请记录
     *
     * @param userId 申请人 ID
     * @param page   页码
     * @param size   每页条数
     * @return 分页申请列表
     */
    Page<TeamApplication> listMyApplications(Long userId, int page, int size);
}
