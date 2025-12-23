package com.example.learningcheckin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.learningcheckin.common.Result;
import com.example.learningcheckin.entity.Blacklist;
import com.example.learningcheckin.entity.SysLog;
import com.example.learningcheckin.mapper.BlacklistMapper;
import com.example.learningcheckin.mapper.SysLogMapper;
import com.example.learningcheckin.service.ISensitiveWordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/content")
public class AdminContentController {

    @Autowired
    private ISensitiveWordService sensitiveWordService;

    @Autowired
    private BlacklistMapper blacklistMapper;

    @Autowired
    private SysLogMapper sysLogMapper;

    @PostMapping("/sensitive/import")
    public ResponseEntity<?> importSensitiveWords(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("File is empty");
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            List<String> words = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    words.add(line.trim());
                }
            }
            sensitiveWordService.importWords(words);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Successfully imported " + words.size() + " words");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error processing file: " + e.getMessage());
        }
    }

    @GetMapping("/sensitive/export")
    public ResponseEntity<?> exportSensitiveWords() {
        List<Blacklist> list = blacklistMapper.selectList(new LambdaQueryWrapper<Blacklist>()
                .eq(Blacklist::getType, "SENSITIVE_WORD"));

        StringBuilder sb = new StringBuilder();
        for (Blacklist item : list) {
            sb.append(item.getValue()).append("\n");
        }

        byte[] bytes = sb.toString().getBytes(StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=sensitive_words.txt")
                .contentType(MediaType.TEXT_PLAIN)
                .body(bytes);
    }

    @GetMapping("/sensitive/logs")
    public Result<IPage<SysLog>> getSensitiveLogs(@RequestParam(defaultValue = "1") Integer page,
                                            @RequestParam(defaultValue = "10") Integer size) {
        Page<SysLog> p = new Page<>(page, size);
        IPage<SysLog> result = sysLogMapper.selectPage(p, new LambdaQueryWrapper<SysLog>()
                .eq(SysLog::getLogType, "SENSITIVE")
                .orderByDesc(SysLog::getCreateTime));
        return Result.success(result);
    }
}
