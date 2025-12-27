package com.example.learningcheckin.controller;

import com.example.learningcheckin.common.Result;
import com.example.learningcheckin.dto.StudyPlanTaskProgressRequest;
import com.example.learningcheckin.entity.StudyPlan;
import com.example.learningcheckin.entity.StudyPlanTask;
import com.example.learningcheckin.service.IStudyPlanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/study-plan")
public class StudyPlanController {

    @Autowired
    private IStudyPlanService studyPlanService;

    @GetMapping("/user/{userId}")
    public Result<List<StudyPlan>> getUserPlans(@PathVariable Long userId) {
        return Result.success(studyPlanService.getUserPlans(userId));
    }

    @PostMapping("/create")
    public Result<StudyPlan> createPlan(@RequestBody StudyPlan plan) {
        try {
            return Result.success(studyPlanService.createPlan(plan));
        } catch (Exception e) {
            return Result.error(400, "Creation failed: " + e.getMessage());
        }
    }

    @PutMapping("/update")
    public Result<StudyPlan> updatePlan(@RequestBody StudyPlan plan) {
        return Result.success(studyPlanService.updatePlan(plan));
    }

    @DeleteMapping("/{planId}")
    public Result<String> deletePlan(@PathVariable Long planId) {
        studyPlanService.deletePlan(planId);
        return Result.success("Deleted successfully");
    }

    @PostMapping("/{planId}/complete")
    public Result<String> completePlan(@PathVariable Long planId) {
        try {
            studyPlanService.completePlan(planId);
            return Result.success("Completed successfully");
        } catch (Exception e) {
            return Result.error(400, e.getMessage());
        }
    }

    @PostMapping("/{planId}/progress")
    public Result<StudyPlan> updateProgress(@PathVariable Long planId, @RequestParam Integer completed, @RequestParam Integer total) {
        try {
            return Result.success(studyPlanService.updateProgress(planId, completed, total, "Manual Update"));
        } catch (Exception e) {
            return Result.error(400, e.getMessage());
        }
    }

    // Task management
    @PostMapping("/task/add")
    public Result<StudyPlanTask> addTask(@RequestBody StudyPlanTask task) {
        return Result.success(studyPlanService.addTask(task));
    }

    @DeleteMapping("/task/{taskId}")
    public Result<String> deleteTask(@PathVariable Long taskId) {
        studyPlanService.deleteTask(taskId);
        return Result.success("Deleted");
    }

    @PutMapping("/task/{taskId}/status")
    public Result<StudyPlanTask> updateTaskStatus(@PathVariable Long taskId, @RequestParam Integer status) {
        return Result.success(studyPlanService.updateTaskStatus(taskId, status));
    }

    @GetMapping("/{planId}/tasks")
    public Result<List<StudyPlanTask>> getPlanTasks(@PathVariable Long planId) {
        return Result.success(studyPlanService.getPlanTasks(planId));
    }

    @PostMapping("/task/{taskId}/progress")
    public Result<StudyPlanTask> updateTaskProgress(@PathVariable Long taskId, @RequestBody StudyPlanTaskProgressRequest request) {
        try {
            return Result.success(studyPlanService.updateTaskProgress(taskId, request));
        } catch (Exception e) {
            return Result.error(400, "Update failed: " + e.getMessage());
        }
    }
}
