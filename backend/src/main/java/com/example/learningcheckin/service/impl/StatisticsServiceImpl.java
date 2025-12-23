package com.example.learningcheckin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.learningcheckin.dto.DailyStatsDTO;
import com.example.learningcheckin.dto.StudentAbnormalDTO;
import com.example.learningcheckin.dto.StudentPortraitDTO;
import com.example.learningcheckin.dto.TrendPointDTO;
import com.example.learningcheckin.entity.*;
import com.example.learningcheckin.mapper.*;
import com.example.learningcheckin.service.IStatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class StatisticsServiceImpl implements IStatisticsService {

    @Autowired
    private CourseStudentMapper courseStudentMapper;

    @Autowired
    private CheckinMapper checkinMapper;

    @Autowired
    private StudyPlanMapper studyPlanMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private TaskMapper taskMapper;

    @Autowired
    private TaskSubmissionMapper taskSubmissionMapper;

    @Autowired
    private TaskCheckinMapper taskCheckinMapper;

    @Override
    public DailyStatsDTO getDailyStats(Long courseId, LocalDate date) {
        if (date == null) {
            date = LocalDate.now();
        }

        DailyStatsDTO stats = new DailyStatsDTO();
        stats.setDate(date);

        // 1. Get Active Students
        List<CourseStudent> students = courseStudentMapper.selectList(new LambdaQueryWrapper<CourseStudent>()
                .eq(CourseStudent::getCourseId, courseId)
                .eq(CourseStudent::getStatus, 0)); // 0: Active

        int totalStudents = students.size();
        stats.setTotalStudents(totalStudents);

        if (totalStudents == 0) {
            stats.setCheckedInCount(0);
            stats.setCheckinRate(BigDecimal.ZERO);
            stats.setAbnormalStudents(new ArrayList<>());
            return stats;
        }

        List<Long> studentIds = students.stream().map(CourseStudent::getStudentId).collect(Collectors.toList());

        // 2. Count Check-ins for these students on date
        // Using CheckinMapper. We need to query count where date = date and userId in studentIds
        Long checkinCount = checkinMapper.selectCount(new LambdaQueryWrapper<Checkin>()
                .eq(Checkin::getCheckinDate, date)
                .in(Checkin::getUserId, studentIds));

        stats.setCheckedInCount(checkinCount.intValue());

        BigDecimal rate = BigDecimal.valueOf(checkinCount)
                .divide(BigDecimal.valueOf(totalStudents), 4, RoundingMode.HALF_UP) // 4 decimal places
                .multiply(BigDecimal.valueOf(100))
                .setScale(1, RoundingMode.HALF_UP); // 1 decimal place (e.g. 85.5)
        stats.setCheckinRate(rate);

        // 3. Find Abnormal Students (Completion Rate < 60%)
        // We calculate average completion rate for each student across all their plans in this course
        List<StudentAbnormalDTO> abnormalList = new ArrayList<>();

        // Get all plans for this course (student copies)
        List<StudyPlan> coursePlans = studyPlanMapper.selectList(new LambdaQueryWrapper<StudyPlan>()
                .eq(StudyPlan::getCourseId, courseId)
                .in(StudyPlan::getUserId, studentIds));

        // Group by Student
        Map<Long, List<StudyPlan>> studentPlans = coursePlans.stream()
                .collect(Collectors.groupingBy(StudyPlan::getUserId));

        for (Long studentId : studentIds) {
            List<StudyPlan> myPlans = studentPlans.getOrDefault(studentId, new ArrayList<>());
            
            int totalTasks = 0;
            int completedTasks = 0;
            
            for (StudyPlan p : myPlans) {
                if (p.getTotalTasks() != null) totalTasks += p.getTotalTasks();
                if (p.getCompletedTasks() != null) completedTasks += p.getCompletedTasks();
            }

            BigDecimal completionRate = BigDecimal.ZERO;
            if (totalTasks > 0) {
                completionRate = BigDecimal.valueOf(completedTasks)
                        .divide(BigDecimal.valueOf(totalTasks), 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100));
            } else {
                // If no tasks, assume 100% or 0%? 
                // If plans exist but 0 tasks, maybe 100%. If no plans, maybe N/A (ignore).
                // Let's assume if no tasks assigned, they are not abnormal.
                if (myPlans.isEmpty()) completionRate = BigDecimal.valueOf(100);
            }

            if (completionRate.compareTo(BigDecimal.valueOf(60)) < 0) {
                StudentAbnormalDTO abnormal = new StudentAbnormalDTO();
                abnormal.setStudentId(studentId);
                abnormal.setTotalTasks(totalTasks);
                abnormal.setCompletedTasks(completedTasks);
                abnormal.setCompletionRate(completionRate.setScale(1, RoundingMode.HALF_UP));
                
                User user = userMapper.selectById(studentId);
                if (user != null) {
                    abnormal.setStudentName(user.getUsername()); // or real name
                    abnormal.setStudentNumber(user.getUsername()); // placeholder
                } else {
                    abnormal.setStudentName("Unknown");
                }
                
                abnormalList.add(abnormal);
            }
        }

        stats.setAbnormalStudents(abnormalList);

        return stats;
    }

    @Override
    public List<TrendPointDTO> getCompletionTrend(Long courseId, Integer days) {
        if (days == null || days <= 0) days = 30;
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days - 1);

        List<TrendPointDTO> trend = new ArrayList<>();

        // Get Active Students
        List<CourseStudent> students = courseStudentMapper.selectList(new LambdaQueryWrapper<CourseStudent>()
                .eq(CourseStudent::getCourseId, courseId)
                .eq(CourseStudent::getStatus, 0));
        
        int totalStudents = students.size();
        if (totalStudents == 0) {
             return trend; // Empty
        }
        List<Long> studentIds = students.stream().map(CourseStudent::getStudentId).collect(Collectors.toList());

        // For each day, calculate Check-in Rate (as a proxy for "Activity/Completion Trend" for now)
        // Optimization: Group by date in SQL
        // SELECT checkin_date, COUNT(*) FROM sys_checkin WHERE ... GROUP BY checkin_date
        
        List<Checkin> checkins = checkinMapper.selectList(new LambdaQueryWrapper<Checkin>()
                .ge(Checkin::getCheckinDate, startDate)
                .le(Checkin::getCheckinDate, endDate)
                .in(Checkin::getUserId, studentIds));
        
        Map<LocalDate, Long> countByDate = checkins.stream()
                .collect(Collectors.groupingBy(Checkin::getCheckinDate, Collectors.counting()));

        for (int i = 0; i < days; i++) {
            LocalDate d = startDate.plusDays(i);
            TrendPointDTO point = new TrendPointDTO();
            point.setDate(d.toString());
            
            Long count = countByDate.getOrDefault(d, 0L);
            BigDecimal rate = BigDecimal.valueOf(count)
                    .divide(BigDecimal.valueOf(totalStudents), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(1, RoundingMode.HALF_UP);
            
            point.setRate(rate);
            trend.add(point);
        }

        return trend;
    }

    @Override
    public StudentPortraitDTO getStudentPortrait(Long courseId, Long studentId) {
        StudentPortraitDTO dto = new StudentPortraitDTO();

        // 1. Basic Stats
        // Total Tasks (Published only)
        Long totalTasks = taskMapper.selectCount(new LambdaQueryWrapper<Task>()
                .eq(Task::getCourseId, courseId)
                .eq(Task::getStatus, 1));
        dto.setTotalTasks(totalTasks.intValue());

        // Get Course Tasks IDs
        List<Task> courseTasks = taskMapper.selectList(new LambdaQueryWrapper<Task>()
                .eq(Task::getCourseId, courseId));
        List<Long> taskIds = courseTasks.stream().map(Task::getId).collect(Collectors.toList());

        int completedTasks = 0;
        int totalCheckins = 0;
        double avgScore = 0.0;

        Map<String, Integer> dateMap = new HashMap<>();

        if (!taskIds.isEmpty()) {
            // Completed Tasks (Submissions with status 1 or 2)
            List<TaskSubmission> submissions = taskSubmissionMapper.selectList(new LambdaQueryWrapper<TaskSubmission>()
                    .in(TaskSubmission::getTaskId, taskIds)
                    .eq(TaskSubmission::getStudentId, studentId));
            
            List<TaskSubmission> completedSubmissions = submissions.stream()
                    .filter(s -> s.getStatus() == 1 || s.getStatus() == 2)
                    .collect(Collectors.toList());
            
            completedTasks = completedSubmissions.size();
            
            // Calculate Avg Score
            double sumScore = completedSubmissions.stream().mapToInt(s -> s.getScore() != null ? s.getScore() : 0).sum();
            if (completedTasks > 0) {
                avgScore = sumScore / completedTasks;
            }

            // Count Checkins (TaskCheckin) for this course's tasks (Recurring)
            List<TaskCheckin> checkins = taskCheckinMapper.selectList(new LambdaQueryWrapper<TaskCheckin>()
                    .in(TaskCheckin::getTaskId, taskIds)
                    .eq(TaskCheckin::getStudentId, studentId));
            totalCheckins = checkins.size();

            // Populate Heatmap from Submissions
            for (TaskSubmission s : submissions) {
                if (s.getSubmitTime() != null) {
                    String dateStr = s.getSubmitTime().toLocalDate().toString();
                    dateMap.put(dateStr, dateMap.getOrDefault(dateStr, 0) + 1);
                }
            }

            // Populate Heatmap from Checkins
            for (TaskCheckin tc : checkins) {
                 String dateStr = tc.getDate().toString();
                 dateMap.put(dateStr, dateMap.getOrDefault(dateStr, 0) + 1);
            }
        }

        dto.setCompletedTasks(completedTasks);
        dto.setTotalCheckins(totalCheckins);
        dto.setAverageScore(avgScore);

        // 2. Radar Chart
        double completionScore = 0.0;
        if (totalTasks > 0) {
            completionScore = (double) completedTasks / totalTasks * 100;
        } else {
            completionScore = 100.0; // No tasks = 100% complete? Or 0? Let's say 100 for now if empty.
        }
        dto.setCompletionScore(Math.min(100.0, completionScore));

        // Punctuality: Mock based on completion (Real logic would check deadlines)
        dto.setPunctualityScore(completionScore > 80 ? 95.0 : 80.0);

        // Quality: Avg Score. If 0 (ungraded), use 80 default.
        dto.setQualityScore(avgScore > 0 ? avgScore : 80.0);

        // Interaction: Mock based on checkins.
        dto.setInteractionScore(Math.min(100.0, totalCheckins * 5.0 + 60.0)); 

        // Growth: Mock
        dto.setGrowthScore(85.0);

        // 3. Heatmap
        List<Map<String, Object>> heatmap = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : dateMap.entrySet()) {
            Map<String, Object> point = new HashMap<>();
            point.put("date", entry.getKey());
            point.put("count", entry.getValue());
            heatmap.add(point);
        }
        dto.setActivityHeatmap(heatmap);

        return dto;
    }

    @Override
    public List<Map<String, Object>> getLeaderboard(Long courseId, String type, Integer limit) {
        if (limit == null || limit <= 0) limit = 10;
        List<Map<String, Object>> result = new ArrayList<>();

        if ("points".equalsIgnoreCase(type) || "score".equalsIgnoreCase(type)) {
            // Top Points
            LambdaQueryWrapper<User> query = new LambdaQueryWrapper<User>()
                    .eq(User::getRole, "STUDENT")
                    .orderByDesc(User::getPoints)
                    .last("LIMIT " + limit);
            
            if (courseId != null) {
                query.inSql(User::getId, "SELECT student_id FROM sys_course_student WHERE course_id = " + courseId + " AND status = 0");
            }
            
            List<User> users = userMapper.selectList(query);
            
            for (int i = 0; i < users.size(); i++) {
                User u = users.get(i);
                Map<String, Object> map = new HashMap<>();
                map.put("rank", i + 1);
                map.put("name", u.getUsername());
                map.put("avatar", u.getAvatar());
                map.put("score", u.getPoints());
                result.add(map);
            }
        } else if ("checkins".equalsIgnoreCase(type)) {
            // Top Checkins (Diligence)
            com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<TaskCheckin> query = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
            query.select("student_id", "count(*) as count")
                 .groupBy("student_id")
                 .orderByDesc("count")
                 .last("LIMIT " + limit);
            
            if (courseId != null) {
                 query.inSql("task_id", "SELECT id FROM sys_task WHERE course_id = " + courseId);
            }
                 
            List<Map<String, Object>> list = taskCheckinMapper.selectMaps(query);
            
            for (int i = 0; i < list.size(); i++) {
                Map<String, Object> item = list.get(i);
                Long studentId = Long.valueOf(item.get("student_id").toString());
                Long count = Long.valueOf(item.get("count").toString());
                
                User u = userMapper.selectById(studentId);
                if (u != null) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("rank", i + 1);
                    map.put("name", u.getUsername());
                    map.put("avatar", u.getAvatar());
                    map.put("score", count);
                    result.add(map);
                }
            }
        }

        return result;
    }
}
