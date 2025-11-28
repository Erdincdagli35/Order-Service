package com.edsoft.order_service.controller;

import com.edsoft.order_service.model.Bill;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderByRoomResponse {
    private Long orderId;
    private String status;
    private BigDecimal total;
    private List<Bill> bills;
    private BigDecimal personalId;
    private String roomNo;

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public List<Bill> getBills() {
        return bills;
    }

    public void setBills(List<Bill> bills) {
        this.bills = bills;
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
