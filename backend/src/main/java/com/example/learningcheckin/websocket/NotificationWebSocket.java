package com.example.learningcheckin.websocket;

import com.example.learningcheckin.utils.JwtUtils;
import com.example.learningcheckin.entity.User;
import com.example.learningcheckin.mapper.UserMapper;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.Map;
import java.util.HashMap;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

@ServerEndpoint(value = "/ws/notification")
@Component
public class NotificationWebSocket {

    private static ConcurrentHashMap<Long, CopyOnWriteArraySet<Session>> sessionPool = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<Long, User> userPool = new ConcurrentHashMap<>();
    
    private static ApplicationContext applicationContext;

    public static void setApplicationContext(ApplicationContext context) {
        applicationContext = context;
    }

    private Long userId;

    @OnOpen
    public void onOpen(Session session) {
        String queryString = session.getQueryString();
        if (queryString != null && queryString.contains("token=")) {
            String token = "";
            String[] params = queryString.split("&");
            for (String param : params) {
                if (param.startsWith("token=")) {
                    token = param.substring(6);
                    break;
                }
            }
            
            if (!token.isEmpty()) {
                JwtUtils jwtUtils = applicationContext.getBean(JwtUtils.class);
                if (jwtUtils.validateToken(token)) {
                    String username = jwtUtils.getUsernameFromToken(token);
                    UserMapper userMapper = applicationContext.getBean(UserMapper.class);
                    User user = userMapper.selectOne(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>().eq(User::getUsername, username));
                    if (user != null) {
                        this.userId = user.getId();
                        sessionPool.computeIfAbsent(this.userId, k -> new CopyOnWriteArraySet<>()).add(session);
                        userPool.put(this.userId, user);
                        return;
                    }
                }
            }
        }
        // If auth fails, close
        try {
            session.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @OnClose
    public void onClose(Session session) {
        if (this.userId != null) {
            CopyOnWriteArraySet<Session> sessions = sessionPool.get(this.userId);
            if (sessions != null) {
                sessions.remove(session);
                if (sessions.isEmpty()) {
                    sessionPool.remove(this.userId);
                    userPool.remove(this.userId);
                }
            }
        }
    }

    @OnError
    public void onError(Session session, Throwable error) {
        error.printStackTrace();
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> msgMap = mapper.readValue(message, new TypeReference<Map<String, Object>>() {});
            String type = (String) msgMap.get("type");
            
            if ("CHAT".equals(type)) {
                String content = (String) msgMap.get("content");
                if (content != null && !content.trim().isEmpty()) {
                    User sender = userPool.get(this.userId);
                    if (sender != null) {
                        broadcastChat(sender, content);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void broadcastChat(User sender, String content) {
        Map<String, Object> chatMsg = new HashMap<>();
        chatMsg.put("type", "CHAT");
        chatMsg.put("senderId", sender.getId());
        chatMsg.put("senderName", sender.getFullName() != null ? sender.getFullName() : sender.getUsername());
        chatMsg.put("senderRole", sender.getRole());
        chatMsg.put("senderAvatar", sender.getAvatar());
        chatMsg.put("content", content);
        chatMsg.put("time", java.time.LocalDateTime.now().toString());
        
        try {
            String jsonMsg = new ObjectMapper().writeValueAsString(chatMsg);
            for (CopyOnWriteArraySet<Session> sessions : sessionPool.values()) {
                for (Session s : sessions) {
                    if (s.isOpen()) {
                        try {
                            s.getBasicRemote().sendText(jsonMsg);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void sendInfo(Long userId, String message) {
        CopyOnWriteArraySet<Session> sessions = sessionPool.get(userId);
        if (sessions != null) {
            for (Session session : sessions) {
                if (session.isOpen()) {
                    try {
                        session.getBasicRemote().sendText(message);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public static java.util.Set<Long> getOnlineUserIds() {
        return sessionPool.keySet();
    }
}
