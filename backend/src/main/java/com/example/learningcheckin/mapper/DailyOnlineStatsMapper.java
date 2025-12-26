package com.example.learningcheckin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.learningcheckin.entity.DailyOnlineStats;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Mapper
public interface DailyOnlineStatsMapper extends BaseMapper<DailyOnlineStats> {

    @Update("INSERT INTO sys_daily_online_stats (user_id, stats_date, online_seconds, last_active_time) " +
            "VALUES (#{userId}, #{date}, #{seconds}, #{now}) " +
            "ON DUPLICATE KEY UPDATE online_seconds = online_seconds + #{seconds}, last_active_time = #{now}")
    void incrementDuration(@Param("userId") Long userId, @Param("date") LocalDate date, @Param("seconds") Long seconds, @Param("now") java.time.LocalDateTime now);

    @Select("SELECT u.id, u.username, u.full_name, u.avatar, u.college, SUM(s.online_seconds) as total_seconds " +
            "FROM sys_user u " +
            "JOIN sys_daily_online_stats s ON u.id = s.user_id " +
            "WHERE u.role = 'TEACHER' AND s.stats_date >= #{startDate} " +
            "GROUP BY u.id " +
            "ORDER BY total_seconds DESC " +
            "LIMIT 100")
    List<Map<String, Object>> getTeacherStatsByDateRange(@Param("startDate") LocalDate startDate);
}
