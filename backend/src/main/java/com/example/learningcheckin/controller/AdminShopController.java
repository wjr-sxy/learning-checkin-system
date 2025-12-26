package com.example.learningcheckin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.learningcheckin.common.Result;
import com.example.learningcheckin.entity.Product;
import com.example.learningcheckin.entity.Order;
import com.example.learningcheckin.service.IProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/shop")
@CrossOrigin
public class AdminShopController {

    @Autowired
    private IProductService productService;

    @GetMapping("/list")
    public Result<Page<Product>> list(@RequestParam(defaultValue = "1") Integer page,
                                      @RequestParam(defaultValue = "10") Integer size,
                                      @RequestParam(required = false) String name,
                                      @RequestParam(required = false) String category) {
        System.out.println("AdminShopController.list called: page=" + page + ", size=" + size + ", name=" + name + ", category=" + category);
        Page<Product> productPage = new Page<>(page, size);
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        if (name != null && !name.isEmpty()) {
            wrapper.like(Product::getName, name);
        }
        if (category != null && !category.isEmpty()) {
            wrapper.eq(Product::getCategory, category);
        }
        wrapper.orderByDesc(Product::getCreateTime);
        Page<Product> result = productService.page(productPage, wrapper);
        System.out.println("AdminShopController.list result: total=" + result.getTotal() + ", records=" + result.getRecords().size());
        return Result.success(result);
    }

    @PostMapping("/add")
    public Result<Product> add(@RequestBody Product product) {
        System.out.println("AdminShopController.add called: " + product);
        // Validation
        if (product.getName() == null || product.getName().length() < 1 || product.getName().length() > 50) {
            return Result.error(400, "Name must be 1-50 characters");
        }
        if (product.getDescription() != null && product.getDescription().length() > 500) {
            return Result.error(400, "Description max 500 characters");
        }
        if (product.getPrice() == null || product.getPrice() < 1 || product.getPrice() > 99999) {
            return Result.error(400, "Points must be 1-99999");
        }
        if (product.getStock() == null || product.getStock() < 0 || product.getStock() > 99999) {
            return Result.error(400, "Stock must be 0-99999");
        }
        if (product.getValidUntil() != null && product.getValidUntil().isBefore(LocalDateTime.now())) {
            return Result.error(400, "Validity must be future time");
        }
        
        product.setCreateTime(LocalDateTime.now());
        product.setStatus(1); // Default On-shelf
        productService.save(product);
        return Result.success(product);
    }

    @PutMapping("/update")
    public Result<Product> update(@RequestBody Product product) {
        if (product.getId() == null) {
            return Result.error(400, "ID required");
        }
        // Validation (Same as add)
         if (product.getName() != null && (product.getName().length() < 1 || product.getName().length() > 50)) {
            return Result.error(400, "Name must be 1-50 characters");
        }
        productService.updateById(product);
        return Result.success(product);
    }

    @DeleteMapping("/delete/{id}")
    public Result<String> delete(@PathVariable Long id) {
        Product product = productService.getById(id);
        if (product == null) {
            return Result.error(404, "Product not found");
        }
        try {
            boolean removed = productService.removeById(id);
            if (removed) {
                return Result.success("Deleted successfully");
            } else {
                return Result.error(500, "Delete failed");
            }
        } catch (Exception e) {
             return Result.error(500, "Cannot delete product: " + e.getMessage());
        }
    }

    @PostMapping("/status/{id}")
    public Result<String> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        Product product = productService.getById(id);
        if (product == null) return Result.error(404, "Product not found");
        product.setStatus(status);
        productService.updateById(product);
        return Result.success("Status updated");
    }

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    @PostMapping("/upload")
    public Result<String> uploadImage(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error(400, "File is empty");
        }
        if (file.getSize() > 2 * 1024 * 1024) {
             return Result.error(400, "File size exceeds 2MB");
        }
        String fileName = file.getOriginalFilename();
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        if (!".jpg".equalsIgnoreCase(suffix) && !".png".equalsIgnoreCase(suffix) && !".jpeg".equalsIgnoreCase(suffix)) {
             return Result.error(400, "Only JPG/PNG supported");
        }
        
        try {
            File directory = new File(uploadDir);
            if (!directory.exists()) {
                directory.mkdirs();
            }
            String newFileName = UUID.randomUUID().toString() + suffix;
            java.nio.file.Path path = java.nio.file.Paths.get(uploadDir + File.separator + newFileName);
            java.nio.file.Files.write(path, file.getBytes());
            return Result.success("/uploads/" + newFileName);
        } catch (IOException e) {
            e.printStackTrace();
            return Result.error(500, "Upload failed");
        }
    }

    @GetMapping("/orders")
    public Result<Page<Order>> getOrders(@RequestParam(defaultValue = "1") Integer page,
                                         @RequestParam(defaultValue = "10") Integer size,
                                         @RequestParam(required = false) Long userId,
                                         @RequestParam(required = false) Long orderId) {
        Page<Order> orderPage = new Page<>(page, size);
        return Result.success(productService.getOrders(orderPage, userId, orderId));
    }

    @PostMapping("/orders/{id}/ship")
    public Result<String> shipOrder(@PathVariable Long id, @RequestBody java.util.Map<String, String> body) {
        String trackingNumber = body.get("trackingNumber");
        if (trackingNumber == null || trackingNumber.isEmpty()) return Result.error(400, "Tracking number required");
        productService.shipOrder(id, trackingNumber);
        return Result.success("Shipped");
    }

    @PostMapping("/orders/{id}/refund")
    public Result<String> refundOrder(@PathVariable Long id) {
        productService.refundProduct(id);
        return Result.success("Refunded");
    }

    @PostMapping("/orders/{id}/abnormal")
    public Result<String> markAbnormal(@PathVariable Long id) {
        productService.markOrderAbnormal(id);
        return Result.success("Marked as abnormal");
    }

    @PostMapping("/orders/{id}/cancel-abnormal")
    public Result<String> cancelAbnormal(@PathVariable Long id) {
        productService.cancelOrderAbnormal(id);
        return Result.success("Abnormal status cancelled");
    }
}
