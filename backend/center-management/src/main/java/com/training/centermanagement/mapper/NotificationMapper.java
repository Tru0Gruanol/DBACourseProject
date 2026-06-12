package com.training.centermanagement.mapper;

import com.training.centermanagement.entity.Notification;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface NotificationMapper {

    @Select("SELECT * FROM notifications WHERE user_id=#{userId} AND user_role=#{role} ORDER BY created_at DESC")
    List<Notification> getByUser(@Param("userId") Integer userId, @Param("role") String role);

    @Select("SELECT COUNT(*) FROM notifications WHERE user_id=#{userId} AND user_role=#{role} AND is_read=0")
    int countUnread(@Param("userId") Integer userId, @Param("role") String role);

    @Update("UPDATE notifications SET is_read=1 WHERE id=#{id}")
    int markRead(Integer id);

    @Update("UPDATE notifications SET is_read=1 WHERE user_id=#{userId} AND user_role=#{role}")
    int markAllRead(@Param("userId") Integer userId, @Param("role") String role);

    @Insert("INSERT INTO notifications(user_id,user_role,title,content) VALUES(#{userId},#{userRole},#{title},#{content})")
    int insert(Notification n);
}
