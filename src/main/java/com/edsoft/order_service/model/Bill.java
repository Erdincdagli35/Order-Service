package com.edsoft.order_service.model;

import jakarta.persistence.Embeddable;
import lombok.Data;

@Embeddable
public class Bill {
    private String productName;
    private Integer piece;

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
}
