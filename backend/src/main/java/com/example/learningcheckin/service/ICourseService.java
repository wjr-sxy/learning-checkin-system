package com.example.learningcheckin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.learningcheckin.entity.Course;
import com.example.learningcheckin.entity.User;

import java.util.List;

public interface ICourseService extends IService<Course> {
    Course createCourse(Long teacherId, String name, String description, String semester);
    List<Course> getTeacherCourses(Long teacherId);
    List<Course> getStudentCourses(Long studentId);
    List<User> getCourseStudents(Long courseId);
    List<com.example.learningcheckin.dto.CourseStudentDTO> getCourseStudentDetails(Long courseId);
    void joinCourse(Long studentId, String code);
    void removeStudent(Long courseId, Long studentId);
    void banStudent(Long courseId, Long studentId);
    void unbanStudent(Long courseId, Long studentId);
    boolean checkCourseNameExists(Long teacherId, String name);
    String refreshCourseCode(Long courseId);
}
