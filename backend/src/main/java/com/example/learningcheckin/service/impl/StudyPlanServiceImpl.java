package com.example.learningcheckin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.learningcheckin.entity.PointsRecord;
import com.example.learningcheckin.entity.StudyPlan;
import com.example.learningcheckin.entity.StudyPlanProgressHistory;
import com.example.learningcheckin.entity.User;
import com.example.learningcheckin.mapper.PointsRecordMapper;
import com.example.learningcheckin.mapper.StudyPlanMapper;
import com.example.learningcheckin.mapper.StudyPlanProgressHistoryMapper;
import com.example.learningcheckin.mapper.StudyPlanTaskMapper;
import com.example.learningcheckin.mapper.UserMapper;
import com.example.learningcheckin.service.IStudyPlanService;
import com.example.learningcheckin.entity.StudyPlanTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

import com.example.learningcheckin.entity.CourseStudent;
import com.example.learningcheckin.mapper.CourseStudentMapper;

@Service
public class StudyPlanServiceImpl extends ServiceImpl<StudyPlanMapper, StudyPlan> implements IStudyPlanService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PointsRecordMapper pointsRecordMapper;

    @Autowired
    private StudyPlanProgressHistoryMapper historyMapper;

    @Autowired
    private StudyPlanTaskMapper taskMapper;

    @Autowired
    private CourseStudentMapper courseStudentMapper;

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
        StudyPlan plan = this.getById(planId);
        if (plan == null) {
            throw new RuntimeException("Plan not found");
        }
        
        // Check permission or status if needed...

        BigDecimal previousProgress = plan.getProgressPercentage();
        if (previousProgress == null) {
            previousProgress = BigDecimal.ZERO;
        }

        plan.setCompletedTasks(completedTasks);
        plan.setTotalTasks(totalTasks);
        
        // Calculate percentage
        BigDecimal percentage = BigDecimal.ZERO;
        if (totalTasks != null && totalTasks > 0) {
            percentage = BigDecimal.valueOf(completedTasks)
                    .divide(BigDecimal.valueOf(totalTasks), 3, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(1, RoundingMode.HALF_UP);
        }
        plan.setProgressPercentage(percentage);
        plan.setUpdateTime(LocalDateTime.now());
        
        // If 100%, mark as completed automatically? Maybe not, let user decide or keep logic separate.
        // But we can check if completedTasks == totalTasks
        
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
        recalculatePlanProgress(task.getPlanId());
        
        return task;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTask(Long taskId) {
        StudyPlanTask task = taskMapper.selectById(taskId);
        if (task != null) {
            Long planId = task.getPlanId();
            taskMapper.deleteById(taskId);
            recalculatePlanProgress(planId);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public StudyPlanTask updateTaskStatus(Long taskId, Integer status) {
        StudyPlanTask task = taskMapper.selectById(taskId);
        if (task != null) {
            task.setStatus(status);
            taskMapper.updateById(task);
            recalculatePlanProgress(task.getPlanId());
        }
        return task;
    }

    @Override
    public List<StudyPlanTask> getPlanTasks(Long planId) {
        return taskMapper.selectList(new LambdaQueryWrapper<StudyPlanTask>()
                .eq(StudyPlanTask::getPlanId, planId)
                .orderByAsc(StudyPlanTask::getDeadline));
    }

    private void recalculatePlanProgress(Long planId) {
        List<StudyPlanTask> tasks = getPlanTasks(planId);
        if (tasks.isEmpty()) return; // Or set to 0?

        int total = tasks.size();
        int completed = (int) tasks.stream().filter(t -> t.getStatus() == 1).count();
        
        updateProgress(planId, completed, total, "Auto update from tasks");
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
