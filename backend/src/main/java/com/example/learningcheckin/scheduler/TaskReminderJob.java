package com.example.learningcheckin.scheduler;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.learningcheckin.entity.Task;
import com.example.learningcheckin.entity.TaskSubmission;
import com.example.learningcheckin.entity.User;
import com.example.learningcheckin.mapper.TaskMapper;
import com.example.learningcheckin.mapper.TaskSubmissionMapper;
import com.example.learningcheckin.mapper.UserMapper;
import com.example.learningcheckin.websocket.NotificationWebSocket;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
public class TaskReminderJob {

    @Autowired
    private TaskMapper taskMapper;

    @Autowired
    private TaskSubmissionMapper submissionMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Scheduled(cron = "0 * * * * ?") // Every minute
    public void checkReminders() {
        LocalDateTime now = LocalDateTime.now();
        
        // Find active tasks with future deadline
        QueryWrapper<Task> query = new QueryWrapper<>();
        query.gt("deadline", now)
             .eq("status", 1) // Published
             .isNotNull("reminder_config");
        
        List<Task> tasks = taskMapper.selectList(query);

        for (Task task : tasks) {
            try {
                String config = task.getReminderConfig();
                if (config == null || config.isEmpty()) continue;

                // Parse config: ["24", "3", "1"]
                List<String> reminders = objectMapper.readValue(config, new TypeReference<List<String>>(){});
                
                for (String hourStr : reminders) {
                    int hours = Integer.parseInt(hourStr);
                    LocalDateTime reminderTime = task.getDeadline().minusHours(hours);
                    
                    // Check if now is within 2 minutes of reminderTime
                    long diffMinutes = Math.abs(Duration.between(now, reminderTime).toMinutes());
                    
                    if (diffMinutes <= 2) {
                        String redisKey = "task:reminder:sent:" + task.getId() + ":" + hours;
                        Boolean sent = redisTemplate.hasKey(redisKey);
                        
                        if (Boolean.FALSE.equals(sent)) {
                            sendReminders(task, hours);
                            redisTemplate.opsForValue().set(redisKey, "1", 24, TimeUnit.HOURS);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void sendReminders(Task task, int hoursLeft) {
        // Find students who haven't submitted
        // 1. Get all students
        List<User> students = userMapper.selectList(new QueryWrapper<User>().eq("role", "STUDENT"));
        
        // 2. Get submissions for this task
        List<TaskSubmission> submissions = submissionMapper.selectList(new QueryWrapper<TaskSubmission>().eq("task_id", task.getId()));
        Set<Long> submittedStudentIds = submissions.stream()
                .map(TaskSubmission::getStudentId)
                .collect(Collectors.toSet());
        
        // 3. Filter and send
        for (User student : students) {
            if (!submittedStudentIds.contains(student.getId())) {
                String message = String.format("{\"type\":\"TASK_REMINDER\", \"taskId\":%d, \"title\":\"%s\", \"hoursLeft\":%d}", 
                        task.getId(), task.getTitle(), hoursLeft);
                NotificationWebSocket.sendInfo(student.getId(), message);
            }
        }
    }
}
