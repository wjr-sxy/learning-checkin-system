package com.example.learningcheckin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.learningcheckin.entity.Checkin;
import com.example.learningcheckin.entity.CourseStudent;
import com.example.learningcheckin.entity.SysNotice;
import com.example.learningcheckin.entity.User;
import com.example.learningcheckin.mapper.CheckinMapper;
import com.example.learningcheckin.mapper.CourseStudentMapper;
import com.example.learningcheckin.mapper.SysNoticeMapper;
import com.example.learningcheckin.mapper.UserMapper;
import com.example.learningcheckin.service.IMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MessageServiceImpl extends ServiceImpl<SysNoticeMapper, SysNotice> implements IMessageService {

    @Autowired
    private CourseStudentMapper courseStudentMapper;

    @Autowired
    private CheckinMapper checkinMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    public void sendMessage(Long senderId, Long receiverId, String title, String content, Integer type) {
        SysNotice message = new SysNotice();
        message.setSenderId(senderId);
        message.setReceiverId(receiverId);
        message.setTitle(title);
        message.setContent(content);
        message.setType("MESSAGE"); // Using string "MESSAGE" for consistency in SysNotice
        message.setIsRead(0);
        message.setCreateTime(LocalDateTime.now());
        this.save(message);
    }

    @Override
    public List<User> getStudentsNeedingReminder(Long courseId, int days) {
        // 1. Get all active students in course
        List<CourseStudent> students = courseStudentMapper.selectList(new LambdaQueryWrapper<CourseStudent>()
                .eq(CourseStudent::getCourseId, courseId)
                .eq(CourseStudent::getStatus, 0));
        
        if (students.isEmpty()) {
            return new ArrayList<>();
        }

        List<Long> studentIds = students.stream().map(CourseStudent::getStudentId).collect(Collectors.toList());
        
        // 2. Find students who have checked in within last 'days'
        LocalDate thresholdDate = LocalDate.now().minusDays(days);
        
        // Optimizing: Select distinct userId from Checkin where date >= threshold and userId in studentIds
        List<Checkin> recentCheckins = checkinMapper.selectList(new LambdaQueryWrapper<Checkin>()
                .in(Checkin::getUserId, studentIds)
                .ge(Checkin::getCheckinDate, thresholdDate)
                .select(Checkin::getUserId)); // Select only ID for optimization? wrapper supports select
        
        List<Long> activeStudentIds = recentCheckins.stream()
                .map(Checkin::getUserId)
                .distinct()
                .collect(Collectors.toList());
        
        // 3. Filter: Students NOT in activeStudentIds
        List<Long> lazyStudentIds = studentIds.stream()
                .filter(id -> !activeStudentIds.contains(id))
                .collect(Collectors.toList());

        if (lazyStudentIds.isEmpty()) {
            return new ArrayList<>();
        }

        return userMapper.selectBatchIds(lazyStudentIds);
    }
}
