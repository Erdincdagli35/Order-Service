package com.edsoft.order_service.model;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class Item {
    private String name;
    private int quantity;
    private BigDecimal price;

    // Boş constructor (Jackson için)
    public Item() {}

    public Item(String name, int quantity, BigDecimal price) {
        this.name = name;
        this.quantity = quantity;
        this.price = price;
    }

    public Item(String name, BigDecimal price, Integer qty) {
        this.name = name;
        this.quantity = qty;
        this.price = price;
    }



    // Getter & Setter
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
}
