package com.example.learningcheckin.controller;

import cn.hutool.core.text.csv.CsvUtil;
import cn.hutool.core.text.csv.CsvWriter;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.learningcheckin.annotation.Log;
import com.example.learningcheckin.common.Result;
import com.example.learningcheckin.entity.Blacklist;
import com.example.learningcheckin.entity.SysLog;
import com.example.learningcheckin.entity.SysNotice;
import com.example.learningcheckin.mapper.BlacklistMapper;
import com.example.learningcheckin.mapper.SysLogMapper;
import com.example.learningcheckin.mapper.SysNoticeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/system")
public class AdminSystemController {

    @Autowired
    private SysNoticeMapper sysNoticeMapper;

    @Autowired
    private BlacklistMapper blacklistMapper;

    @Autowired
    private SysLogMapper sysLogMapper;

    // Announcements
    @GetMapping("/announcements")
    public Result<Page<SysNotice>> listAnnouncements(@RequestParam(defaultValue = "1") Integer page,
                                                        @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(sysNoticeMapper.selectPage(new Page<>(page, size), 
                new LambdaQueryWrapper<SysNotice>()
                        .eq(SysNotice::getType, "ANNOUNCEMENT")
                        .orderByDesc(SysNotice::getCreateTime)));
    }

    @PostMapping("/announcements")
    @Log(module = "系统工具", action = "保存公告")
    public Result<String> saveAnnouncement(@RequestBody SysNotice announcement) {
        announcement.setType("ANNOUNCEMENT");
        if (announcement.getId() == null) {
            announcement.setCreateTime(LocalDateTime.now());
            sysNoticeMapper.insert(announcement);
        } else {
            sysNoticeMapper.updateById(announcement);
        }
        return Result.success("Saved successfully");
    }

    @DeleteMapping("/announcements/{id}")
    @Log(module = "系统工具", action = "删除公告")
    public Result<String> deleteAnnouncement(@PathVariable Long id) {
        sysNoticeMapper.deleteById(id);
        return Result.success("Deleted successfully");
    }

    // Blacklist
    @GetMapping("/blacklist")
    public Result<Page<Blacklist>> listBlacklist(@RequestParam(defaultValue = "1") Integer page,
                                                 @RequestParam(defaultValue = "10") Integer size,
                                                 @RequestParam(required = false) String type) {
        LambdaQueryWrapper<Blacklist> wrapper = new LambdaQueryWrapper<>();
        if (type != null && !type.isEmpty()) {
            wrapper.eq(Blacklist::getType, type);
        }
        wrapper.orderByDesc(Blacklist::getCreateTime);
        return Result.success(blacklistMapper.selectPage(new Page<>(page, size), wrapper));
    }

    @PostMapping("/blacklist")
    @Log(module = "系统工具", action = "添加黑名单")
    public Result<String> addBlacklist(@RequestBody Blacklist blacklist) {
        blacklist.setCreateTime(LocalDateTime.now());
        blacklistMapper.insert(blacklist);
        return Result.success("Added to blacklist");
    }

    @DeleteMapping("/blacklist/{id}")
    @Log(module = "系统工具", action = "删除黑名单")
    public Result<String> deleteBlacklist(@PathVariable Long id) {
        blacklistMapper.deleteById(id);
        return Result.success("Removed from blacklist");
    }

    // Logs
    @GetMapping("/logs/operation")
    public Result<Page<SysLog>> listOperationLogs(@RequestParam(defaultValue = "1") Integer page,
                                                        @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(sysLogMapper.selectPage(new Page<>(page, size), 
                new LambdaQueryWrapper<SysLog>()
                        .eq(SysLog::getLogType, "OPERATION")
                        .orderByDesc(SysLog::getCreateTime)));
    }

    @GetMapping("/logs/operation/export")
    public void exportOperationLogs(HttpServletResponse response) throws IOException {
        response.setContentType("text/csv;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=operation_logs.csv");
        
        List<SysLog> list = sysLogMapper.selectList(new LambdaQueryWrapper<SysLog>()
                .eq(SysLog::getLogType, "OPERATION")
                .orderByDesc(SysLog::getCreateTime));
        
        CsvWriter writer = CsvUtil.getWriter(response.getWriter());
        writer.write(new String[]{"ID", "User ID", "Module", "Action", "Content", "IP", "Status", "Execution Time (ms)", "Create Time"});
        for (SysLog log : list) {
            writer.write(new String[]{
                String.valueOf(log.getId()),
                String.valueOf(log.getUserId()),
                log.getModule(),
                log.getAction(),
                log.getContent(),
                log.getIp(),
                log.getStatus() == 0 ? "Success" : "Fail",
                String.valueOf(log.getExecutionTime()),
                String.valueOf(log.getCreateTime())
            });
        }
        writer.flush();
        writer.close();
    }

    @GetMapping("/logs/login")
    public Result<Page<SysLog>> listLoginLogs(@RequestParam(defaultValue = "1") Integer page,
                                                @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(sysLogMapper.selectPage(new Page<>(page, size), 
                new LambdaQueryWrapper<SysLog>()
                        .eq(SysLog::getLogType, "LOGIN")
                        .orderByDesc(SysLog::getCreateTime)));
    }

    @GetMapping("/logs/login/export")
    public void exportLoginLogs(HttpServletResponse response) throws IOException {
        response.setContentType("text/csv;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=login_logs.csv");
        
        List<SysLog> list = sysLogMapper.selectList(new LambdaQueryWrapper<SysLog>()
                .eq(SysLog::getLogType, "LOGIN")
                .orderByDesc(SysLog::getCreateTime));
        
        CsvWriter writer = CsvUtil.getWriter(response.getWriter());
        writer.write(new String[]{"ID", "User ID", "Action", "IP", "Status", "Content", "Create Time"});
        for (SysLog log : list) {
            writer.write(new String[]{
                String.valueOf(log.getId()),
                String.valueOf(log.getUserId()),
                log.getAction(),
                log.getIp(),
                log.getStatus() == 0 ? "Success" : "Fail",
                log.getContent(),
                String.valueOf(log.getCreateTime())
            });
        }
        writer.flush();
        writer.close();
    }

    @GetMapping("/health")
    public Result<Map<String, Object>> getSystemHealth() {
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        
        // 1. Login Failures
        Long loginFailures = sysLogMapper.selectCount(new LambdaQueryWrapper<SysLog>()
                .eq(SysLog::getLogType, "LOGIN")
                .eq(SysLog::getStatus, 1)
                .ge(SysLog::getCreateTime, oneHourAgo));

        // 2. Interface Errors
        Long interfaceErrors = sysLogMapper.selectCount(new LambdaQueryWrapper<SysLog>()
                .eq(SysLog::getLogType, "OPERATION")
                .eq(SysLog::getStatus, 1) // 1: Fail
                .ge(SysLog::getCreateTime, oneHourAgo));

        // 3. Slow Queries (Execution time > 1000ms)
        Long slowQueries = sysLogMapper.selectCount(new LambdaQueryWrapper<SysLog>()
                .eq(SysLog::getLogType, "OPERATION")
                .gt(SysLog::getExecutionTime, 1000)
                .ge(SysLog::getCreateTime, oneHourAgo));

        Map<String, Object> health = new HashMap<>();
        health.put("loginFailures", loginFailures);
        health.put("interfaceErrors", interfaceErrors);
        health.put("slowQueries", slowQueries);
        health.put("status", (loginFailures > 5 || interfaceErrors > 10 || slowQueries > 5) ? "Warning" : "Normal");

        return Result.success(health);
    }
}
