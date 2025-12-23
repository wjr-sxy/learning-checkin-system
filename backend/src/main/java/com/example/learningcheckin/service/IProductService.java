package com.example.learningcheckin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.learningcheckin.entity.Order;
import com.example.learningcheckin.entity.Product;
import java.util.List;

public interface IProductService extends IService<Product> {
    List<Product> getProducts();
    List<Product> getOwnedProducts(Long userId);
    void exchangeProduct(Long userId, Long productId, java.util.Map<String, String> shippingInfo);
    void refundProduct(Long orderId);
    List<Order> getExchangeHistory(Long userId);

    // Admin
    com.baomidou.mybatisplus.extension.plugins.pagination.Page<Order> getOrders(com.baomidou.mybatisplus.extension.plugins.pagination.Page<Order> page, Long userId, Long orderId);
    void shipOrder(Long orderId, String trackingNumber);
    void markOrderAbnormal(Long orderId);
    void cancelOrderAbnormal(Long orderId);
}
