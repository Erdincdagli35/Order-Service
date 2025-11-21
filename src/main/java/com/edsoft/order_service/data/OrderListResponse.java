package com.edsoft.order_service.data;

import com.edsoft.order_service.model.Order;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderListResponse {
    private Order order;
    private String productName;

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }
}
