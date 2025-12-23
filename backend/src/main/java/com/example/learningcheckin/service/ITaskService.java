package com.example.learningcheckin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.learningcheckin.entity.Task;
import com.example.learningcheckin.entity.TaskCheckin;
import com.example.learningcheckin.entity.TaskSubmission;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface ITaskService extends IService<Task> {
    Task createTask(Task task);
    void submitTask(Long taskId, Long studentId, String content, String fileUrls);
    void submitRecurringTask(Long taskId, Long studentId, LocalDate date, String content, String fileUrl);
    void gradeSubmission(Long submissionId, Integer score, Integer rating, String comment, Long graderId);
    void returnSubmission(Long submissionId, String comment);
    
    // Batch operations
    void batchPass(List<Long> submissionIds);
    void batchReturn(List<Long> submissionIds, String comment);
    
    List<Map<String, Object>> getStudentTasks(Long studentId); // Returns tasks with submission status
    com.baomidou.mybatisplus.core.metadata.IPage<TaskSubmission> getTaskSubmissions(com.baomidou.mybatisplus.core.metadata.IPage<TaskSubmission> page, Long taskId);
    Map<String, Object> getTaskStats(Long taskId);
    List<Map<String, Object>> getTaskLeaderboard(Long taskId);
    List<Map<String, Object>> getTeacherTasks(Long teacherId);
    
    // For recurring tasks
    List<TaskCheckin> getTaskCheckins(Long taskId, Long studentId);
    void checkRecurringTasks();
}
