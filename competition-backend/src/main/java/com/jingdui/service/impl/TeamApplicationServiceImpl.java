package com.jingdui.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jingdui.common.BusinessException;
import com.jingdui.entity.TeamApplication;
import com.jingdui.entity.TeamPost;
import com.jingdui.entity.User;
import com.jingdui.mapper.TeamApplicationMapper;
import com.jingdui.mapper.TeamPostMapper;
import com.jingdui.mapper.UserMapper;
import com.jingdui.service.NotificationService;
import com.jingdui.service.TeamApplicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 组队申请服务实现
 *
 * 规则：
 * - 不能申请自己的帖子
 * - 同一帖子不允许重复待审核申请
 * - 通过时帖子 current_count+1，满员则帖子状态改为已招满
 * - 审核/申请操作均发通知给对方
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TeamApplicationServiceImpl implements TeamApplicationService {

    private final TeamApplicationMapper applicationMapper;
    private final TeamPostMapper teamPostMapper;
    private final UserMapper userMapper;
    private final NotificationService notificationService;

    /** 申请状态常量 */
    private static final int STATUS_PENDING  = 0;  // 待审核
    private static final int STATUS_APPROVED = 1;  // 已通过
    private static final int STATUS_REJECTED = 2;  // 已拒绝

    /** 帖子状态常量 */
    private static final int POST_RECRUITING = 0;  // 招募中
    private static final int POST_FULL       = 1;  // 已招满

    // ================================================================
    // 提交申请
    // ================================================================
    @Override
    @Transactional
    public TeamApplication apply(Long postId, Long userId, String message) {
        // 1. 校验帖子存在
        TeamPost post = teamPostMapper.selectById(postId);
        if (post == null) {
            throw new BusinessException(404, "组队帖不存在");
        }

        // 2. 不能申请自己的帖子
        if (post.getAuthorId().equals(userId)) {
            throw new BusinessException(400, "不能申请自己发布的帖子");
        }

        // 3. 不能重复申请（待审核状态）
        TeamApplication existing = applicationMapper.findPendingByUserAndPost(userId, postId);
        if (existing != null) {
            throw new BusinessException(400, "你已提交过申请，请耐心等待审核");
        }

        // 4. 校验帖子是否已招满
        if (post.getStatus() != null && post.getStatus() == POST_FULL) {
            throw new BusinessException(400, "该队伍已招满");
        }

        // 5. 创建申请记录
        TeamApplication application = new TeamApplication();
        application.setTeamPostId(postId);
        application.setApplicantId(userId);
        application.setMessage(message);
        application.setStatus(STATUS_PENDING);
        application.setCreatedAt(LocalDateTime.now());
        application.setUpdatedAt(LocalDateTime.now());
        applicationMapper.insert(application);

        log.info("[申请] applicationId={} postId={} userId={}", application.getId(), postId, userId);

        // 6. 通知帖子作者
        User applicant = userMapper.selectById(userId);
        String applicantName = applicant != null ? applicant.getName() : "有用户";
        String postTitle = post.getTitle() != null ? post.getTitle() : "你的帖子";
        notificationService.send(
                post.getAuthorId(),
                "application",
                applicantName + " 申请加入「" + postTitle + "」" +
                        (message != null && !message.isBlank() ? "，留言：" + message : ""),
                application.getId()
        );

        return application;
    }

    // ================================================================
    // 审核申请
    // ================================================================
    @Override
    @Transactional
    public TeamApplication review(Long applicationId, Long authorId, Integer status, String reply) {
        // 1. 校验申请存在
        TeamApplication application = applicationMapper.selectById(applicationId);
        if (application == null) {
            throw new BusinessException(404, "申请记录不存在");
        }

        // 2. 校验帖子存在且当前用户是作者
        TeamPost post = teamPostMapper.selectById(application.getTeamPostId());
        if (post == null) {
            throw new BusinessException(404, "组队帖不存在");
        }
        if (!post.getAuthorId().equals(authorId)) {
            throw new BusinessException(403, "只有帖子作者才能审核申请");
        }

        // 3. 校验申请状态（只有待审核状态才能审核）
        if (application.getStatus() != STATUS_PENDING) {
            throw new BusinessException(400, "该申请已被处理，无需重复审核");
        }

        // 4. 更新申请状态
        application.setStatus(status);
        application.setUpdatedAt(LocalDateTime.now());
        applicationMapper.updateById(application);

        log.info("[审核] applicationId={} status={} reply={}", applicationId, status, reply);

        // 5. 通过时更新帖子人数
        String notifTitle;
        String notifContent;
        if (status == STATUS_APPROVED) {
            int newCount = (post.getCurrentCount() != null ? post.getCurrentCount() : 0) + 1;
            post.setCurrentCount(newCount);

            // 人数满了 → 帖子改为已招满
            if (post.getNeedCount() != null && newCount >= post.getNeedCount()) {
                post.setStatus(POST_FULL);
                log.info("[审核] postId={} 已招满，currentCount={} needCount={}",
                        post.getId(), newCount, post.getNeedCount());
            }
            teamPostMapper.updateById(post);

            notifTitle = "申请已通过";
            notifContent = "你申请加入「" + (post.getTitle() != null ? post.getTitle() : "组队帖") + "」已通过！" +
                    (reply != null && !reply.isBlank() ? " 队长回复：" + reply : "");
        } else {
            notifTitle = "申请已被拒绝";
            notifContent = "你申请加入「" + (post.getTitle() != null ? post.getTitle() : "组队帖") + "」未通过审核。" +
                    (reply != null && !reply.isBlank() ? " 理由：" + reply : "");
        }

        // 6. 通知申请人
        notificationService.send(
                application.getApplicantId(),
                "application",
                (notifTitle != null ? notifTitle : "申请结果") + "：" + notifContent,
                application.getId()
        );

        return application;
    }

    // ================================================================
    // 取消申请
    // ================================================================
    @Override
    @Transactional
    public void cancel(Long applicationId, Long userId) {
        TeamApplication application = applicationMapper.selectById(applicationId);
        if (application == null) {
            throw new BusinessException(404, "申请记录不存在");
        }

        // 只有申请人自己能取消
        if (!application.getApplicantId().equals(userId)) {
            throw new BusinessException(403, "只能取消自己的申请");
        }

        // 如果之前已通过，需要回退帖子人数
        if (application.getStatus() == STATUS_APPROVED) {
            TeamPost post = teamPostMapper.selectById(application.getTeamPostId());
            if (post != null) {
                int newCount = Math.max(0,
                        (post.getCurrentCount() != null ? post.getCurrentCount() : 1) - 1);
                post.setCurrentCount(newCount);
                // 如果之前是已招满状态，人数减少了就恢复招募中
                if (post.getStatus() != null && post.getStatus() == POST_FULL && newCount < post.getNeedCount()) {
                    post.setStatus(POST_RECRUITING);
                }
                teamPostMapper.updateById(post);
            }
        }

        applicationMapper.deleteById(applicationId);
        log.info("[取消申请] applicationId={} userId={}", applicationId, userId);
    }

    // ================================================================
    // 查看帖子申请列表（仅帖子作者）
    // ================================================================
    @Override
    public Page<TeamApplication> listByPost(Long postId, int page, int size) {
        List<TeamApplication> allApps = applicationMapper.findByPostId(postId);

        // 应用层分页
        int total = allApps.size();
        int start = (page - 1) * size;
        int end = Math.min(start + size, total);

        // 批量填充申请人名称（已在 findByPostId SQL 中 JOIN，无需额外查询）

        Page<TeamApplication> result = new Page<>(page, size);
        result.setTotal(total);
        result.setRecords(start < total ? allApps.subList(start, end) : List.of());
        return result;
    }

    // ================================================================
    // 查看我的申请记录
    // ================================================================
    @Override
    public Page<TeamApplication> listMyApplications(Long userId, int page, int size) {
        LambdaQueryWrapper<TeamApplication> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TeamApplication::getApplicantId, userId)
               .orderByDesc(TeamApplication::getCreatedAt);

        Page<TeamApplication> result = applicationMapper.selectPage(new Page<>(page, size), wrapper);

        // 批量填充帖子标题
        List<Long> postIds = result.getRecords().stream()
                .map(TeamApplication::getTeamPostId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (!postIds.isEmpty()) {
            Map<Long, String> titleMap = teamPostMapper.selectBatchIds(postIds).stream()
                    .collect(Collectors.toMap(TeamPost::getId, TeamPost::getTitle));
            result.getRecords().forEach(a -> {
                String title = titleMap.get(a.getTeamPostId());
                if (title != null) a.setTeamPostTitle(title);
            });
        }

        return result;
    }
}
