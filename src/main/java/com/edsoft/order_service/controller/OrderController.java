package com.edsoft.order_service.controller;

import com.edsoft.order_service.data.OrderCreateRequest;
import com.edsoft.order_service.data.OrderListResponse;
import com.edsoft.order_service.model.Order;
import com.edsoft.order_service.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin({"https://orderservice.up.railway.app", "http://localhost:8080"})
public class OrderController {

    @Autowired
    OrderService orderService;

    @PostMapping("/create")
    public ResponseEntity<Order> createOrder(@RequestBody OrderCreateRequest req) throws Exception {
        Order created = orderService.createOrder(req);
        return ResponseEntity.status(200).body(created);
    }

    @GetMapping("/list")
    public List<Order> allOrders() {
        return orderService.listAllOrders();
    }

    @PutMapping("/edit/{id}")
    public ResponseEntity<Order> editOrder(@PathVariable Long id, @RequestBody OrderCreateRequest req) {
        Order edited = orderService.editOrder(id, req);
        return ResponseEntity.status(200).body(edited);
    }

    @GetMapping("/list/{id}")
    public ResponseEntity<Order> findOrder(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.listOrder(id));
    }

    @DeleteMapping("/cancel/{id}")
    public ResponseEntity<Void> cancelOrder(@PathVariable Long id) {
        orderService.cancelOrder(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/will/deliver/{id}")
    public ResponseEntity<Order> willDeliver(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.willDeliver(id));
    }

    @PutMapping("/delivered/{id}")
    public ResponseEntity<Order> delivered(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.delivered(id));
    }

    @GetMapping("/list/room/{roomNo}")
    public ResponseEntity<List<OrderByRoomResponse>> findOrderByRoom(@PathVariable String roomNo) {
        return ResponseEntity.ok(orderService.listOrderByRoomNo(roomNo));
    }
}
