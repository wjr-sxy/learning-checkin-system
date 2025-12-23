package com.example.learningcheckin.scheduler;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.learningcheckin.entity.SysLog;
import com.example.learningcheckin.entity.Order;
import com.example.learningcheckin.entity.Product;
import com.example.learningcheckin.entity.User;
import com.example.learningcheckin.mapper.SysLogMapper;
import com.example.learningcheckin.mapper.OrderMapper;
import com.example.learningcheckin.mapper.ProductMapper;
import com.example.learningcheckin.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class ExpirationScheduler {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private SysLogMapper sysLogMapper;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private static final String LOCK_KEY = "lock:expiration_task";

    @Scheduled(cron = "0 0/5 * * * ?") // Run every 5 minutes
    @Transactional(rollbackFor = Exception.class)
    public void processExpirations() {
        // Distributed Lock: SETNX + EXPIRE
        Boolean locked = stringRedisTemplate.opsForValue().setIfAbsent(LOCK_KEY, "LOCKED", 4, TimeUnit.MINUTES);
        if (!Boolean.TRUE.equals(locked)) {
            log.info("Expiration task is running on another instance.");
            return;
        }

        try {
            log.info("Starting expiration processing...");
            
            // 1. Find all valid orders
            List<Order> validOrders = orderMapper.selectList(new LambdaQueryWrapper<Order>()
                    .eq(Order::getStatus, 0)); // 0: Valid

            LocalDateTime now = LocalDateTime.now();

            for (Order order : validOrders) {
                Product product = productMapper.selectById(order.getProductId());
                if (product == null) continue;

                // Check if product has expiration
                if (product.getDays() != null && product.getDays() > 0) {
                    LocalDateTime expireTime = order.getCreateTime().plusDays(product.getDays());
                    
                    if (expireTime.isBefore(now)) {
                        // Expired!
                        log.info("Order {} (Product {}, User {}) expired at {}", order.getId(), product.getId(), order.getUserId(), expireTime);

                        // 2. Update Order Status to 2 (Expired)
                        order.setStatus(2);
                        orderMapper.updateById(order);

                        // 3. Log Expiration
                        SysLog expLog = new SysLog();
                        expLog.setLogType("EXPIRATION");
                        expLog.setUserId(order.getUserId());
                        expLog.setModule("商城系统");
                        expLog.setAction("商品过期");
                        expLog.setContent("Order " + order.getId() + " (Product " + product.getId() + ") expired");
                        expLog.setExtraInfo(String.format("{\"orderId\":%d, \"productId\":%d}", order.getId(), product.getId()));
                        expLog.setCreateTime(now);
                        sysLogMapper.insert(expLog);

                        // 4. Check if user is currently using this item and un-equip if necessary
                        // Note: User might have another valid order for the same product, so we should check that.
                        checkAndUnequip(order.getUserId(), product);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error during expiration processing", e);
        } finally {
            stringRedisTemplate.delete(LOCK_KEY);
        }
    }

    private void checkAndUnequip(Long userId, Product product) {
        User user = userMapper.selectById(userId);
        if (user == null) return;

        boolean needUpdate = false;

        // Check Avatar Frame
        if (product.getImageUrl() != null && product.getImageUrl().equals(user.getCurrentAvatarFrame())) {
            // Check if user has ANY other valid order for this product
            if (!hasValidOrder(userId, product.getId())) {
                user.setCurrentAvatarFrame(null);
                needUpdate = true;
            }
        }
        
        // Check Skin/Background (Assuming profileBackground uses videoUrl or imageUrl)
        // Check Profile Background
        if (product.getVideoUrl() != null && product.getVideoUrl().equals(user.getProfileBackground())) {
            if (!hasValidOrder(userId, product.getId())) {
                user.setProfileBackground(null);
                needUpdate = true;
            }
        } else if (product.getImageUrl() != null && product.getImageUrl().equals(user.getProfileBackground())) {
            if (!hasValidOrder(userId, product.getId())) {
                user.setProfileBackground(null);
                needUpdate = true;
            }
        }

        if (needUpdate) {
            userMapper.updateById(user);
            log.info("Unequipped expired item for user {}", userId);
        }
    }

    private boolean hasValidOrder(Long userId, Long productId) {
        Long count = orderMapper.selectCount(new LambdaQueryWrapper<Order>()
                .eq(Order::getUserId, userId)
                .eq(Order::getProductId, productId)
                .eq(Order::getStatus, 0));
        return count > 0;
    }
}
