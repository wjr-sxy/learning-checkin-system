package com.example.learningcheckin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.learningcheckin.common.Result;
import com.example.learningcheckin.entity.SysLog;
import com.example.learningcheckin.entity.User;
import com.example.learningcheckin.mapper.SysLogMapper;
import com.example.learningcheckin.mapper.UserMapper;
import com.example.learningcheckin.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.example.learningcheckin.dto.RegisterRequest;
import com.example.learningcheckin.service.EmailService;
import org.springframework.validation.BindingResult;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin
public class AuthController {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private SysLogMapper sysLogMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private EmailService emailService;

    @GetMapping("/me")
    public Result<User> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return Result.error(401, "未登录");
        }
        
        String username = (String) authentication.getPrincipal();
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (user != null) {
            user.setPassword(null);
            return Result.success(user);
        }
        return Result.error(404, "用户不存在");
    }

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> loginData, HttpServletRequest request) {
        String username = loginData.get("username");
        String password = loginData.get("password");

        SysLog log = new SysLog();
        log.setLogType("LOGIN");
        log.setModule("认证中心");
        log.setAction("用户登录: " + username);
        log.setIp(request.getRemoteAddr());
        log.setCreateTime(LocalDateTime.now());

        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (user == null) {
            log.setStatus(1);
            log.setContent("User not found");
            sysLogMapper.insert(log);
            return Result.error(401, "用户不存在");
        }
        
        log.setUserId(user.getId());

        if (!passwordEncoder.matches(password, user.getPassword())) {
            log.setStatus(1);
            log.setContent("Incorrect password");
            sysLogMapper.insert(log);
            return Result.error(401, "密码错误");
        }
        
        if (user.getStatus() != null && user.getStatus() == 1) {
            log.setStatus(1);
            log.setContent("Account banned");
            sysLogMapper.insert(log);
            return Result.error(403, "账号已被封禁");
        }

        String token = jwtUtils.generateToken(user.getUsername(), user.getRole());
        user.setPassword(null);
        
        log.setStatus(0);
        log.setContent("Login success");
        sysLogMapper.insert(log);

        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("user", user);

        return Result.success(data);
    }

    @PostMapping("/register")
    public Result<String> register(@Valid @RequestBody RegisterRequest registerRequest, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getAllErrors().stream()
                    .map(error -> error.getDefaultMessage())
                    .collect(Collectors.joining(", "));
            return Result.error(400, errorMessage);
        }

        if (userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getUsername, registerRequest.getUsername())) > 0) {
            return Result.error(400, "用户名已存在");
        }

        if (userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getEmail, registerRequest.getEmail())) > 0) {
            return Result.error(400, "邮箱已注册");
        }

        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setEmail(registerRequest.getEmail());
        user.setAvatar(registerRequest.getAvatar());
        user.setPoints(0);
        user.setRole("USER");
        
        try {
            userMapper.insert(user);
        } catch (Exception e) {
            // Handle concurrent insertion issues or DB constraints
            if (e.getMessage().contains("Duplicate entry")) {
                 return Result.error(400, "用户已存在");
            }
            throw e;
        }

        // Send welcome email asynchronously (in a real app, use @Async)
        // For now we just call it directly but it simulates sending
        emailService.sendWelcomeEmail(user.getEmail(), user.getUsername());

        return Result.success("注册成功");
    }
}
