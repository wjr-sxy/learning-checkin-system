package com.example.learningcheckin.controller;

import com.example.learningcheckin.common.Result;
import com.example.learningcheckin.dto.StudyPlanProgressRequest;
import com.example.learningcheckin.entity.StudyPlan;
import com.example.learningcheckin.entity.StudyPlanProgressHistory;
import com.example.learningcheckin.entity.StudyPlanTask;
import com.example.learningcheckin.service.IStudyPlanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/study-plan")
@CrossOrigin
public class StudyPlanController {

    @Autowired
    private IStudyPlanService studyPlanService;

    @GetMapping
    public Result<List<StudyPlan>> getUserPlans(@RequestParam Long userId) {
        return Result.success(studyPlanService.getUserPlans(userId));
    }

    @PostMapping
    public Result<StudyPlan> createPlan(@RequestBody StudyPlan plan) {
        return Result.success(studyPlanService.createPlan(plan));
    }

    @PutMapping
    public Result<StudyPlan> updatePlan(@RequestBody StudyPlan plan) {
        return Result.success(studyPlanService.updatePlan(plan));
    }

    @DeleteMapping("/{id}")
    public Result<String> deletePlan(@PathVariable Long id) {
        studyPlanService.deletePlan(id);
        return Result.success("删除成功");
    }

    @PutMapping("/{id}/complete")
    public Result<String> completePlan(@PathVariable Long id) {
        try {
            studyPlanService.completePlan(id);
            return Result.success("计划已完成，积分已发放！");
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
    }

    @PutMapping("/{id}/progress")
    public Result<StudyPlan> updateProgress(@PathVariable Long id, @RequestBody StudyPlanProgressRequest request) {
        try {
            StudyPlan plan = studyPlanService.updateProgress(id, request.getCompletedTasks(), request.getTotalTasks(), request.getNote());
            return Result.success(plan);
        } catch (Exception e) {
            return Result.error(400, "Update failed: " + e.getMessage());
        }
    }

    @GetMapping("/{id}/history")
    public Result<List<StudyPlanProgressHistory>> getProgressHistory(@PathVariable Long id) {
        return Result.success(studyPlanService.getProgressHistory(id));
    }

    // Task Endpoints

    @GetMapping("/{id}/tasks")
    public Result<List<StudyPlanTask>> getPlanTasks(@PathVariable Long id) {
        return Result.success(studyPlanService.getPlanTasks(id));
    }

    @PostMapping("/{id}/tasks")
    public Result<StudyPlanTask> addTask(@PathVariable Long id, @RequestBody StudyPlanTask task) {
        task.setPlanId(id);
        return Result.success(studyPlanService.addTask(task));
    }

    @DeleteMapping("/tasks/{taskId}")
    public Result<String> deleteTask(@PathVariable Long taskId) {
        studyPlanService.deleteTask(taskId);
        return Result.success("Deleted task");
    }

    @PutMapping("/tasks/{taskId}/status")
    public Result<StudyPlanTask> updateTaskStatus(@PathVariable Long taskId, @RequestBody Map<String, Integer> body) {
        Integer status = body.get("status");
        return Result.success(studyPlanService.updateTaskStatus(taskId, status));
    }

    // Course Plan Distribution
    @GetMapping("/course/{courseId}")
    public Result<List<StudyPlan>> getCoursePlans(@PathVariable Long courseId, @RequestParam(required = false) Long creatorId) {
        return Result.success(studyPlanService.getCoursePlans(courseId, creatorId));
    }

    @PostMapping("/{planId}/distribute")
    public Result<String> distributePlan(@PathVariable Long planId, @RequestBody Map<String, Long> body) {
        Long courseId = body.get("courseId");
        studyPlanService.distributePlanToCourse(planId, courseId);
        return Result.success("Plan distributed to course students");
    }
    
    @GetMapping("/daily-points")
    public Result<Integer> getDailyPoints(@RequestParam Long userId) {
        return Result.success(studyPlanService.getDailyPlanPoints(userId));
    }
}
