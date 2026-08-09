package com.jingdui.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jingdui.config.AiProperties;
import com.jingdui.dto.ChatRequest;
import com.jingdui.entity.Competition;
import com.jingdui.entity.TeamPost;
import com.jingdui.entity.User;
import com.jingdui.mapper.CompetitionMapper;
import com.jingdui.mapper.TeamPostMapper;
import com.jingdui.mapper.UserMapper;
import com.jingdui.security.CurrentUser;
import com.jingdui.service.ChatService;
import com.jingdui.service.SiliconFlowStreamService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * AI 聊天服务实现
 *
 * - mock=true  → 本地关键词匹配（旧逻辑，无需网络）
 * - mock=false → OkHttp 直连硅基流动 DeepSeek 模型
 *
 * 注：流式端点（/api/ai/stream/chat、/api/chat）由 Controller 直接调用
 *     {@link SiliconFlowStreamService#streamToEmitter}，不再经过本类。
 */
@Slf4j
@Service
public class ChatServiceImpl implements ChatService {

    /* ---- Mock 模式依赖 ---- */
    private final CompetitionMapper competitionMapper;
    private final TeamPostMapper teamPostMapper;
    private final UserMapper userMapper;

    /* ---- 真实 AI 依赖 ---- */
    private final SiliconFlowStreamService siliconFlowStreamService;
    private final AiProperties aiProperties;

    public ChatServiceImpl(CompetitionMapper competitionMapper,
                           TeamPostMapper teamPostMapper,
                           UserMapper userMapper,
                           SiliconFlowStreamService siliconFlowStreamService,
                           AiProperties aiProperties) {
        this.competitionMapper = competitionMapper;
        this.teamPostMapper = teamPostMapper;
        this.userMapper = userMapper;
        this.siliconFlowStreamService = siliconFlowStreamService;
        this.aiProperties = aiProperties;
    }

    /**
     * 同步聊天：mock 模式走本地匹配，真实模式走硅基流动
     */
    @Override
    public String chat(ChatRequest request) {
        if (aiProperties.isMock()) {
            return mockChat(request);
        }
        return siliconFlowStreamService.chatSync(request.getMessage());
    }

    /* ================================================================
     * Mock 逻辑（app.ai.mock=true 时使用）
     * ================================================================ */
    private static final Map<String, String> KEYWORD_CATEGORY = new LinkedHashMap<>();
    static {
        KEYWORD_CATEGORY.put("数学", "数学建模");  KEYWORD_CATEGORY.put("建模", "数学建模");
        KEYWORD_CATEGORY.put("算法", "编程算法");  KEYWORD_CATEGORY.put("编程", "编程算法");
        KEYWORD_CATEGORY.put("代码", "编程算法");  KEYWORD_CATEGORY.put("程序", "编程算法");
        KEYWORD_CATEGORY.put("开发", "编程算法");  KEYWORD_CATEGORY.put("创业", "创新创业");
        KEYWORD_CATEGORY.put("创新", "创新创业");  KEYWORD_CATEGORY.put("商业", "创新创业");
        KEYWORD_CATEGORY.put("电子", "电子设计");  KEYWORD_CATEGORY.put("电路", "电子设计");
        KEYWORD_CATEGORY.put("硬件", "电子设计");  KEYWORD_CATEGORY.put("嵌入式", "电子设计");
        KEYWORD_CATEGORY.put("机器人", "机器人");  KEYWORD_CATEGORY.put("机械", "机器人");
        KEYWORD_CATEGORY.put("视觉", "机器人");    KEYWORD_CATEGORY.put("安全", "信息安全");
        KEYWORD_CATEGORY.put("网络", "信息安全");  KEYWORD_CATEGORY.put("CTF", "信息安全");
        KEYWORD_CATEGORY.put("设计", "设计传媒");  KEYWORD_CATEGORY.put("广告", "设计传媒");
        KEYWORD_CATEGORY.put("传媒", "设计传媒");  KEYWORD_CATEGORY.put("英语", "语言文学");
        KEYWORD_CATEGORY.put("演讲", "语言文学");  KEYWORD_CATEGORY.put("写作", "语言文学");
        KEYWORD_CATEGORY.put("智能车", "智能硬件"); KEYWORD_CATEGORY.put("物联网", "智能硬件");
        KEYWORD_CATEGORY.put("计算机", "计算机设计"); KEYWORD_CATEGORY.put("软件", "计算机设计");
        KEYWORD_CATEGORY.put("前端", "计算机设计"); KEYWORD_CATEGORY.put("后端", "计算机设计");
        KEYWORD_CATEGORY.put("Python", "编程算法"); KEYWORD_CATEGORY.put("Java", "编程算法");
        KEYWORD_CATEGORY.put("C++", "编程算法");    KEYWORD_CATEGORY.put("深度学习", "机器人");
        KEYWORD_CATEGORY.put("AI", "机器人");       KEYWORD_CATEGORY.put("人工智能", "机器人");
    }

    public String mockChat(ChatRequest request) {
        String message = request.getMessage();
        if (message == null || message.isBlank()) {
            return "你好！请告诉我你的情况，比如专业、年级、擅长的技能，我来帮你推荐合适的竞赛和队伍。";
        }

        Set<String> matchedCategories = new LinkedHashSet<>();
        String lower = message.toLowerCase();
        for (Map.Entry<String, String> entry : KEYWORD_CATEGORY.entrySet()) {
            if (lower.contains(entry.getKey())) matchedCategories.add(entry.getValue());
        }

        List<Competition> matchedComps;
        if (!matchedCategories.isEmpty()) {
            LambdaQueryWrapper<Competition> wrapper = new LambdaQueryWrapper<>();
            wrapper.in(Competition::getCategory, matchedCategories)
                   .orderByAsc(Competition::getDeadline);
            matchedComps = competitionMapper.selectList(wrapper);
        } else {
            LambdaQueryWrapper<Competition> wrapper = new LambdaQueryWrapper<>();
            wrapper.orderByAsc(Competition::getDeadline).last("LIMIT 6");
            matchedComps = competitionMapper.selectList(wrapper);
        }

        StringBuilder reply = new StringBuilder();
        String userName = getUserName(CurrentUser.getUserId());
        reply.append(userName != null ? "你好，" + userName + "！" : "你好！");

        if (matchedComps.isEmpty()) {
            reply.append("\n\n目前我没有找到与你需求完全匹配的竞赛。请告诉我更多信息：\n");
            reply.append("• 你擅长什么技术？（如编程、数学、电子、设计等）\n");
            reply.append("• 你是什么专业、年级？\n");
            reply.append("• 你想参加什么类型的竞赛？\n\n");
            reply.append("我会根据你的描述为你精准推荐！");
            return reply.toString();
        }

        reply.append("\n\n根据你的描述，我为你推荐以下竞赛：\n");
        int count = Math.min(matchedComps.size(), 5);
        for (int i = 0; i < count; i++) {
            Competition comp = matchedComps.get(i);
            reply.append("\n**").append(i + 1).append(".** ")
                .append(getStatusEmoji(comp.getDeadline()))
                .append(" **").append(comp.getTitle()).append("**\n");
            reply.append("  📂 ").append(comp.getCategory())
                .append(" · ").append(comp.getLevel() != null ? comp.getLevel() : "国家级");
            if (comp.getMaxTeamSize() != null) reply.append(" · ")
                .append(comp.getMinTeamSize() != null ? comp.getMinTeamSize() : 1)
                .append("-").append(comp.getMaxTeamSize()).append("人");
            reply.append("\n  📅 截止：").append(comp.getDeadline())
                .append(" · ").append(getStatusText(comp.getDeadline())).append("\n");
            LambdaQueryWrapper<TeamPost> tpWrapper = new LambdaQueryWrapper<>();
            tpWrapper.eq(TeamPost::getCompetitionId, comp.getId());
            long teamCount = teamPostMapper.selectCount(tpWrapper);
            if (teamCount > 0) reply.append("  👥 ").append(teamCount).append(" 个队伍正在招募队友\n");
        }
        reply.append("\n如果这些推荐不太符合预期，请告诉我更多信息，我会重新匹配！");
        return reply.toString();
    }

    private String getUserName(Long userId) {
        if (userId == null) return null;
        User user = userMapper.selectById(userId);
        return user != null ? user.getName() : null;
    }

    private String getStatusEmoji(LocalDate deadline) {
        if (deadline == null) return "📌";
        long days = ChronoUnit.DAYS.between(LocalDate.now(), deadline);
        if (days < 0) return "🔴";
        if (days <= 7) return "⚠️";
        return "✅";
    }

    private String getStatusText(LocalDate deadline) {
        if (deadline == null) return "未知";
        long days = ChronoUnit.DAYS.between(LocalDate.now(), deadline);
        if (days < 0) return "已截止";
        if (days <= 7) return "即将截止";
        return "报名中";
    }
}
