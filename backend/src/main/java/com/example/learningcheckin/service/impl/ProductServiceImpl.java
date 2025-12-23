package com.example.learningcheckin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.learningcheckin.entity.Order;
import com.example.learningcheckin.entity.PointsRecord;
import com.example.learningcheckin.entity.Product;
import com.example.learningcheckin.entity.User;
import com.example.learningcheckin.mapper.OrderMapper;
import com.example.learningcheckin.mapper.PointsRecordMapper;
import com.example.learningcheckin.mapper.ProductMapper;
import com.example.learningcheckin.mapper.UserMapper;
import com.example.learningcheckin.service.IProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.redis.core.StringRedisTemplate;
import java.util.concurrent.TimeUnit;
import java.util.UUID;

@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements IProductService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private PointsRecordMapper pointsRecordMapper;

    @Override
    public List<Product> getProducts() {
        return this.list();
    }

    @Override
    public List<Product> getOwnedProducts(Long userId) {
        List<Order> orders = orderMapper.selectList(new LambdaQueryWrapper<Order>()
                .eq(Order::getUserId, userId)
                .in(Order::getStatus, 0, 2) // Valid and Expired orders
        );
        
        if (orders.isEmpty()) {
            return Collections.emptyList();
        }
        
        List<Long> productIds = orders.stream()
                .map(Order::getProductId)
                .distinct()
                .collect(Collectors.toList());
                
        List<Product> products = this.listByIds(productIds);
        
        // Populate expireTime based on latest order
        for (Product product : products) {
            if (product.getDays() != null && product.getDays() > 0) {
                orders.stream()
                        .filter(o -> o.getProductId().equals(product.getId()))
                        .max((o1, o2) -> o1.getCreateTime().compareTo(o2.getCreateTime()))
                        .ifPresent(latestOrder -> {
                            product.setExpireTime(latestOrder.getCreateTime().plusDays(product.getDays()));
                        });
            }
        }
        
        return products;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void exchangeProduct(Long userId, Long productId, java.util.Map<String, String> shippingInfo) {
        String lockKey = "lock:product:" + productId;
        String clientId = UUID.randomUUID().toString();
        boolean locked = false;
        int retryCount = 0;

        try {
            // Redis Distributed Lock with Retry
            while (retryCount < 3) {
                locked = Boolean.TRUE.equals(redisTemplate.opsForValue().setIfAbsent(lockKey, clientId, 30, TimeUnit.SECONDS));
                if (locked) break;
                retryCount++;
                try { Thread.sleep(200); } catch (InterruptedException e) {}
            }

            if (!locked) {
                throw new RuntimeException("System busy, please try again later.");
            }

            // 1. Validate Product
            Product product = this.getById(productId);
            if (product == null) {
                throw new RuntimeException("Product not found");
            }
            if (product.getStatus() != null && product.getStatus() == 0) {
                throw new RuntimeException("Product is off-shelf");
            }
            if (product.getValidUntil() != null && product.getValidUntil().isBefore(LocalDateTime.now())) {
                throw new RuntimeException("Product listing has expired");
            }
            if (product.getStock() <= 0) {
                throw new RuntimeException("Out of stock");
            }

            // 2. Calculate Price (Renewal Discount)
            int finalPrice = product.getPrice();
            if (product.getDays() != null && product.getDays() > 0) {
                Long historyCount = orderMapper.selectCount(new LambdaQueryWrapper<Order>()
                        .eq(Order::getUserId, userId)
                        .eq(Order::getProductId, productId)
                        .in(Order::getStatus, 0, 2));
                if (historyCount > 0) {
                    finalPrice = (int) (product.getPrice() * 0.8);
                }
            }

            // 3. Check & Deduct Points (Atomic)
            int rows = userMapper.update(null, new LambdaUpdateWrapper<User>()
                .setSql("points = points - " + finalPrice)
                .eq(User::getId, userId)
                .ge(User::getPoints, finalPrice));
            
            if (rows == 0) {
                User u = userMapper.selectById(userId);
                throw new RuntimeException("Insufficient points. Need " + finalPrice + ", Have " + (u != null ? u.getPoints() : 0));
            }

            // 4. Deduct Stock (Atomic)
            boolean stockDeducted = this.update(new LambdaUpdateWrapper<Product>()
                .setSql("stock = stock - 1")
                .eq(Product::getId, productId)
                .gt(Product::getStock, 0));

            if (!stockDeducted) {
                throw new RuntimeException("Out of stock");
            }

            // 5. Create Order
            Order order = new Order();
            order.setUserId(userId);
            order.setProductId(productId);
            order.setPrice(finalPrice);
            order.setStatus(0);
            order.setIsAbnormal(false);
            order.setCreateTime(LocalDateTime.now());
            
            // Shipping Info
            if (shippingInfo != null) {
                order.setReceiverName(shippingInfo.get("name"));
                order.setReceiverPhone(shippingInfo.get("phone"));
                order.setShippingAddress(shippingInfo.get("address"));
                order.setShippingStatus(0); // Pending
            }
            
            orderMapper.insert(order);

            // 6. Record Points
            PointsRecord record = new PointsRecord();
            record.setUserId(userId);
            record.setType(2); // Consume
            record.setAmount(finalPrice);
            record.setDescription("Exchange Product: " + product.getName());
            record.setCreateTime(LocalDateTime.now());
            pointsRecordMapper.insert(record);

        } finally {
            if (locked && clientId.equals(redisTemplate.opsForValue().get(lockKey))) {
                redisTemplate.delete(lockKey);
            }
        }
    }

    @Override
    public List<Order> getExchangeHistory(Long userId) {
        return orderMapper.selectList(new LambdaQueryWrapper<Order>()
                .eq(Order::getUserId, userId)
                .orderByDesc(Order::getCreateTime));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void refundProduct(Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new RuntimeException("Order not found");
        }
        if (order.getStatus() != null && order.getStatus() == 1) {
            throw new RuntimeException("Order already refunded");
        }

        User user = userMapper.selectById(order.getUserId());
        Product product = this.getById(order.getProductId());
        
        // 1. Return Points
        user.setPoints(user.getPoints() + order.getPrice());
        userMapper.updateById(user);

        // 2. Restore Stock
        if (product != null) {
            product.setStock(product.getStock() + 1);
            this.updateById(product);
        }

        // 3. Update Order Status
        order.setStatus(1);
        orderMapper.updateById(order);

        // 4. Record Points History
        PointsRecord pr = new PointsRecord();
        pr.setUserId(user.getId());
        pr.setType(1); // Gain
        pr.setAmount(order.getPrice());
        pr.setDescription("Refund: " + (product != null ? product.getName() : "Unknown Product"));
        pr.setCreateTime(LocalDateTime.now());
        pointsRecordMapper.insert(pr);
    }

    @Override
    public com.baomidou.mybatisplus.extension.plugins.pagination.Page<Order> getOrders(com.baomidou.mybatisplus.extension.plugins.pagination.Page<Order> page, Long userId, Long orderId) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        if (userId != null) {
            wrapper.eq(Order::getUserId, userId);
        }
        if (orderId != null) {
            wrapper.eq(Order::getId, orderId);
        }
        wrapper.orderByDesc(Order::getCreateTime);
        return orderMapper.selectPage(page, wrapper);
    }

    @Override
    public void shipOrder(Long orderId, String trackingNumber) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) throw new RuntimeException("Order not found");
        order.setTrackingNumber(trackingNumber);
        order.setShippingStatus(1); // Shipped
        orderMapper.updateById(order);
    }

    @Override
    public void markOrderAbnormal(Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) throw new RuntimeException("Order not found");
        order.setIsAbnormal(true);
        orderMapper.updateById(order);
    }

    @Override
    public void cancelOrderAbnormal(Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) throw new RuntimeException("Order not found");
        order.setIsAbnormal(false);
        orderMapper.updateById(order);
    }
}
