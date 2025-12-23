package com.example.learningcheckin;

import com.example.learningcheckin.dto.RegisterRequest;
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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserMapper userMapper;

    private String studentToken;

    @BeforeEach
    public void setup() throws Exception {
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
        Map<String, Object> resMap = objectMapper.readValue(sRes.getResponse().getContentAsString(), new TypeReference<Map<String, Object>>() {});
        studentToken = "Bearer " + ((Map<String, Object>)resMap.get("data")).get("token");
    }

    @Test
    public void testStudentCannotCreateCourse() throws Exception {
        Map<String, Object> courseParams = new HashMap<>();
        courseParams.put("name", "Illegal Course");
        
        // This should ideally return 403, but let's check current implementation.
        // If it's only checked in frontend, backend might allow it unless we added role checks.
        // Let's see what happens.
        mockMvc.perform(post("/api/course/create")
                .header("Authorization", studentToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(courseParams)))
                .andExpect(status().isForbidden()); 
    }

    @Test
    public void testUnauthenticatedCannotAccessTasks() throws Exception {
        mockMvc.perform(get("/api/tasks/student"))
                .andExpect(status().isForbidden()); // Spring Security default for unauthenticated
    }

    @Test
    public void testStudentCannotAccessAdminLogs() throws Exception {
        mockMvc.perform(get("/api/admin/system/logs/login")
                .header("Authorization", studentToken))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testStudentCannotCreateTask() throws Exception {
        Map<String, Object> taskParams = new HashMap<>();
        taskParams.put("title", "Illegal Task");
        taskParams.put("courseId", 1L);

        mockMvc.perform(post("/api/tasks/create")
                .header("Authorization", studentToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(taskParams)))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testTeacherCanCreateCourse() throws Exception {
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
        String teacherToken = "Bearer " + ((Map<String, Object>)resMap.get("data")).get("token");

        Map<String, Object> courseParams = new HashMap<>();
        courseParams.put("teacherId", tUser.getId());
        courseParams.put("name", "Teacher Course");
        courseParams.put("description", "Desc");
        courseParams.put("semester", "2025");

        mockMvc.perform(post("/api/course/create")
                .header("Authorization", teacherToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(courseParams)))
                .andExpect(status().isOk());
    }
}
