package com.example.learningcheckin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.learningcheckin.entity.Checkin;
import java.time.LocalDate;
import java.util.List;

public interface ICheckinService extends IService<Checkin> {
    boolean isCheckedIn(Long userId, LocalDate date);
    Checkin dailyCheckin(Long userId);
    List<Checkin> getHistory(Long userId);
    Checkin reCheckin(Long userId, LocalDate date);
}
