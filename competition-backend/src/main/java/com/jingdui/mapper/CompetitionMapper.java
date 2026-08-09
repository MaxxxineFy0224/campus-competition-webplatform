package com.jingdui.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jingdui.entity.Competition;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CompetitionMapper extends BaseMapper<Competition> {

    /**
     * 根据用户 ID 查询收藏的竞赛列表
     */
    @Select("SELECT c.* FROM competitions c " +
            "INNER JOIN favorites f ON f.item_id = c.id AND f.item_type = 'competition' " +
            "WHERE f.user_id = #{userId} " +
            "ORDER BY f.created_at DESC")
    List<Competition> findFavoritesByUserId(@Param("userId") Long userId);
}
