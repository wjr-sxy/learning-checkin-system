package com.example.learningcheckin;

import com.example.learningcheckin.dto.RegisterRequest;
import com.example.learningcheckin.entity.Task;
import com.example.learningcheckin.entity.User;
import com.example.learningcheckin.mapper.UserMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class BusinessIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserMapper userMapper;

    private String teacherToken;
    private String studentToken;
    private Long teacherId;
    private Long studentId;

    @BeforeEach
    public void setup() throws Exception {
        // Register & Login Teacher
        String tName = "teacher_" + UUID.randomUUID().toString().substring(0, 6);
        RegisterRequest tReg = new RegisterRequest();
        tReg.setUsername(tName);
        tReg.setPassword("pass123");
        tReg.setEmail(tName + "@test.com");
        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(tReg)));

        // Update role to TEACHER
        User tUser = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, tName));
        tUser.setRole("TEACHER");
        userMapper.updateById(tUser);

        Map<String, String> tLogin = new HashMap<>();
        tLogin.put("username", tName);
        tLogin.put("password", "pass123");
        MvcResult tRes = mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(tLogin)))
                .andExpect(status().isOk())
                .andReturn();
        Map<String, Object> resMap = objectMapper.readValue(tRes.getResponse().getContentAsString(), new TypeReference<Map<String, Object>>() {});
        teacherToken = "Bearer " + ((Map<String, Object>)resMap.get("data")).get("token");

        // Register & Login Student
        String sName = "student_" + UUID.randomUUID().toString().substring(0, 6);
        RegisterRequest sReg = new RegisterRequest();
        sReg.setUsername(sName);
        sReg.setPassword("pass123");
        sReg.setEmail(sName + "@test.com");
        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(sReg)));

        Map<String, String> sLogin = new HashMap<>();
        sLogin.put("username", sName);
        sLogin.put("password", "pass123");
        MvcResult sRes = mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(sLogin)))
                .andExpect(status().isOk())
                .andReturn();
        resMap = objectMapper.readValue(sRes.getResponse().getContentAsString(), new TypeReference<Map<String, Object>>() {});
        studentToken = "Bearer " + ((Map<String, Object>)resMap.get("data")).get("token");
        
        // Set IDs for tests
        teacherId = tUser.getId();
        studentId = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, sName)).getId();
    }

    @Test
    public void testCourseAndTaskFlow() throws Exception {
        // 1. Teacher creates a course
        Map<String, Object> courseParams = new HashMap<>();
        courseParams.put("teacherId", teacherId);
        courseParams.put("name", "Test Course");
        courseParams.put("description", "Test Description");
        courseParams.put("semester", "2025 Spring");
        
        MvcResult cRes = mockMvc.perform(post("/api/course/create")
                .header("Authorization", teacherToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(courseParams)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn();
        
        Map<String, Object> cMap = objectMapper.readValue(cRes.getResponse().getContentAsString(), new TypeReference<Map<String, Object>>() {});
        Long courseId = Long.valueOf(((Map<String, Object>)cMap.get("data")).get("id").toString());
        String joinCode = ((Map<String, Object>)cMap.get("data")).get("code").toString();

        // 2. Student joins the course
        Map<String, Object> joinParams = new HashMap<>();
        joinParams.put("studentId", studentId);
        joinParams.put("code", joinCode);
        
        mockMvc.perform(post("/api/course/join")
                .header("Authorization", studentToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(joinParams)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        // 3. Teacher creates a task
        Task task = new Task();
        task.setCourseId(courseId);
        task.setTitle("Test Task");
        task.setContent("Task Content");
        task.setDeadline(LocalDateTime.now().plusDays(1));
        task.setRewardPoints(10);
        task.setSubmitType("TEXT");
        task.setStatus(1); // Set to PUBLISHED
        
        mockMvc.perform(post("/api/tasks/create")
                .header("Authorization", teacherToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(task)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        // 4. Student views tasks
        MvcResult tListRes = mockMvc.perform(get("/api/tasks/student")
                .header("Authorization", studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andReturn();
        
        Map<String, Object> tListMap = objectMapper.readValue(tListRes.getResponse().getContentAsString(), new TypeReference<Map<String, Object>>() {});
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> tasks = (List<Map<String, Object>>) tListMap.get("data");
        // Accessing "task" object within the list element
        Map<String, Object> taskData = (Map<String, Object>) tasks.get(0).get("task");
        Long taskId = Long.valueOf(taskData.get("id").toString());

        // 5. Student submits task
        Map<String, String> submission = new HashMap<>();
        submission.put("content", "This is my task submission content.");
        
        mockMvc.perform(post("/api/tasks/" + taskId + "/submit")
                .header("Authorization", studentToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(submission)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        // 6. Check leaderboard
        mockMvc.perform(get("/api/tasks/" + taskId + "/leaderboard")
                .header("Authorization", studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }
}
