package com.jingdui.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jingdui.entity.Favorite;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface FavoriteMapper extends BaseMapper<Favorite> {

    /**
     * 查找用户是否收藏了某个对象
     */
    @Select("SELECT * FROM favorites WHERE user_id = #{userId} AND item_id = #{itemId} AND item_type = #{itemType}")
    Favorite findByUserAndItem(@Param("userId") Long userId,
                               @Param("itemId") Long itemId,
                               @Param("itemType") String itemType);
}
