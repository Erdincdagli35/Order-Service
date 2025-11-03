package com.edsoft.order_service.controller;

import com.edsoft.order_service.model.Order;
import com.edsoft.order_service.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin("https://beachorder.up.railway.app/")
public class OrderController {

    @Autowired
    OrderService orderService;

    @PostMapping
    public ResponseEntity<Order> createOrder(@RequestBody Order order, Principal principal) {
        String user = principal != null ? principal.getName() : order.getCustomerUsername();
        Order saved = orderService.createOrder(user, order.getItems());
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping
    public ResponseEntity<List<Order>> myOrders(Principal principal) {
        return ResponseEntity.ok(orderService.listAllOrders());
    }

    @GetMapping("all")
    public List<Order> allOrders() {
        return orderService.listAllOrders();
    }

    @PostMapping("/{id}/deliver")
    public Order deliver(@PathVariable Long id) { return orderService.markDelivered(id); }
}

