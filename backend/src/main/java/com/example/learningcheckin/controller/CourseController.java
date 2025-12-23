package com.example.learningcheckin.controller;

import com.example.learningcheckin.common.Result;
import com.example.learningcheckin.entity.Course;
import com.example.learningcheckin.entity.User;
import com.example.learningcheckin.service.ICourseService;
import com.example.learningcheckin.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/course")
public class CourseController {

    @Autowired
    private ICourseService courseService;
    
    @Autowired
    private com.example.learningcheckin.mapper.UserMapper userMapper;

    @Autowired
    private EmailService emailService;

    @PostMapping("/create")
    public Result<Course> createCourse(@RequestBody Map<String, Object> params) {
        // In a real app, get current user from context. Here assuming passed or handled by filter.
        // We will pass userId from frontend for simplicity or extracted from token in a real scenario.
        // For now let's assume the frontend passes `teacherId`.
        Long teacherId = Long.valueOf(params.get("teacherId").toString());
        String name = (String) params.get("name");
        String description = (String) params.get("description");
        String semester = (String) params.get("semester");
        
        Course course = courseService.createCourse(teacherId, name, description, semester);
        return Result.success(course);
    }

    @GetMapping("/teacher/{teacherId}")
    public Result<List<Course>> getTeacherCourses(@PathVariable Long teacherId) {
        return Result.success(courseService.getTeacherCourses(teacherId));
    }

    @GetMapping("/student/{studentId}")
    public Result<List<Course>> getStudentCourses(@PathVariable Long studentId) {
        return Result.success(courseService.getStudentCourses(studentId));
    }

    @GetMapping("/{courseId}/students")
    public Result<List<User>> getCourseStudents(@PathVariable Long courseId) {
        return Result.success(courseService.getCourseStudents(courseId));
    }
    
    @GetMapping("/{courseId}/students/details")
    public Result<List<com.example.learningcheckin.dto.CourseStudentDTO>> getCourseStudentDetails(@PathVariable Long courseId) {
        return Result.success(courseService.getCourseStudentDetails(courseId));
    }

    @PostMapping("/{courseId}/remove")
    public Result<String> removeStudent(@PathVariable Long courseId, @RequestBody Map<String, Long> params) {
        Long studentId = params.get("studentId");
        courseService.removeStudent(courseId, studentId);
        return Result.success("Student removed");
    }

    @PostMapping("/{courseId}/ban")
    public Result<String> banStudent(@PathVariable Long courseId, @RequestBody Map<String, Long> params) {
        Long studentId = params.get("studentId");
        courseService.banStudent(courseId, studentId);
        return Result.success("Student banned");
    }

    @PostMapping("/{courseId}/unban")
    public Result<String> unbanStudent(@PathVariable Long courseId, @RequestBody Map<String, Long> params) {
        Long studentId = params.get("studentId");
        courseService.unbanStudent(courseId, studentId);
        return Result.success("Student unbanned");
    }

    @GetMapping("/check-name")
    public Result<Boolean> checkName(@RequestParam Long teacherId, @RequestParam String name) {
        return Result.success(courseService.checkCourseNameExists(teacherId, name));
    }

    @PostMapping("/{courseId}/refresh-code")
    public Result<String> refreshCode(@PathVariable Long courseId) {
        String newCode = courseService.refreshCourseCode(courseId);
        return Result.success(newCode);
    }

    @Autowired
    private com.example.learningcheckin.service.IMessageService messageService;

    @Autowired
    private com.example.learningcheckin.service.INotificationService notificationService;

    @PostMapping("/{courseId}/remind")
    public Result<String> remindStudents(@PathVariable Long courseId) {
        // 1. Find students who haven't checked in for 3 days
        List<User> lazyStudents = messageService.getStudentsNeedingReminder(courseId, 3);
        
        if (lazyStudents == null || lazyStudents.isEmpty()) {
            return Result.success("No students need reminding.");
        }

        int successCount = 0;
        String subject = "Learning Check-in Reminder";
        String content = "You haven't checked in for 3 days. Please complete your tasks.";

        for (User student : lazyStudents) {
            try {
                // 1. Send Station Message
                messageService.sendMessage(0L, student.getId(), subject, content, 2);
                
                // 2. Send Notification (System & WebSocket)
                notificationService.sendNotification(student.getId(), "学习打卡提醒", "您已有3天未打卡，请及时完成学习任务。", "ALERT");
                com.example.learningcheckin.websocket.NotificationWebSocket.sendInfo(student.getId(), 
                    "{\"type\":\"ALERT\", \"title\":\"学习打卡提醒\", \"content\":\"您已有3天未打卡，请及时完成学习任务。\"}");

                // 3. Send Email
                if (student.getEmail() != null && !student.getEmail().isEmpty()) {
                    emailService.sendSimpleMail(student.getEmail(), subject, content);
                }
                successCount++;
            } catch (Exception e) {
                // Log error but continue
                e.printStackTrace();
            }
        }
        
        return Result.success("Successfully reminded " + successCount + " students.");
    }

    @PostMapping("/{courseId}/remind/check")
    public Result<List<User>> checkStudentsToRemind(@PathVariable Long courseId) {
        // Default check for 3 days
        return Result.success(messageService.getStudentsNeedingReminder(courseId, 3));
    }

    @PostMapping("/{courseId}/remind/send")
    @SuppressWarnings("unchecked")
    public Result<String> sendReminders(@PathVariable Long courseId, @RequestBody Map<String, Object> body) {
        String subject = (String) body.get("subject");
        String content = (String) body.get("content");
        List<Integer> studentIdsInt = (List<Integer>) body.get("studentIds");
        
        if (studentIdsInt == null || studentIdsInt.isEmpty()) {
            return Result.error(400, "No students selected");
        }

        int successCount = 0;
        for (Integer idInt : studentIdsInt) {
            Long studentId = Long.valueOf(idInt);
            User student = userMapper.selectById(studentId);
            if (student != null) {
                // 1. Send Station Message
                messageService.sendMessage(0L, studentId, subject, content, 2); // 0=System, 2=Remind
                
                // 2. Send Notification
                notificationService.sendNotification(studentId, subject, content, "ALERT");
                com.example.learningcheckin.websocket.NotificationWebSocket.sendInfo(studentId, 
                    String.format("{\"type\":\"ALERT\", \"title\":\"%s\", \"content\":\"%s\"}", subject, content));

                // 3. Send Email (Async recommended, but here synchronous for simplicity)
                if (student.getEmail() != null && !student.getEmail().isEmpty()) {
                    emailService.sendSimpleMail(student.getEmail(), subject, content);
                }
                successCount++;
            }
        }

        return Result.success("Sent reminders to " + successCount + " students");
    }

    @PostMapping("/join")
    public Result<String> joinCourse(@RequestBody Map<String, Object> params) {
        Long studentId = Long.valueOf(params.get("studentId").toString());
        String code = (String) params.get("code");
        try {
            courseService.joinCourse(studentId, code);
            return Result.success("Joined successfully");
        } catch (Exception e) {
            return Result.error(400, e.getMessage());
        }
    }
}
