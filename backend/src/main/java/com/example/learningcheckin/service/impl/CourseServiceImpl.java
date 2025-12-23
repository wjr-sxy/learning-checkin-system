package com.example.learningcheckin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.learningcheckin.entity.Course;
import com.example.learningcheckin.entity.CourseStudent;
import com.example.learningcheckin.entity.User;
import com.example.learningcheckin.mapper.CourseMapper;
import com.example.learningcheckin.mapper.CourseStudentMapper;
import com.example.learningcheckin.mapper.UserMapper;
import com.example.learningcheckin.service.ICourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CourseServiceImpl extends ServiceImpl<CourseMapper, Course> implements ICourseService {

    @Autowired
    private CourseStudentMapper courseStudentMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private com.example.learningcheckin.service.ISensitiveWordService sensitiveWordService;

    @Override
    public Course createCourse(Long teacherId, String name, String description, String semester) {
        // Sensitive word check
        if (sensitiveWordService.containsSensitive(name) || sensitiveWordService.containsSensitive(description)) {
            throw new RuntimeException("Course name or description contains sensitive words.");
        }

        Course course = new Course();
        course.setTeacherId(teacherId);
        course.setName(name);
        course.setDescription(description);
        course.setSemester(semester);
        course.setCode(UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        course.setCodeExpireTime(LocalDateTime.now().plusDays(7)); // Valid for 7 days
        course.setCreateTime(LocalDateTime.now());
        this.save(course);
        return course;
    }

    @Override
    public List<Course> getTeacherCourses(Long teacherId) {
        return this.list(new LambdaQueryWrapper<Course>().eq(Course::getTeacherId, teacherId));
    }

    @Override
    public List<Course> getStudentCourses(Long studentId) {
        return baseMapper.selectCoursesByStudentId(studentId);
    }

    @Override
    public List<User> getCourseStudents(Long courseId) {
        List<CourseStudent> relations = courseStudentMapper.selectList(
                new LambdaQueryWrapper<CourseStudent>().eq(CourseStudent::getCourseId, courseId)
        );
        if (relations.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> studentIds = relations.stream().map(CourseStudent::getStudentId).collect(Collectors.toList());
        List<User> users = userMapper.selectBatchIds(studentIds);
        // clean password
        users.forEach(u -> u.setPassword(null));
        return users;
    }

    @Override
    public List<com.example.learningcheckin.dto.CourseStudentDTO> getCourseStudentDetails(Long courseId) {
        List<CourseStudent> relations = courseStudentMapper.selectList(
                new LambdaQueryWrapper<CourseStudent>().eq(CourseStudent::getCourseId, courseId)
        );
        if (relations.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> studentIds = relations.stream().map(CourseStudent::getStudentId).collect(Collectors.toList());
        Map<Long, User> userMap = userMapper.selectBatchIds(studentIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));
        
        return relations.stream().map(r -> {
            User u = userMap.get(r.getStudentId());
            if (u == null) return null;
            com.example.learningcheckin.dto.CourseStudentDTO dto = new com.example.learningcheckin.dto.CourseStudentDTO();
            dto.setId(u.getId());
            dto.setUsername(u.getUsername());
            dto.setEmail(u.getEmail());
            dto.setPoints(u.getPoints());
            dto.setStatus(r.getStatus());
            dto.setJoinTime(r.getJoinTime());
            return dto;
        }).filter(java.util.Objects::nonNull).collect(Collectors.toList());
    }

    @Override
    public void removeStudent(Long courseId, Long studentId) {
        courseStudentMapper.delete(new LambdaQueryWrapper<CourseStudent>()
                .eq(CourseStudent::getCourseId, courseId)
                .eq(CourseStudent::getStudentId, studentId));
    }

    @Override
    public void banStudent(Long courseId, Long studentId) {
        CourseStudent cs = courseStudentMapper.selectOne(new LambdaQueryWrapper<CourseStudent>()
                .eq(CourseStudent::getCourseId, courseId)
                .eq(CourseStudent::getStudentId, studentId));
        if (cs != null) {
            cs.setStatus(1);
            courseStudentMapper.updateById(cs);
        }
    }

    @Override
    public void unbanStudent(Long courseId, Long studentId) {
        CourseStudent cs = courseStudentMapper.selectOne(new LambdaQueryWrapper<CourseStudent>()
                .eq(CourseStudent::getCourseId, courseId)
                .eq(CourseStudent::getStudentId, studentId));
        if (cs != null) {
            cs.setStatus(0);
            courseStudentMapper.updateById(cs);
        }
    }

    @Override
    public boolean checkCourseNameExists(Long teacherId, String name) {
        return this.count(new LambdaQueryWrapper<Course>()
                .eq(Course::getTeacherId, teacherId)
                .eq(Course::getName, name)) > 0;
    }

    @Override
    public String refreshCourseCode(Long courseId) {
        Course course = this.getById(courseId);
        if (course != null) {
            String newCode = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            course.setCode(newCode);
            course.setCodeExpireTime(LocalDateTime.now().plusDays(7)); // Reset to 7 days
            this.updateById(course);
            return newCode;
        }
        return null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void joinCourse(Long studentId, String code) {
        Course course = this.getOne(new LambdaQueryWrapper<Course>().eq(Course::getCode, code));
        if (course == null) {
            throw new RuntimeException("Invalid course code");
        }
        
        // Check Expiration
        if (course.getCodeExpireTime() != null && LocalDateTime.now().isAfter(course.getCodeExpireTime())) {
            throw new RuntimeException("Invitation code has expired");
        }
        
        // Check if already joined
        CourseStudent existing = courseStudentMapper.selectOne(new LambdaQueryWrapper<CourseStudent>()
                .eq(CourseStudent::getCourseId, course.getId())
                .eq(CourseStudent::getStudentId, studentId));
        if (existing != null) {
             if (existing.getStatus() != null && existing.getStatus() == 1) {
                 throw new RuntimeException("You are banned from this course");
            }
            throw new RuntimeException("Already joined this course");
        }

        CourseStudent cs = new CourseStudent();
        cs.setCourseId(course.getId());
        cs.setStudentId(studentId);
        cs.setStatus(0);
        cs.setJoinTime(LocalDateTime.now());
        cs.setCreateTime(LocalDateTime.now());
        courseStudentMapper.insert(cs);
    }
}
