package com.example.learningcheckin.controller;

import com.example.learningcheckin.common.Result;
import com.example.learningcheckin.entity.User;
import com.example.learningcheckin.service.IFriendService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/friend")
@CrossOrigin
public class FriendController {

    @Autowired
    private IFriendService friendService;

    @PostMapping("/request")
    public Result<String> sendRequest(@RequestBody Map<String, Object> body) {
        Long userId = Long.valueOf(body.get("userId").toString());
        Long targetUserId = Long.valueOf(body.get("targetUserId").toString());
        return friendService.sendFriendRequest(userId, targetUserId);
    }

    @PostMapping("/handle")
    public Result<String> handleRequest(@RequestBody Map<String, Object> body) {
        Long userId = Long.valueOf(body.get("userId").toString());
        Long requestId = Long.valueOf(body.get("requestId").toString());
        Integer status = Integer.valueOf(body.get("status").toString());
        return friendService.handleFriendRequest(userId, requestId, status);
    }

    @DeleteMapping("/delete")
    public Result<String> deleteFriend(@RequestBody Map<String, Object> body) {
        Long userId = Long.valueOf(body.get("userId").toString());
        Long friendId = Long.valueOf(body.get("friendId").toString());
        return friendService.deleteFriend(userId, friendId);
    }

    @GetMapping("/list")
    public Result<List<Map<String, Object>>> getFriendList(@RequestParam Long userId) {
        return friendService.getFriendList(userId);
    }

    @GetMapping("/requests")
    public Result<List<Map<String, Object>>> getFriendRequests(@RequestParam Long userId) {
        return friendService.getFriendRequests(userId);
    }

    @GetMapping("/search")
    public Result<List<User>> searchUsers(@RequestParam String keyword, @RequestParam Long currentUserId) {
        return friendService.searchUsers(keyword, currentUserId);
    }
}
