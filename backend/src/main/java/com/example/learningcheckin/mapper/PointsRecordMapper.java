package com.example.learningcheckin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.learningcheckin.dto.RankingDTO;
import com.example.learningcheckin.entity.PointsRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface PointsRecordMapper extends BaseMapper<PointsRecord> {

    @Select("SELECT u.id as userId, u.username, u.avatar, SUM(pr.amount) as score " +
            "FROM sys_points_record pr " +
            "JOIN sys_user u ON pr.user_id = u.id " +
            "WHERE pr.type = 1 AND pr.create_time >= #{startTime} " +
            "GROUP BY u.id, u.username, u.avatar " +
            "ORDER BY score DESC " +
            "LIMIT #{limit}")
    List<RankingDTO> selectRanking(@Param("startTime") LocalDateTime startTime, @Param("limit") int limit);
}
