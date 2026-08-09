package com.jingdui.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jingdui.entity.TeamPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface TeamPostMapper extends BaseMapper<TeamPost> {

    /**
     * 查询某竞赛下的所有组队帖（含作者名、竞赛名）
     */
    @Select("SELECT tp.*, u.name AS author_name, c.title AS competition_title, c.category AS competition_category " +
            "FROM team_posts tp " +
            "LEFT JOIN users u ON u.id = tp.author_id " +
            "LEFT JOIN competitions c ON c.id = tp.competition_id " +
            "WHERE tp.competition_id = #{competitionId} " +
            "ORDER BY tp.created_at DESC")
    List<TeamPost> findByCompetitionId(@Param("competitionId") Long competitionId);

    /**
     * 根据用户 ID 查询收藏的组队帖
     */
    @Select("SELECT tp.*, u.name AS author_name, c.title AS competition_title, c.category AS competition_category " +
            "FROM team_posts tp " +
            "INNER JOIN favorites f ON f.item_id = tp.id AND f.item_type = 'team' " +
            "LEFT JOIN users u ON u.id = tp.author_id " +
            "LEFT JOIN competitions c ON c.id = tp.competition_id " +
            "WHERE f.user_id = #{userId} " +
            "ORDER BY f.created_at DESC")
    List<TeamPost> findFavoritesByUserId(@Param("userId") Long userId);
}
