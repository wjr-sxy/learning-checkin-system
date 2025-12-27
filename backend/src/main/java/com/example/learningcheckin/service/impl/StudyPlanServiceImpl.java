package com.example.learningcheckin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.learningcheckin.dto.StudyPlanTaskProgressRequest;
import com.example.learningcheckin.entity.*;
import com.example.learningcheckin.event.TaskCompletedEvent;
import com.example.learningcheckin.mapper.*;
import com.example.learningcheckin.service.ILearningLogService;
import com.example.learningcheckin.service.IStudyPlanService;
import com.example.learningcheckin.service.IPointsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class StudyPlanServiceImpl extends ServiceImpl<StudyPlanMapper, StudyPlan> implements IStudyPlanService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PointsRecordMapper pointsRecordMapper;
    
    @Autowired
    private IPointsService pointsService;

    @Autowired
    private StudyPlanProgressHistoryMapper historyMapper;

    @Autowired
    private StudyPlanTaskMapper taskMapper;

    @Autowired
    private CourseStudentMapper courseStudentMapper;
    
    @Autowired
    private com.example.learningcheckin.mapper.CheckinMapper checkinMapper;

    @Autowired
    private ILearningLogService learningLogService;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Override
    public List<StudyPlan> getUserPlans(Long userId) {
        List<StudyPlan> plans = this.list(new LambdaQueryWrapper<StudyPlan>()
                .eq(StudyPlan::getUserId, userId)
                .orderByDesc(StudyPlan::getCreateTime));

        // Check for expiration
        java.time.LocalDate today = java.time.LocalDate.now();
        for (StudyPlan plan : plans) {
            if (plan.getStatus() == 0 && plan.getEndDate() != null && plan.getEndDate().isBefore(today)) {
                plan.setStatus(2); // Set to Expired
                this.updateById(plan);
            }
        }
        return plans;
    }

    @Override
    public StudyPlan createPlan(StudyPlan plan) {
        plan.setStatus(0);
        plan.setCreateTime(LocalDateTime.now());
        plan.setUpdateTime(LocalDateTime.now());
        if (plan.getTargetHours() == null) {
            plan.setTargetHours(0);
        }

        // Check daily creation limit (Max 5 for point eligibility)
        java.time.LocalDate today = java.time.LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(23, 59, 59);
        
        Long count = this.count(new LambdaQueryWrapper<StudyPlan>()
                .eq(StudyPlan::getUserId, plan.getUserId())
                .ge(StudyPlan::getCreateTime, startOfDay)
                .le(StudyPlan::getCreateTime, endOfDay));
        
        if (count >= 5) {
             plan.setIsPointEligible(false);
        } else {
             plan.setIsPointEligible(true);
        }

        this.save(plan);
        return plan;
    }

    @Override
    public StudyPlan updatePlan(StudyPlan plan) {
        plan.setUpdateTime(LocalDateTime.now());
        this.updateById(plan);
        return plan;
    }

    @Override
    public void deletePlan(Long planId) {
        this.removeById(planId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void completePlan(Long planId) {
        StudyPlan plan = this.getById(planId);
        if (plan == null) {
            throw new RuntimeException("Plan not found");
        }
        
        // Double check expiration
        if (plan.getEndDate() != null && plan.getEndDate().isBefore(java.time.LocalDate.now())) {
            plan.setStatus(2);
            this.updateById(plan);
            throw new RuntimeException("Plan has expired");
        }

        if (plan.getStatus() != 0) {
            throw new RuntimeException("Plan already completed or expired");
        }

        plan.setStatus(1); // Completed
        plan.setUpdateTime(LocalDateTime.now());
        this.updateById(plan);

        // Add Points Reward
        // Rule: +2 points per plan, max 10 points per day, only if eligible
        if (Boolean.TRUE.equals(plan.getIsPointEligible())) {
            int pointsReward = 2;
            
            // Check daily cap
            java.time.LocalDate today = java.time.LocalDate.now();
            LocalDateTime startOfDay = today.atStartOfDay();
            LocalDateTime endOfDay = today.atTime(23, 59, 59);
            
            List<PointsRecord> dailyRecords = pointsRecordMapper.selectList(new LambdaQueryWrapper<PointsRecord>()
                     .eq(PointsRecord::getUserId, plan.getUserId())
                     .eq(PointsRecord::getType, 1)
                     .ge(PointsRecord::getCreateTime, startOfDay)
                     .le(PointsRecord::getCreateTime, endOfDay));
            
            int dailyPoints = dailyRecords.stream().mapToInt(PointsRecord::getAmount).sum();
            
            if (dailyPoints < 10) {
                if (dailyPoints + pointsReward > 10) {
                    pointsReward = 10 - dailyPoints;
                }
                
                if (pointsReward > 0) {
                    User user = userMapper.selectById(plan.getUserId());
                    user.setPoints(user.getPoints() + pointsReward);
                    userMapper.updateById(user);
            
                    PointsRecord pr = new PointsRecord();
                    pr.setUserId(user.getId());
                    pr.setType(1);
                    pr.setAmount(pointsReward);
                    pr.setDescription("Completed Study Plan: " + plan.getTitle());
                    pr.setCreateTime(LocalDateTime.now());
                    pointsRecordMapper.insert(pr);
                }
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public StudyPlan updateProgress(Long planId, Integer completedTasks, Integer totalTasks, String note) {
        // Overloaded for backward compatibility or direct calls
        return updateProgressInternal(planId, completedTasks, totalTasks, null, note);
    }
    
    private StudyPlan updateProgressInternal(Long planId, Integer completedTasks, Integer totalTasks, BigDecimal overridePercentage, String note) {
        StudyPlan plan = this.getById(planId);
        if (plan == null) {
            throw new RuntimeException("Plan not found");
        }
        
        BigDecimal previousProgress = plan.getProgressPercentage();
        if (previousProgress == null) {
            previousProgress = BigDecimal.ZERO;
        }

        plan.setCompletedTasks(completedTasks);
        plan.setTotalTasks(totalTasks);
        
        // Calculate percentage
        BigDecimal percentage = BigDecimal.ZERO;
        if (overridePercentage != null) {
            percentage = overridePercentage;
        } else if (totalTasks != null && totalTasks > 0) {
            percentage = BigDecimal.valueOf(completedTasks)
                    .divide(BigDecimal.valueOf(totalTasks), 3, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(1, RoundingMode.HALF_UP);
        }
        
        // Cap at 100
        if (percentage.compareTo(BigDecimal.valueOf(100)) > 0) {
            percentage = BigDecimal.valueOf(100);
        }
        
        plan.setProgressPercentage(percentage);
        plan.setUpdateTime(LocalDateTime.now());
        
        // Check Completion (100%)
        if (percentage.compareTo(BigDecimal.valueOf(100)) == 0 && plan.getStatus() == 0) {
            plan.setStatus(1); // Completed
            
            // Award Agreed Points
            if (plan.getRewardPoints() != null && plan.getRewardPoints() > 0) {
                pointsService.addPoints(plan.getUserId(), plan.getRewardPoints(), "完成学习计划: " + plan.getTitle());
            } else if (Boolean.TRUE.equals(plan.getIsPointEligible())) {
                // Fallback to old logic? 
                // We can call completePlan logic or just leave it.
                // For now, let's respect rewardPoints.
            }
        }
        
        this.updateById(plan);

        // Create History Record
        StudyPlanProgressHistory history = new StudyPlanProgressHistory();
        history.setPlanId(planId);
        history.setPreviousProgress(previousProgress);
        history.setNewProgress(percentage);
        history.setCompletedTasks(completedTasks);
        history.setTotalTasks(totalTasks);
        history.setNote(note);
        history.setCreateTime(LocalDateTime.now());
        
        historyMapper.insert(history);
        
        return plan;
    }

    @Override
    public List<StudyPlanProgressHistory> getProgressHistory(Long planId) {
        return historyMapper.selectList(new LambdaQueryWrapper<StudyPlanProgressHistory>()
                .eq(StudyPlanProgressHistory::getPlanId, planId)
                .orderByDesc(StudyPlanProgressHistory::getCreateTime));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public StudyPlanTask addTask(StudyPlanTask task) {
        task.setCreateTime(LocalDateTime.now());
        if (task.getStatus() == null) task.setStatus(0);
        taskMapper.insert(task);
        
        // Recalculate plan progress
        refreshPlanProgress(task.getPlanId());
        
        return task;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTask(Long taskId) {
        StudyPlanTask task = taskMapper.selectById(taskId);
        if (task != null) {
            Long planId = task.getPlanId();
            taskMapper.deleteById(taskId);
            refreshPlanProgress(planId);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public StudyPlanTask updateTaskStatus(Long taskId, Integer status) {
        StudyPlanTask task = taskMapper.selectById(taskId);
        if (task != null) {
            task.setStatus(status);
            taskMapper.updateById(task);
            refreshPlanProgress(task.getPlanId());
        }
        return task;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public StudyPlanTask updateTaskProgress(Long taskId, StudyPlanTaskProgressRequest request) {
        StudyPlanTask task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new RuntimeException("Task not found");
        }

        StudyPlan plan = this.getById(task.getPlanId());
        if (plan == null) {
            throw new RuntimeException("Plan not found");
        }

        // Security check for duration (e.g. max 4 hours per request)
        if (request.getDuration() != null && request.getDuration() > 14400) {
            throw new RuntimeException("Duration abnormal");
        }

        // Update Progress
        int current = task.getCurrentValue() != null ? task.getCurrentValue() : 0;
        int target = task.getTargetValue() != null ? task.getTargetValue() : 1;
        
        if (Boolean.TRUE.equals(request.getIsIncremental())) {
            current += request.getProgress() != null ? request.getProgress() : 0;
        } else {
            current = request.getProgress() != null ? request.getProgress() : current;
        }
        
        task.setCurrentValue(current);
        
        // Check completion
        boolean justCompleted = false;
        if (current >= target && (task.getStatus() == null || task.getStatus() == 0)) {
            task.setStatus(1);
            justCompleted = true;
        } else if (current < target) {
            task.setStatus(0);
        }
        
        taskMapper.updateById(task);

        // Log Activity
        learningLogService.logActivity(plan.getUserId(), taskId, task.getType(), request.getDuration(), 
            "Progress: " + current + "/" + target);

        // Refresh Plan Progress
        refreshPlanProgress(plan.getId());

        // Publish Event if completed
        if (justCompleted) {
            eventPublisher.publishEvent(new TaskCompletedEvent(this, plan.getUserId(), taskId, task.getType()));
        }

        return task;
    }

    @Override
    public List<StudyPlanTask> getPlanTasks(Long planId) {
        return taskMapper.selectList(new LambdaQueryWrapper<StudyPlanTask>()
                .eq(StudyPlanTask::getPlanId, planId)
                .orderByAsc(StudyPlanTask::getDeadline));
    }

    private void refreshPlanProgress(Long planId) {
        StudyPlan plan = this.getById(planId);
        if (plan == null) return;
        
        // 1. Tasks
        List<StudyPlanTask> tasks = getPlanTasks(planId);
        int totalTasks = tasks.size();
        int completedTasks = (int) tasks.stream().filter(t -> t.getStatus() == 1).count();
        
        BigDecimal taskProgress = BigDecimal.ZERO;
        if (totalTasks > 0) {
            taskProgress = BigDecimal.valueOf(completedTasks)
                    .divide(BigDecimal.valueOf(totalTasks), 3, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        }

        // 2. Time
        BigDecimal timeProgress = BigDecimal.ZERO;
        boolean hasTimeTarget = false;
        if (plan.getTargetHours() != null && plan.getTargetHours() > 0) {
            hasTimeTarget = true;
            LocalDate start = plan.getStartDate();
            LocalDate end = plan.getEndDate() != null ? plan.getEndDate() : LocalDate.now();
            
            List<Checkin> checkins = checkinMapper.selectList(new LambdaQueryWrapper<Checkin>()
                    .eq(Checkin::getUserId, plan.getUserId())
                    .ge(Checkin::getCheckinDate, start)
                    .le(Checkin::getCheckinDate, end));
            
            int totalMinutes = checkins.stream()
                    .mapToInt(c -> c.getStudyDuration() != null ? c.getStudyDuration() : 0)
                    .sum();
            
            timeProgress = BigDecimal.valueOf(totalMinutes)
                    .divide(BigDecimal.valueOf(plan.getTargetHours() * 60L), 3, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
            
            if (timeProgress.compareTo(BigDecimal.valueOf(100)) > 0) {
                timeProgress = BigDecimal.valueOf(100);
            }
        }
        
        // Combine
        BigDecimal finalProgress = BigDecimal.ZERO;
        if (totalTasks > 0 && hasTimeTarget) {
            finalProgress = taskProgress.add(timeProgress).divide(BigDecimal.valueOf(2), 1, RoundingMode.HALF_UP);
        } else if (totalTasks > 0) {
            finalProgress = taskProgress;
        } else if (hasTimeTarget) {
            finalProgress = timeProgress;
        }
        
        updateProgressInternal(planId, completedTasks, totalTasks, finalProgress, "Auto update (Tasks + Time)");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateAllPlansProgress(Long userId) {
        List<StudyPlan> activePlans = this.list(new LambdaQueryWrapper<StudyPlan>()
                .eq(StudyPlan::getUserId, userId)
                .eq(StudyPlan::getStatus, 0));
        
        for (StudyPlan plan : activePlans) {
             refreshPlanProgress(plan.getId());
        }
    }

    @Override
    public List<StudyPlan> getCoursePlans(Long courseId, Long creatorId) {
        LambdaQueryWrapper<StudyPlan> wrapper = new LambdaQueryWrapper<StudyPlan>()
                .eq(StudyPlan::getCourseId, courseId)
                .orderByDesc(StudyPlan::getCreateTime);
        
        if (creatorId != null) {
            wrapper.eq(StudyPlan::getUserId, creatorId);
        }
        
        return this.list(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void distributePlanToCourse(Long planId, Long courseId) {
        // 1. Get the template plan
        StudyPlan template = this.getById(planId);
        if (template == null) {
            throw new RuntimeException("Plan template not found");
        }
        
        // 2. Get all tasks for this plan
        List<StudyPlanTask> tasks = taskMapper.selectList(new LambdaQueryWrapper<StudyPlanTask>()
                .eq(StudyPlanTask::getPlanId, planId));

        // 3. Get all students in the course
        List<CourseStudent> students = courseStudentMapper.selectList(new LambdaQueryWrapper<CourseStudent>()
                .eq(CourseStudent::getCourseId, courseId));
        
        for (CourseStudent student : students) {
            // Create copy of plan
            StudyPlan newPlan = new StudyPlan();
            newPlan.setUserId(student.getStudentId());
            newPlan.setCourseId(courseId);
            newPlan.setTitle(template.getTitle());
            newPlan.setDescription(template.getDescription());
            newPlan.setTargetHours(template.getTargetHours());
            newPlan.setStartDate(template.getStartDate());
            newPlan.setEndDate(template.getEndDate());
            newPlan.setStatus(0);
            newPlan.setTotalTasks(template.getTotalTasks());
            newPlan.setCompletedTasks(0);
            newPlan.setProgressPercentage(BigDecimal.ZERO);
            newPlan.setCreateTime(LocalDateTime.now());
            newPlan.setUpdateTime(LocalDateTime.now());
            newPlan.setRewardPoints(template.getRewardPoints()); // Copy reward points
            
            this.save(newPlan);
            
            // Copy tasks
            for (StudyPlanTask t : tasks) {
                StudyPlanTask newTask = new StudyPlanTask();
                newTask.setPlanId(newPlan.getId());
                newTask.setTitle(t.getTitle());
                newTask.setDescription(t.getDescription());
                newTask.setDeadline(t.getDeadline());
                newTask.setPriority(t.getPriority());
                newTask.setStatus(0);
                newTask.setCreateTime(LocalDateTime.now());
                taskMapper.insert(newTask);
            }
        }
    }

    @Override
    public Integer getDailyPlanPoints(Long userId) {
        java.time.LocalDate today = java.time.LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(23, 59, 59);
        
        List<PointsRecord> dailyRecords = pointsRecordMapper.selectList(new LambdaQueryWrapper<PointsRecord>()
                 .eq(PointsRecord::getUserId, userId)
                 .eq(PointsRecord::getType, 1) // Type 1 is Study Plan Reward
                 .ge(PointsRecord::getCreateTime, startOfDay)
                 .le(PointsRecord::getCreateTime, endOfDay));
        
        return dailyRecords.stream().mapToInt(PointsRecord::getAmount).sum();
    }
}
