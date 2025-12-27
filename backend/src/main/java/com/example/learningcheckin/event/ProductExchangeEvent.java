package com.example.learningcheckin.event;

import org.springframework.context.ApplicationEvent;
import lombok.Getter;

@Getter
public class ProductExchangeEvent extends ApplicationEvent {
    private final Long userId;
    private final Long productId;
    private final Long orderId;
    private final Integer price;

    public ProductExchangeEvent(Object source, Long userId, Long productId, Long orderId, Integer price) {
        super(source);
        this.userId = userId;
        this.productId = productId;
        this.orderId = orderId;
        this.price = price;
    }
}
