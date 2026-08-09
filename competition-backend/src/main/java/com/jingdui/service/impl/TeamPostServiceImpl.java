package com.jingdui.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jingdui.common.BusinessException;
import com.jingdui.dto.TeamPostRequest;
import com.jingdui.entity.Competition;
import com.jingdui.entity.Favorite;
import com.jingdui.entity.TeamPost;
import com.jingdui.entity.User;
import com.jingdui.mapper.CompetitionMapper;
import com.jingdui.mapper.FavoriteMapper;
import com.jingdui.mapper.TeamPostMapper;
import com.jingdui.mapper.UserMapper;
import com.jingdui.service.TeamPostService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TeamPostServiceImpl implements TeamPostService {

    private final TeamPostMapper teamPostMapper;
    private final CompetitionMapper competitionMapper;
    private final UserMapper userMapper;
    private final FavoriteMapper favoriteMapper;

    @Override
    public Page<TeamPost> listTeamPosts(int page, int size, String category) {
        LambdaQueryWrapper<TeamPost> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(category)) {
            // 分类筛选通过子查询实现（这里用简单方式：先查出符合条件的competitionIds）
            LambdaQueryWrapper<Competition> compWrapper = new LambdaQueryWrapper<>();
            compWrapper.select(Competition::getId).eq(Competition::getCategory, category);
            List<Long> compIds = competitionMapper.selectList(compWrapper)
                    .stream().map(Competition::getId).toList();

            if (compIds.isEmpty()) {
                return new Page<>(page, size);
            }
            wrapper.in(TeamPost::getCompetitionId, compIds);
        }

        wrapper.orderByDesc(TeamPost::getCreatedAt);

        Page<TeamPost> result = teamPostMapper.selectPage(new Page<>(page, size), wrapper);

        // 填充联表字段
        result.getRecords().forEach(this::enrichPost);

        return result;
    }

    @Override
    public TeamPost getById(Long id) {
        TeamPost post = teamPostMapper.selectById(id);
        if (post == null) {
            throw new BusinessException(404, "组队帖不存在");
        }
        enrichPost(post);
        return post;
    }

    @Override
    @Transactional
    public TeamPost create(TeamPostRequest request) {
        // 校验竞赛存在
        Competition comp = competitionMapper.selectById(request.getCompetitionId());
        if (comp == null) {
            throw new BusinessException(400, "所选竞赛不存在");
        }

        // 校验用户存在
        User user = userMapper.selectById(request.getAuthorId());
        if (user == null) {
            throw new BusinessException(400, "用户不存在");
        }

        TeamPost post = new TeamPost();
        post.setCompetitionId(request.getCompetitionId());
        post.setAuthorId(request.getAuthorId());
        post.setTitle(StringUtils.hasText(request.getTitle())
                ? request.getTitle()
                : "寻找队友参加" + comp.getTitle());
        post.setDescription(request.getDescription());
        post.setRequiredSkills(request.getRequiredSkills());
        post.setContact(request.getContact());
        post.setDeadline(request.getDeadline());
        post.setNeedCount(request.getNeedCount() != null ? request.getNeedCount() : 1);
        post.setCurrentCount(1);
        post.setStatus(0);
        post.setCreatedAt(java.time.LocalDateTime.now());

        teamPostMapper.insert(post);
        return post;
    }

    @Override
    @Transactional
    public void delete(Long postId, Long userId) {
        TeamPost post = teamPostMapper.selectById(postId);
        if (post == null) {
            throw new BusinessException(404, "组队帖不存在");
        }
        if (!post.getAuthorId().equals(userId)) {
            throw new BusinessException(403, "只能删除自己发布的组队帖");
        }
        teamPostMapper.deleteById(postId);
    }

    @Override
    @Transactional
    public boolean toggleFavorite(Long postId, Long userId) {
        if (teamPostMapper.selectById(postId) == null) {
            throw new BusinessException(404, "组队帖不存在");
        }

        Favorite existing = favoriteMapper.findByUserAndItem(userId, postId, "team");
        if (existing != null) {
            favoriteMapper.deleteById(existing.getId());
            return false;
        } else {
            Favorite fav = new Favorite();
            fav.setUserId(userId);
            fav.setItemId(postId);
            fav.setItemType("team");
            favoriteMapper.insert(fav);
            return true;
        }
    }

    @Override
    public Page<TeamPost> listFavorites(Long userId, int page, int size) {
        List<TeamPost> allFavs = teamPostMapper.findFavoritesByUserId(userId);
        allFavs.forEach(this::enrichPost);

        int total = allFavs.size();
        int start = (page - 1) * size;
        int end = Math.min(start + size, total);

        Page<TeamPost> result = new Page<>(page, size);
        result.setTotal(total);
        result.setRecords(start < total ? allFavs.subList(start, end) : List.of());
        return result;
    }

    /* ---- 工具方法 ---- */

    /**
     * 填充联表字段（作者名、竞赛名）并计算是否过期
     */
    private void enrichPost(TeamPost post) {
        // 竞赛信息
        if (post.getCompetitionId() != null) {
            Competition comp = competitionMapper.selectById(post.getCompetitionId());
            if (comp != null) {
                post.setCompetitionTitle(comp.getTitle());
                post.setCompetitionCategory(comp.getCategory());
            }
        }

        // 作者信息
        if (post.getAuthorId() != null) {
            User user = userMapper.selectById(post.getAuthorId());
            if (user != null) {
                post.setAuthorName(user.getName());
            }
        }

        // 是否过期
        if (post.getDeadline() != null) {
            post.setExpired(post.getDeadline().isBefore(LocalDate.now()));
        } else {
            post.setExpired(false);
        }
    }
}
