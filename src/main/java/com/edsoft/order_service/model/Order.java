package com.edsoft.order_service.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.*;

@Entity
@Table(name = "orders")
@Data
public class Order {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String customerUsername; // auth'dan gelen user

    @Column(nullable=false)
    private String status; // CREATED, PROCESSING, DELIVERED

    private Instant createdAt = Instant.now();

    //Getter and Setter

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCustomerUsername() {
        return customerUsername;
    }

    public void setCustomerUsername(String customerUsername) {
        this.customerUsername = customerUsername;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}