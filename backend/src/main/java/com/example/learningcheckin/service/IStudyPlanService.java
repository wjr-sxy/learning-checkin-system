package com.example.learningcheckin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.learningcheckin.entity.StudyPlan;
import com.example.learningcheckin.entity.StudyPlanProgressHistory;
import com.example.learningcheckin.entity.StudyPlanTask;

import java.util.List;

public interface IStudyPlanService extends IService<StudyPlan> {
    List<StudyPlan> getUserPlans(Long userId);
    StudyPlan createPlan(StudyPlan plan);
    StudyPlan updatePlan(StudyPlan plan);
    void deletePlan(Long planId);
    void completePlan(Long planId);
    StudyPlan updateProgress(Long planId, Integer completedTasks, Integer totalTasks, String note);
    List<StudyPlanProgressHistory> getProgressHistory(Long planId);
    
    // Task related
    StudyPlanTask addTask(StudyPlanTask task);
    void deleteTask(Long taskId);
    StudyPlanTask updateTaskStatus(Long taskId, Integer status);
    List<StudyPlanTask> getPlanTasks(Long planId);

    // Teacher Plan Distribution
    List<StudyPlan> getCoursePlans(Long courseId, Long creatorId);
    void distributePlanToCourse(Long planId, Long courseId);
    
    Integer getDailyPlanPoints(Long userId);
}
