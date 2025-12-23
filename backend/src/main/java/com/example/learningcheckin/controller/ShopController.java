package com.example.learningcheckin.controller;

import com.example.learningcheckin.common.Result;
import com.example.learningcheckin.entity.Order;
import com.example.learningcheckin.entity.Product;
import com.example.learningcheckin.service.IProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/shop")
@CrossOrigin
public class ShopController {

    @Autowired
    private IProductService productService;

    @GetMapping("/products")
    public Result<List<Product>> getProducts() {
        return Result.success(productService.getProducts());
    }

    @GetMapping("/owned")
    public Result<List<Product>> getOwnedProducts(@RequestParam Long userId) {
        return Result.success(productService.getOwnedProducts(userId));
    }

    @PostMapping("/product")
    public Result<String> saveProduct(@RequestBody Product product) {
        if (product.getId() == null) {
            product.setCreateTime(LocalDateTime.now());
        }
        // product.setUpdateTime(LocalDateTime.now());
        productService.saveOrUpdate(product);
        return Result.success("保存成功");
    }
    
    @DeleteMapping("/product/{id}")
    public Result<String> deleteProduct(@PathVariable Long id) {
        productService.removeById(id);
        return Result.success("删除成功");
    }

    @PostMapping("/exchange")
    public Result<String> exchangeProduct(@RequestBody Map<String, Object> data) {
        Long userId = Long.valueOf(data.get("userId").toString());
        Long productId = Long.valueOf(data.get("productId").toString());
        
        Map<String, String> shippingInfo = null;
        if (data.containsKey("address")) {
            shippingInfo = new HashMap<>();
            shippingInfo.put("address", (String) data.get("address"));
            shippingInfo.put("name", (String) data.get("name"));
            shippingInfo.put("phone", (String) data.get("phone"));
        }

        try {
            productService.exchangeProduct(userId, productId, shippingInfo);
            return Result.success("兑换成功！");
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
    }

    @GetMapping("/history")
    public Result<List<Order>> getExchangeHistory(@RequestParam Long userId) {
        return Result.success(productService.getExchangeHistory(userId));
    }

    @PostMapping("/refund/{orderId}")
    public Result<String> refundProduct(@PathVariable Long orderId) {
        try {
            productService.refundProduct(orderId);
            return Result.success("退款成功！积分已退回");
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
    }
}
