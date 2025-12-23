package com.example.learningcheckin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.learningcheckin.entity.Order;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderMapper extends BaseMapper<Order> {
}
