package com.example.learningcheckin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.learningcheckin.common.Result;
import com.example.learningcheckin.entity.FriendRequest;
import com.example.learningcheckin.entity.Friendship;
import com.example.learningcheckin.entity.User;
import com.example.learningcheckin.mapper.FriendRequestMapper;
import com.example.learningcheckin.mapper.FriendshipMapper;
import com.example.learningcheckin.mapper.UserMapper;
import com.example.learningcheckin.service.ICheckinService;
import com.example.learningcheckin.service.IFriendService;
import com.example.learningcheckin.websocket.NotificationWebSocket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class FriendServiceImpl implements IFriendService {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private FriendshipMapper friendshipMapper;
    @Autowired
    private FriendRequestMapper friendRequestMapper;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private ICheckinService checkinService;

    @Override
    @Transactional
    public Result<String> sendFriendRequest(Long userId, Long targetUserId) {
        // Frequency limit: 5 requests per minute
        String key = "friend:request:limit:" + userId;
        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null && count == 1) {
            redisTemplate.expire(key, 1, TimeUnit.MINUTES);
        }
        if (count != null && count > 5) {
            return Result.error(500, "Too many requests. Please try again later.");
        }

        if (userId.equals(targetUserId)) {
            return Result.error(500, "Cannot add yourself");
        }
        User target = userMapper.selectById(targetUserId);
        if (target == null) {
            return Result.error(500, "User not found");
        }
        if (target.getAllowFriendAdd() != null && !target.getAllowFriendAdd()) {
            return Result.error(500, "User does not accept friend requests");
        }

        // Check if already friends
        Long friendCount = friendshipMapper.selectCount(new LambdaQueryWrapper<Friendship>()
                .eq(Friendship::getUserId, userId)
                .eq(Friendship::getFriendId, targetUserId));
        if (friendCount > 0) {
            return Result.error(500, "Already friends");
        }

        // Check pending request
        Long pending = friendRequestMapper.selectCount(new LambdaQueryWrapper<FriendRequest>()
                .eq(FriendRequest::getSenderId, userId)
                .eq(FriendRequest::getReceiverId, targetUserId)
                .eq(FriendRequest::getStatus, 0));
        if (pending > 0) {
            return Result.error(500, "Request already sent");
        }

        FriendRequest req = new FriendRequest();
        req.setSenderId(userId);
        req.setReceiverId(targetUserId);
        req.setStatus(0);
        req.setCreateTime(LocalDateTime.now());
        friendRequestMapper.insert(req);

        // Notify
        NotificationWebSocket.sendInfo(targetUserId, "{\"type\":\"FRIEND_REQUEST\", \"from\":\"" + userId + "\"}");

        return Result.success("Request sent");
    }

    @Override
    @Transactional
    public Result<String> handleFriendRequest(Long userId, Long requestId, Integer status) {
        FriendRequest req = friendRequestMapper.selectById(requestId);
        if (req == null || !req.getReceiverId().equals(userId)) {
            return Result.error(500, "Request not found");
        }
        if (req.getStatus() != 0) {
            return Result.error(500, "Request already handled");
        }

        req.setStatus(status); // 1: Accept, 2: Reject
        friendRequestMapper.updateById(req);

        if (status == 1) {
            // Create friendship (bi-directional)
            Friendship f1 = new Friendship();
            f1.setUserId(userId);
            f1.setFriendId(req.getSenderId());
            f1.setCreateTime(LocalDateTime.now());
            friendshipMapper.insert(f1);

            Friendship f2 = new Friendship();
            f2.setUserId(req.getSenderId());
            f2.setFriendId(userId);
            f2.setCreateTime(LocalDateTime.now());
            friendshipMapper.insert(f2);
            
            NotificationWebSocket.sendInfo(req.getSenderId(), "{\"type\":\"FRIEND_ACCEPT\", \"from\":\"" + userId + "\"}");
        }

        return Result.success("Handled");
    }

    @Override
    @Transactional
    public Result<String> deleteFriend(Long userId, Long friendId) {
        friendshipMapper.delete(new LambdaQueryWrapper<Friendship>()
                .eq(Friendship::getUserId, userId).eq(Friendship::getFriendId, friendId));
        friendshipMapper.delete(new LambdaQueryWrapper<Friendship>()
                .eq(Friendship::getUserId, friendId).eq(Friendship::getFriendId, userId));
        return Result.success("Deleted");
    }

    @Override
    public Result<List<Map<String, Object>>> getFriendList(Long userId) {
        List<Friendship> list = friendshipMapper.selectList(new LambdaQueryWrapper<Friendship>()
                .eq(Friendship::getUserId, userId));
        
        if (list.isEmpty()) return Result.success(new ArrayList<>());
        
        List<Long> friendIds = list.stream().map(Friendship::getFriendId).collect(Collectors.toList());
        List<User> users = userMapper.selectBatchIds(friendIds);
        
        List<Map<String, Object>> result = users.stream().map(u -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", u.getId());
            map.put("username", u.getUsername());
            map.put("fullName", u.getFullName());
            map.put("college", u.getCollege());
            map.put("avatar", u.getAvatar());
            map.put("currentAvatarFrame", u.getCurrentAvatarFrame());
            map.put("isCheckedIn", checkinService.isCheckedIn(u.getId(), LocalDate.now()));
            
            boolean isOnline = false;
            if (u.getLastActiveTime() != null) {
                isOnline = u.getLastActiveTime().isAfter(LocalDateTime.now().minusMinutes(5));
            }
            map.put("isOnline", isOnline);
            map.put("online", isOnline);
            map.put("lastActiveTime", u.getLastActiveTime());
            return map;
        }).collect(Collectors.toList());
        
        return Result.success(result);
    }

    @Override
    public Result<List<Map<String, Object>>> getFriendRequests(Long userId) {
        List<FriendRequest> requests = friendRequestMapper.selectList(new LambdaQueryWrapper<FriendRequest>()
                .eq(FriendRequest::getReceiverId, userId)
                .eq(FriendRequest::getStatus, 0)
                .orderByDesc(FriendRequest::getCreateTime));

        if (requests.isEmpty()) return Result.success(new ArrayList<>());

        List<Long> senderIds = requests.stream().map(FriendRequest::getSenderId).collect(Collectors.toList());
        Map<Long, User> senders = userMapper.selectBatchIds(senderIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        List<Map<String, Object>> result = requests.stream().map(r -> {
            User u = senders.get(r.getSenderId());
            if (u == null) return null;
            Map<String, Object> map = new HashMap<>();
            map.put("requestId", r.getId());
            map.put("senderId", u.getId());
            map.put("username", u.getUsername());
            map.put("fullName", u.getFullName());
            map.put("avatar", u.getAvatar());
            map.put("createTime", r.getCreateTime());
            return map;
        }).filter(java.util.Objects::nonNull).collect(Collectors.toList());

        return Result.success(result);
    }

    @Override
    public Result<List<User>> searchUsers(String keyword, String college, Long currentUserId) {
        List<User> users = userMapper.selectList(new LambdaQueryWrapper<User>()
                .ne(User::getId, currentUserId)
                .and(w -> w.like(User::getUsername, keyword)
                        .or().like(User::getFullName, keyword)
                        .or().like(User::getCollege, keyword))
                .eq(college != null && !college.isEmpty(), User::getCollege, college)
                .last("LIMIT 20"));
        
        // Mask sensitive info
        users.forEach(u -> {
            u.setPassword(null);
            u.setPoints(null);
        });
        
        return Result.success(users);
    }

    @Override
    public Result<String> remindFriend(Long userId, Long friendId) {
        // 1. Check friendship
        Long count = friendshipMapper.selectCount(new LambdaQueryWrapper<Friendship>()
                .eq(Friendship::getUserId, userId)
                .eq(Friendship::getFriendId, friendId));
        if (count == 0) return Result.error(400, "Not friends");

        // 2. Check if friend checked in
        boolean checkedIn = checkinService.isCheckedIn(friendId, LocalDate.now());
        if (checkedIn) return Result.error(400, "Friend already checked in today");

        // 3. Rate limit (Redis)
        String key = "friend:remind:" + userId + ":" + friendId + ":" + LocalDate.now();
        Boolean hasReminded = redisTemplate.hasKey(key);
        if (Boolean.TRUE.equals(hasReminded)) {
             return Result.error(400, "Already reminded today");
        }

        // 4. Send Notification
        User sender = userMapper.selectById(userId);
        String senderName = (sender.getFullName() != null && !sender.getFullName().isEmpty()) 
                            ? sender.getFullName() : sender.getUsername();
        
        String msg = String.format("{\"type\":\"ALERT\", \"title\":\"好友提醒\", \"content\":\"您的好友 [%s] 提醒您今天还没打卡，快去学习吧！\"}", senderName);
        NotificationWebSocket.sendInfo(friendId, msg);
        
        // Set Redis flag (expire in 24h)
        redisTemplate.opsForValue().set(key, "1", 24, TimeUnit.HOURS);

        return Result.success("Reminded successfully");
    }
}