package com.example.learningcheckin.controller;

import cn.hutool.core.text.csv.CsvUtil;
import cn.hutool.core.text.csv.CsvWriter;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.learningcheckin.annotation.Log;
import com.example.learningcheckin.common.Result;
import com.example.learningcheckin.entity.PointsRecord;
import com.example.learningcheckin.entity.User;
import com.example.learningcheckin.mapper.PointsRecordMapper;
import com.example.learningcheckin.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PointsRecordMapper pointsRecordMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping
    public Result<Page<User>> listUsers(@RequestParam(defaultValue = "1") Integer page,
                                        @RequestParam(defaultValue = "10") Integer size,
                                        @RequestParam(required = false) String username) {
        Page<User> userPage = new Page<>(page, size);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (username != null && !username.isEmpty()) {
            wrapper.like(User::getUsername, username);
        }
        wrapper.orderByDesc(User::getCreateTime);
        return Result.success(userMapper.selectPage(userPage, wrapper));
    }

    @GetMapping("/export")
    public void exportUsers(HttpServletResponse response, @RequestParam(required = false) String username) throws IOException {
        response.setContentType("text/csv;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=users.csv");

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (username != null && !username.isEmpty()) {
            wrapper.like(User::getUsername, username);
        }
        wrapper.orderByDesc(User::getCreateTime);
        List<User> list = userMapper.selectList(wrapper);

        CsvWriter writer = CsvUtil.getWriter(response.getWriter());
        writer.write(new String[]{"ID", "Username", "Email", "Role", "Points", "Status", "Continuous Checkin Days", "Last Checkin Date", "Create Time"});
        for (User user : list) {
            writer.write(new String[]{
                String.valueOf(user.getId()),
                user.getUsername(),
                user.getEmail(),
                user.getRole(),
                String.valueOf(user.getPoints()),
                String.valueOf(user.getStatus()),
                String.valueOf(user.getContinuousCheckinDays()),
                String.valueOf(user.getLastCheckinDate()),
                String.valueOf(user.getCreateTime())
            });
        }
        writer.flush();
        writer.close();
    }

    @PostMapping("/{id}/status")
    @Log(module = "用户管理", action = "修改状态")
    public Result<String> updateUserStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        Integer status = body.get("status");
        User user = userMapper.selectById(id);
        if (user == null) {
            return Result.error(404, "User not found");
        }
        user.setStatus(status);
        userMapper.updateById(user);
        return Result.success("User status updated");
    }

    @PostMapping("/{id}/password")
    @Log(module = "用户管理", action = "重置密码")
    public Result<String> resetPassword(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String newPassword = body.get("password");
        User user = userMapper.selectById(id);
        if (user == null) {
            return Result.error(404, "User not found");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(user);
        return Result.success("Password reset successfully");
    }

    @PostMapping("/{id}/points")
    @Transactional
    @Log(module = "用户管理", action = "调整积分")
    public Result<String> adjustPoints(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Integer amount = (Integer) body.get("amount");
        String reason = (String) body.get("reason");
        String typeStr = (String) body.get("type"); // "ADD" or "DEDUCT"

        User user = userMapper.selectById(id);
        if (user == null) {
            return Result.error(404, "User not found");
        }

        if ("DEDUCT".equals(typeStr)) {
            if (user.getPoints() < amount) {
                return Result.error(400, "Insufficient points");
            }
            user.setPoints(user.getPoints() - amount);
        } else {
            user.setPoints(user.getPoints() + amount);
        }
        userMapper.updateById(user);

        // Record
        PointsRecord record = new PointsRecord();
        record.setUserId(id);
        record.setType("DEDUCT".equals(typeStr) ? 2 : 1); // 1: Gain, 2: Consume
        record.setAmount(amount);
        record.setDescription("Admin Adjustment: " + reason);
        record.setCreateTime(LocalDateTime.now());
        pointsRecordMapper.insert(record);

        return Result.success("Points adjusted successfully");
    }
}
