package com.example.learningcheckin.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.learningcheckin.common.Result;
import com.example.learningcheckin.entity.CommentTemplate;
import com.example.learningcheckin.mapper.CommentTemplateMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/grading/templates")
public class CommentTemplateController {

    @Autowired
    private CommentTemplateMapper commentTemplateMapper;

    @GetMapping
    public Result<List<CommentTemplate>> list(@RequestParam Long teacherId) {
        return Result.success(commentTemplateMapper.selectList(new QueryWrapper<CommentTemplate>().eq("teacher_id", teacherId).orderByDesc("usage_count")));
    }

    @PostMapping
    public Result<CommentTemplate> create(@RequestBody CommentTemplate template) {
        template.setCreateTime(LocalDateTime.now());
        template.setUsageCount(0);
        commentTemplateMapper.insert(template);
        return Result.success(template);
    }

    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        commentTemplateMapper.deleteById(id);
        return Result.success("Deleted successfully");
    }

    @PostMapping("/{id}/use")
    public Result<String> incrementUsage(@PathVariable Long id) {
        CommentTemplate template = commentTemplateMapper.selectById(id);
        if (template != null) {
            template.setUsageCount(template.getUsageCount() + 1);
            commentTemplateMapper.updateById(template);
        }
        return Result.success("Usage count updated");
    }
}
