package com.example.learningcheckin.controller;

import com.example.learningcheckin.common.Result;
import com.example.learningcheckin.entity.Product;
import com.example.learningcheckin.entity.SysLog;
import com.example.learningcheckin.entity.User;
import com.example.learningcheckin.mapper.SysLogMapper;
import com.example.learningcheckin.mapper.UserMapper;
import com.example.learningcheckin.service.IProductService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/user")
@CrossOrigin
public class UserController {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private SysLogMapper sysLogMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private IProductService productService;

    @PostMapping("/equip")
    public Result<String> equipDecoration(@RequestBody Map<String, Object> body) {
        Long userId = Long.valueOf(body.get("userId").toString());
        Long productId = Long.valueOf(body.get("productId").toString());
        String type = (String) body.get("type"); // "avatar", "skin", "background"
        
        // Validate ownership and expiration
        List<Product> owned = productService.getOwnedProducts(userId);
        Product target = owned.stream().filter(p -> p.getId().equals(productId)).findFirst().orElse(null);
        
        if (target == null) {
            return Result.error(400, "You do not own this product.");
        }
        
        if (target.getExpireTime() != null && target.getExpireTime().isBefore(LocalDateTime.now())) {
             return Result.error(400, "This product has expired.");
        }

        User user = userMapper.selectById(userId);
        if ("avatar".equals(type)) {
            user.setCurrentAvatarFrame(target.getImageUrl());
        } else if ("skin".equals(type)) {
            user.setCurrentSkin(target.getImageUrl());
        } else if ("background".equals(type)) {
            String bgUrl = target.getVideoUrl() != null && !target.getVideoUrl().isEmpty() 
                    ? target.getVideoUrl() 
                    : target.getImageUrl();
            user.setProfileBackground(bgUrl);
        } else {
             return Result.error(400, "Invalid type");
        }
        
        userMapper.updateById(user);
        return Result.success("Equipped successfully");
    }

    @PutMapping("/profile")
    public Result<User> updateProfile(@RequestBody Map<String, Object> body) {
        Long userId = Long.valueOf(body.get("id").toString());
        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.error(404, "User not found");
        }

        if (body.containsKey("email")) {
            user.setEmail((String) body.get("email"));
        }
        if (body.containsKey("avatar")) {
            user.setAvatar((String) body.get("avatar"));
        }
        if (body.containsKey("fullName")) {
            String fullName = (String) body.get("fullName");
            if (fullName.length() > 20) {
                return Result.error(400, "姓名长度限制20字符");
            }
            user.setFullName(fullName);
        }
        if (body.containsKey("college")) {
            user.setCollege((String) body.get("college"));
        }
        if (body.containsKey("phone")) {
            String phone = (String) body.get("phone");
            if (!Pattern.matches("^1[3-9]\\d{9}$", phone)) {
                return Result.error(400, "手机号格式不正确");
            }
            user.setPhone(phone);
        }
        if (body.containsKey("allowFriendAdd")) {
            user.setAllowFriendAdd(Boolean.valueOf(body.get("allowFriendAdd").toString()));
        }
        
        userMapper.updateById(user);
        user.setPassword(null);
        return Result.success(user);
    }

    @PutMapping("/password")
    public Result<String> updatePassword(@RequestBody Map<String, String> body) {
        Long userId = Long.valueOf(body.get("id"));
        String oldPassword = body.get("oldPassword");
        String newPassword = body.get("newPassword");

        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.error(404, "User not found");
        }

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            return Result.error(400, "Old password incorrect");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(user);

        return Result.success("Password updated successfully");
    }

    @GetMapping("/login-logs")
    public Result<List<SysLog>> getLoginLogs(@RequestParam Long userId) {
        return Result.success(sysLogMapper.selectList(new LambdaQueryWrapper<SysLog>()
                .eq(SysLog::getUserId, userId)
                .eq(SysLog::getLogType, "LOGIN")
                .orderByDesc(SysLog::getCreateTime)));
    }

    @GetMapping("/operation-logs")
    public Result<List<SysLog>> getOperationLogs(
            @RequestParam Long userId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String module) {
        
        LambdaQueryWrapper<SysLog> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysLog::getUserId, userId)
                .eq(SysLog::getLogType, "OPERATION");
        
        if (startDate != null && !startDate.isEmpty()) {
            queryWrapper.ge(SysLog::getCreateTime, startDate + " 00:00:00");
        }
        if (endDate != null && !endDate.isEmpty()) {
            queryWrapper.le(SysLog::getCreateTime, endDate + " 23:59:59");
        }
        if (module != null && !module.isEmpty()) {
            queryWrapper.eq(SysLog::getModule, module);
        }
        
        queryWrapper.orderByDesc(SysLog::getCreateTime);
        return Result.success(sysLogMapper.selectList(queryWrapper));
    }
}
