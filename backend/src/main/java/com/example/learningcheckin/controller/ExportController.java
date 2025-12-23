package com.example.learningcheckin.controller;

import cn.hutool.core.io.IoUtil;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.example.learningcheckin.dto.CourseStudentDTO;
import com.example.learningcheckin.service.ICourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/export")
@CrossOrigin
public class ExportController {

    @Autowired
    private ICourseService courseService;

    @GetMapping("/course/{courseId}")
    public void exportCourseData(@PathVariable Long courseId, HttpServletResponse response) throws IOException {
        // 1. Prepare Data
        List<CourseStudentDTO> students = courseService.getCourseStudentDetails(courseId);
        
        List<Map<String, Object>> rows = new ArrayList<>();
        for (CourseStudentDTO s : students) {
            // Get additional stats if needed, e.g., completion rate
            // Ideally, we should have a unified stats service, but for now, let's just export basic info + points
            
            rows.add(cn.hutool.core.map.MapUtil.<String, Object>builder()
                    .put("学号/用户名", s.getUsername())
                    .put("邮箱", s.getEmail())
                    .put("积分", s.getPoints())
                    .put("加入时间", s.getJoinTime().toString())
                    .put("状态", s.getStatus() == 0 ? "正常" : "禁入")
                    .build());
        }

        // 2. Write to Excel using Hutool
        ExcelWriter writer = ExcelUtil.getWriter();
        writer.addHeaderAlias("学号/用户名", "学号/用户名");
        writer.addHeaderAlias("邮箱", "邮箱");
        writer.addHeaderAlias("积分", "积分");
        writer.addHeaderAlias("加入时间", "加入时间");
        writer.addHeaderAlias("状态", "状态");
        
        writer.write(rows, true);

        // 3. Set Response
        response.setContentType("application/vnd.ms-excel;charset=utf-8");
        String fileName = URLEncoder.encode("课程数据导出", "UTF-8");
        response.setHeader("Content-Disposition", "attachment;filename=" + fileName + ".xlsx");

        ServletOutputStream out = response.getOutputStream();
        writer.flush(out, true);
        writer.close();
        IoUtil.close(out);
    }
}
