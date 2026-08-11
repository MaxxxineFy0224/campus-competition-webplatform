package com.jingdui.service.impl;

import com.jingdui.common.BusinessException;
import com.jingdui.dto.TeamPostRequest;
import com.jingdui.entity.Competition;
import com.jingdui.entity.TeamPost;
import com.jingdui.entity.User;
import com.jingdui.mapper.CompetitionMapper;
import com.jingdui.mapper.FavoriteMapper;
import com.jingdui.mapper.TeamPostMapper;
import com.jingdui.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * TeamPostServiceImpl 单元测试 —— 组队帖创建 & 删除
 */
@ExtendWith(MockitoExtension.class)
class TeamPostServiceImplTest {

    @Mock
    private TeamPostMapper teamPostMapper;

    @Mock
    private CompetitionMapper competitionMapper;

    @Mock
    private UserMapper userMapper;

    @Mock
    private FavoriteMapper favoriteMapper;

    @InjectMocks
    private TeamPostServiceImpl teamPostService;

    private TeamPostRequest validRequest;
    private Competition existingComp;
    private User existingUser;

    @BeforeEach
    void setUp() {
        existingComp = new Competition();
        existingComp.setId(1L);
        existingComp.setTitle("2026 全国大学生数学建模竞赛");

        existingUser = new User();
        existingUser.setId(10L);
        existingUser.setName("测试用户");

        validRequest = new TeamPostRequest();
        validRequest.setCompetitionId(1L);
        validRequest.setAuthorId(10L);
        validRequest.setTitle("求2名数学建模队友，冲击国奖！");
        validRequest.setDescription("我们是一支已有1人的队伍，希望招募2名队友");
        validRequest.setRequiredSkills("Python,MATLAB,数据分析");
        validRequest.setContact("QQ: 123456");
        validRequest.setDeadline(LocalDate.now().plusDays(30));
        validRequest.setNeedCount(2);
    }

    /* ================================================================
     * 创建组队帖
     * ================================================================ */

    @Test
    @DisplayName("创建组队帖成功")
    void create_shouldSucceed() {
        when(competitionMapper.selectById(1L)).thenReturn(existingComp);
        when(userMapper.selectById(10L)).thenReturn(existingUser);
        when(teamPostMapper.insert(any(TeamPost.class))).thenAnswer(inv -> {
            TeamPost post = inv.getArgument(0);
            post.setId(100L);
            return 1;
        });

        TeamPost result = teamPostService.create(validRequest);

        assertNotNull(result);
        assertEquals(100L, result.getId());
        assertEquals(1L, result.getCompetitionId());
        assertEquals(10L, result.getAuthorId());
        assertEquals("求2名数学建模队友，冲击国奖！", result.getTitle());
        assertEquals(2, result.getNeedCount());
        assertEquals(1, result.getCurrentCount());
        assertEquals(0, result.getStatus());
        assertNotNull(result.getCreatedAt());

        verify(competitionMapper).selectById(1L);
        verify(userMapper).selectById(10L);
        verify(teamPostMapper).insert(any(TeamPost.class));
    }

    @Test
    @DisplayName("创建组队帖 —— 标题为空时自动生成标题")
    void create_shouldAutoGenerateTitle_whenTitleEmpty() {
        validRequest.setTitle(null);

        when(competitionMapper.selectById(1L)).thenReturn(existingComp);
        when(userMapper.selectById(10L)).thenReturn(existingUser);
        when(teamPostMapper.insert(any(TeamPost.class))).thenAnswer(inv -> {
            TeamPost post = inv.getArgument(0);
            post.setId(101L);
            return 1;
        });

        TeamPost result = teamPostService.create(validRequest);

        assertEquals("寻找队友参加2026 全国大学生数学建模竞赛", result.getTitle());
    }

    @Test
    @DisplayName("创建组队帖失败 —— 竞赛不存在")
    void create_shouldFail_whenCompetitionNotFound() {
        when(competitionMapper.selectById(1L)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> teamPostService.create(validRequest));

        assertEquals(400, ex.getCode());
        assertEquals("所选竞赛不存在", ex.getMessage());

        verify(competitionMapper).selectById(1L);
        verify(userMapper, never()).selectById(any());
        verify(teamPostMapper, never()).insert(any(TeamPost.class));
    }

    @Test
    @DisplayName("创建组队帖失败 —— 用户不存在")
    void create_shouldFail_whenUserNotFound() {
        when(competitionMapper.selectById(1L)).thenReturn(existingComp);
        when(userMapper.selectById(10L)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> teamPostService.create(validRequest));

        assertEquals(400, ex.getCode());
        assertEquals("用户不存在", ex.getMessage());

        verify(competitionMapper).selectById(1L);
        verify(userMapper).selectById(10L);
        verify(teamPostMapper, never()).insert(any(TeamPost.class));
    }

    @Test
    @DisplayName("创建组队帖 —— needCount 为 null 时默认 1")
    void create_shouldDefaultNeedCount_whenNull() {
        validRequest.setNeedCount(null);

        when(competitionMapper.selectById(1L)).thenReturn(existingComp);
        when(userMapper.selectById(10L)).thenReturn(existingUser);
        when(teamPostMapper.insert(any(TeamPost.class))).thenAnswer(inv -> {
            TeamPost post = inv.getArgument(0);
            post.setId(102L);
            return 1;
        });

        TeamPost result = teamPostService.create(validRequest);

        assertEquals(1, result.getNeedCount());
    }

    /* ================================================================
     * 删除组队帖
     * ================================================================ */

    @Test
    @DisplayName("删除组队帖成功 —— 作者本人删除")
    void delete_shouldSucceed_whenAuthorDeletes() {
        TeamPost post = new TeamPost();
        post.setId(100L);
        post.setAuthorId(10L);

        when(teamPostMapper.selectById(100L)).thenReturn(post);
        when(teamPostMapper.deleteById(100L)).thenReturn(1);

        assertDoesNotThrow(() -> teamPostService.delete(100L, 10L));

        verify(teamPostMapper).selectById(100L);
        verify(teamPostMapper).deleteById(100L);
    }

    @Test
    @DisplayName("删除组队帖失败 —— 帖子不存在")
    void delete_shouldFail_whenPostNotFound() {
        when(teamPostMapper.selectById(999L)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> teamPostService.delete(999L, 10L));

        assertEquals(404, ex.getCode());
        assertEquals("组队帖不存在", ex.getMessage());

        verify(teamPostMapper, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("删除组队帖失败 —— 非作者无权删除")
    void delete_shouldFail_whenNotAuthor() {
        TeamPost post = new TeamPost();
        post.setId(100L);
        post.setAuthorId(10L);

        when(teamPostMapper.selectById(100L)).thenReturn(post);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> teamPostService.delete(100L, 20L));

        assertEquals(403, ex.getCode());
        assertEquals("只能删除自己发布的组队帖", ex.getMessage());

        verify(teamPostMapper, never()).deleteById(anyLong());
    }
}