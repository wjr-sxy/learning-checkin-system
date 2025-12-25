package com.example.learningcheckin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.learningcheckin.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    @Select("SELECT SUM(total_online_seconds) FROM sys_user")
    Long sumTotalOnlineTime();

    @Update("UPDATE sys_user SET points = points - #{amount} WHERE id = #{userId} AND points >= #{amount}")
    int deductPoints(@Param("userId") Long userId, @Param("amount") Integer amount);
}
