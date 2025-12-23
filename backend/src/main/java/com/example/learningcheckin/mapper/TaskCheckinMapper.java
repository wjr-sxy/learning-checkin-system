package com.example.learningcheckin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.learningcheckin.entity.TaskCheckin;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TaskCheckinMapper extends BaseMapper<TaskCheckin> {
}
