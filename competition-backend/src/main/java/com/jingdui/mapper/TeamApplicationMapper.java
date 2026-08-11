package com.jingdui.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jingdui.entity.TeamApplication;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface TeamApplicationMapper extends BaseMapper<TeamApplication> {

    /**
     * 查询某个组队帖的所有申请（含申请人名称、帖子标题）
     */
    @Select("SELECT ta.*, u.name AS applicant_name, tp.title AS team_post_title " +
            "FROM team_applications ta " +
            "LEFT JOIN users u ON u.id = ta.applicant_id " +
            "LEFT JOIN team_posts tp ON tp.id = ta.team_post_id " +
            "WHERE ta.team_post_id = #{postId} " +
            "ORDER BY ta.created_at DESC")
    List<TeamApplication> findByPostId(@Param("postId") Long postId);

    /**
     * 查询用户对某帖子的待审核申请（status=0）
     */
    @Select("SELECT * FROM team_applications " +
            "WHERE applicant_id = #{userId} " +
            "AND team_post_id = #{postId} " +
            "AND status = 0 " +
            "LIMIT 1")
    TeamApplication findPendingByUserAndPost(@Param("userId") Long userId,
                                              @Param("postId") Long postId);
}
