package com.edsoft.order_service.model;


import java.math.BigDecimal;

public class OrderCreatedEvent {

    private Long orderId;
    private String roomNo;
    private BigDecimal price;

    public OrderCreatedEvent() {
    }

    public OrderCreatedEvent(Long orderId, String roomNo, BigDecimal price) {
        this.orderId = orderId;
        this.roomNo = roomNo;
        this.price = price;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getRoomNo() {
        return roomNo;
    }

    public void setRoomNo(String roomNo) {
        this.roomNo = roomNo;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    @Override
    public String toString() {
        return "OrderCreatedEvent{" +
                "orderId=" + orderId +
                ", roomNo='" + roomNo + '\'' +
                ", price=" + price +
                '}';
    }
}