package com.jingdui.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jingdui.common.BusinessException;
import com.jingdui.dto.UserUpdateRequest;
import com.jingdui.entity.Competition;
import com.jingdui.entity.TeamPost;
import com.jingdui.entity.User;
import com.jingdui.mapper.CompetitionMapper;
import com.jingdui.mapper.TeamPostMapper;
import com.jingdui.mapper.UserMapper;
import com.jingdui.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final TeamPostMapper teamPostMapper;
    private final CompetitionMapper competitionMapper;

    @Override
    public User getById(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        return user;
    }

    @Override
    public User update(Long id, UserUpdateRequest request) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }

        // 检查昵称是否被其他用户占用
        if (StringUtils.hasText(request.getName())) {
            LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(User::getName, request.getName())
                   .ne(User::getId, id);
            if (userMapper.selectCount(wrapper) > 0) {
                throw new BusinessException(400, "昵称已被占用");
            }
            user.setName(request.getName());
        }

        // 仅更新非空字段，避免覆盖已有数据
        if (request.getAvatar() != null) user.setAvatar(request.getAvatar());
        if (request.getSchool() != null) user.setSchool(request.getSchool());
        if (request.getMajor() != null) user.setMajor(request.getMajor());
        if (request.getGrade() != null) user.setGrade(request.getGrade());
        if (request.getBio() != null) user.setBio(request.getBio());
        if (request.getSkills() != null) user.setSkills(request.getSkills());

        userMapper.updateById(user);
        return user;
    }

    @Override
    public List<TeamPost> getUserPosts(Long userId) {
        if (userMapper.selectById(userId) == null) {
            throw new BusinessException(404, "用户不存在");
        }

        LambdaQueryWrapper<TeamPost> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TeamPost::getAuthorId, userId)
               .orderByDesc(TeamPost::getCreatedAt);

        List<TeamPost> posts = teamPostMapper.selectList(wrapper);

        // 填充联表字段
        posts.forEach(post -> {
            if (post.getCompetitionId() != null) {
                Competition comp = competitionMapper.selectById(post.getCompetitionId());
                if (comp != null) {
                    post.setCompetitionTitle(comp.getTitle());
                }
            }
            User author = userMapper.selectById(userId);
            if (author != null) {
                post.setAuthorName(author.getName());
            }
        });

        return posts;
    }

    @Override
    public List<?> getUserFavorites(Long userId, String type) {
        if (userMapper.selectById(userId) == null) {
            throw new BusinessException(404, "用户不存在");
        }

        if ("competition".equals(type)) {
            return competitionMapper.findFavoritesByUserId(userId);
        } else if ("team".equals(type)) {
            List<TeamPost> posts = teamPostMapper.findFavoritesByUserId(userId);
            posts.forEach(post -> {
                if (post.getCompetitionId() != null) {
                    Competition comp = competitionMapper.selectById(post.getCompetitionId());
                    if (comp != null) {
                        post.setCompetitionTitle(comp.getTitle());
                    }
                }
                if (post.getAuthorId() != null) {
                    User author = userMapper.selectById(post.getAuthorId());
                    if (author != null) {
                        post.setAuthorName(author.getName());
                    }
                }
            });
            return posts;
        }

        throw new BusinessException(400, "不支持的收藏类型: " + type);
    }
}
