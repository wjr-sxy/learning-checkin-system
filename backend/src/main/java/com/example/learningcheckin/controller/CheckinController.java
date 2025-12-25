package com.example.learningcheckin.controller;

import com.example.learningcheckin.common.Result;
import com.example.learningcheckin.entity.Checkin;
import com.example.learningcheckin.service.ICheckinService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/checkin")
@CrossOrigin
public class CheckinController {

    @Autowired
    private ICheckinService checkinService;

    @GetMapping("/status")
    public Result<Boolean> getCheckinStatus(@RequestParam Long userId) {
        return Result.success(checkinService.isCheckedIn(userId, LocalDate.now()));
    }

    @PostMapping("/daily")
    public Result<Checkin> dailyCheckin(@RequestBody Map<String, Long> data, HttpServletRequest request) {
        Long userId = data.get("userId");
        String ipAddress = getClientIp(request);
        try {
            Checkin checkin = checkinService.dailyCheckin(userId, ipAddress);
            return Result.success(checkin);
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && ip.length() != 0 && !"unknown".equalsIgnoreCase(ip)) {
            // Multiple proxies, the first IP is the real client IP
            if (ip.indexOf(",") != -1) {
                ip = ip.split(",")[0];
            }
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    @PostMapping("/recheckin")
    public Result<Checkin> reCheckin(@RequestBody Map<String, Object> data, HttpServletRequest request) {
        Long userId = Long.valueOf(data.get("userId").toString());
        String dateStr = data.get("date").toString();
        LocalDate date = LocalDate.parse(dateStr);
        String ipAddress = getClientIp(request);
        try {
            Checkin checkin = checkinService.reCheckin(userId, date, ipAddress);
            return Result.success(checkin);
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
    }

    @GetMapping("/history")
    public Result<List<Checkin>> getHistory(@RequestParam Long userId) {
        return Result.success(checkinService.getHistory(userId));
    }
}
