package com.edsoft.order_service.controller;

import com.edsoft.order_service.data.OrderCreateRequest;
import com.edsoft.order_service.model.Order;
import com.edsoft.order_service.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin({"https://orderservice.up.railway.app", "http://localhost:8080"})
public class OrderController {

    @Autowired
    OrderService orderService;

    @PostMapping("/create")
    public ResponseEntity<Order> createOrder(@RequestBody OrderCreateRequest req) {
        Order created = orderService.createOrder(req);
        return ResponseEntity.status(200).body(created);
    }

    /*
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
    */
}
