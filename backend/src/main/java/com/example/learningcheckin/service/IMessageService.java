package com.example.learningcheckin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.learningcheckin.entity.SysNotice;
import com.example.learningcheckin.entity.User;

import java.util.List;

public interface IMessageService extends IService<SysNotice> {
    void sendMessage(Long senderId, Long receiverId, String title, String content, Integer type);
    List<User> getStudentsNeedingReminder(Long courseId, int days);
}
