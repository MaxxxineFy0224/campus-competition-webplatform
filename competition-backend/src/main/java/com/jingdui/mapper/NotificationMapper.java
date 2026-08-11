package com.jingdui.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jingdui.entity.Notification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface NotificationMapper extends BaseMapper<Notification> {

    /**
     * 查询用户的未读通知（按时间倒序）
     */
    @Select("SELECT * FROM notifications " +
            "WHERE user_id = #{userId} AND is_read = 0 " +
            "ORDER BY created_at DESC")
    List<Notification> findUnreadByUserId(@Param("userId") Long userId);

    /**
     * 查询用户的所有通知（按时间倒序）
     */
    @Select("SELECT * FROM notifications " +
            "WHERE user_id = #{userId} " +
            "ORDER BY created_at DESC")
    List<Notification> findByUserId(@Param("userId") Long userId);

    /**
     * 标记通知为已读
     */
    @Update("UPDATE notifications SET is_read = 1 WHERE id = #{id}")
    int markAsRead(@Param("id") Long id);

    /**
     * 标记用户所有通知为已读
     */
    @Update("UPDATE notifications SET is_read = 1 WHERE user_id = #{userId} AND is_read = 0")
    int markAllAsRead(@Param("userId") Long userId);
}
