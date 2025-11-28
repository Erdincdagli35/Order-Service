package com.edsoft.order_service.data;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderCreateRequest {
    private List<OrderItemRequest> items;
    private BigDecimal personalId;
    private String roomNo;

    @Data
    public static class OrderItemRequest {
        private Long productId;
        private String productName;
        private Integer qty;

        public Long getProductId() {
            return productId;
        }

        public void setProductId(Long productId) {
            this.productId = productId;
        }

        public String getProductName() {
            return productName;
        }

        public void setProductName(String productName) {
            this.productName = productName;
        }

        public Integer getQty() {
            return qty;
        }

        public void setQty(Integer qty) {
            this.qty = qty;
        }
    }

    public List<OrderItemRequest> getItems() {
        return items;
    }

    public void setItems(List<OrderItemRequest> items) {
        this.items = items;
    }

    public BigDecimal getPersonalId() {
        return personalId;
    }

    public void setPersonalId(BigDecimal personalId) {
        this.personalId = personalId;
    }

    public String getRoomNo() {
        return roomNo;
    }

    public void setRoomNo(String roomNo) {
        this.roomNo = roomNo;
    }
}
