package com.example.learningcheckin.service;

import com.example.learningcheckin.common.Result;
import com.example.learningcheckin.entity.User;
import java.util.List;
import java.util.Map;

public interface IFriendService {
    Result<String> sendFriendRequest(Long userId, Long targetUserId);
    Result<String> handleFriendRequest(Long userId, Long requestId, Integer status); // 1: Accept, 2: Reject
    Result<String> deleteFriend(Long userId, Long friendId);
    Result<List<Map<String, Object>>> getFriendList(Long userId);
    Result<List<Map<String, Object>>> getFriendRequests(Long userId);
    Result<List<User>> searchUsers(String keyword, String college, Long currentUserId);
    Result<String> remindFriend(Long userId, Long friendId);
}
