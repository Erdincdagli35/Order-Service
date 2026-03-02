package com.edsoft.order_service.model;

import jakarta.persistence.Embeddable;

import java.math.BigDecimal;

@Embeddable
public class Bill {
    private String productName;
    private Integer piece;
    private String personalName;

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public long getPiece() {
        return piece;
    }

    public void setPiece(Integer piece) {
        this.piece = piece;
    }

    public String getPersonalName() {
        return personalName;
    }

    public void setPersonalName(String personalName) {
        this.personalName = personalName;
    }
}
