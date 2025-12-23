package com.example.learningcheckin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.learningcheckin.entity.Course;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CourseMapper extends BaseMapper<Course> {
    @Select("SELECT c.* FROM sys_course c " +
            "JOIN sys_course_student cs ON c.id = cs.course_id " +
            "WHERE cs.student_id = #{studentId}")
    List<Course> selectCoursesByStudentId(Long studentId);
}
