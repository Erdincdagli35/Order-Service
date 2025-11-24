package com.edsoft.order_service.model;

import jakarta.persistence.Embeddable;
import lombok.Data;

import java.math.BigDecimal;

@Embeddable
public class Bill {
    private String productName;
    private Integer piece;
    private BigDecimal personalId;

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Integer getPiece() {
        return piece;
    }

    public void setPiece(Integer piece) {
        this.piece = piece;
    }

    public BigDecimal getPersonalId() {
        return personalId;
    }

    public void setPersonalId(BigDecimal personalId) {
        this.personalId = personalId;
    }
}
