package com.jingdui.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jingdui.entity.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CommentMapper extends BaseMapper<Comment> {

    /**
     * 查询某帖子的顶级评论列表（含用户名称、头像，按时间排序）
     */
    @Select("SELECT c.*, u.name AS user_name, u.avatar AS user_avatar " +
            "FROM comments c " +
            "LEFT JOIN users u ON u.id = c.user_id " +
            "WHERE c.team_post_id = #{postId} AND c.parent_id IS NULL " +
            "ORDER BY c.created_at ASC")
    List<Comment> findTopLevelByPostId(@Param("postId") Long postId);

    /**
     * 查询某条评论的所有子回复（按时间升序）
     */
    @Select("SELECT c.*, u.name AS user_name, u.avatar AS user_avatar " +
            "FROM comments c " +
            "LEFT JOIN users u ON u.id = c.user_id " +
            "WHERE c.parent_id = #{parentId} " +
            "ORDER BY c.created_at ASC")
    List<Comment> findRepliesByParentId(@Param("parentId") Long parentId);
}
