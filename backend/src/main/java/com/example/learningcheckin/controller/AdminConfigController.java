package com.example.learningcheckin.controller;

import com.example.learningcheckin.common.Result;
import com.example.learningcheckin.service.IConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/config")
public class AdminConfigController {

    @Autowired
    private IConfigService configService;

    @GetMapping("/points-multiplier")
    public Result<Double> getPointsMultiplier() {
        return Result.success(configService.getPointsMultiplier());
    }

    @PostMapping("/points-multiplier")
    public Result<String> setPointsMultiplier(@RequestBody Map<String, Double> payload) {
        Double multiplier = payload.get("multiplier");
        if (multiplier == null || multiplier < 0.1 || multiplier > 5.0) {
            return Result.error(400, "Invalid multiplier (0.1 - 5.0)");
        }
        configService.setPointsMultiplier(multiplier);
        return Result.success("Updated successfully");
    }
}
