package com.edsoft.order_service.service;

import com.edsoft.order_service.client.ProductClient;
import com.edsoft.order_service.data.OrderCreateRequest;
import com.edsoft.order_service.data.ProductResponse;
import com.edsoft.order_service.model.Bill;
import com.edsoft.order_service.model.Order;
import com.edsoft.order_service.repository.OrderRepository;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class OrderService {

    @Autowired
    private final ProductClient productClient;

    @Autowired
    private final OrderRepository orderRepository;

    public OrderService(ProductClient productClient, OrderRepository orderRepository) {
        this.productClient = productClient;
        this.orderRepository = orderRepository;
    }

    public Order createOrder(OrderCreateRequest req) {

        // 1) Ürünleri çek
        List<Long> productIds = req.getItems()
                .stream()
                .map(OrderCreateRequest.OrderItemRequest::getProductId)
                .toList();

        List<ProductResponse> products = new ArrayList<>();

        for (Long productId : productIds) {
            ProductResponse product = productClient.getProduct(productId);
            products.add(product);
        }

        List<Bill> bills = new ArrayList<>();

        // 3) Order oluştur
        Order order = new Order();
        order.setStatus("Pending");

        // 2) Toplam fiyatı hesapla
        BigDecimal total = BigDecimal.ZERO;

        for (int i = 0; i < req.getItems().size(); i++) {
            OrderCreateRequest.OrderItemRequest itemReq = req.getItems().get(i);
            ProductResponse product = products.get(i);

            BigDecimal price = product.getPrice() != null
                    ? product.getPrice()
                    : BigDecimal.ZERO;

            BigDecimal itemTotal = price.multiply(BigDecimal.valueOf(itemReq.getQty()));

            total = total.add(itemTotal);
            Bill bill = new Bill();

            for (OrderCreateRequest.OrderItemRequest itemRequest : req.getItems()){
                bill.setPiece(itemRequest.getQty());
            }

            bill.setProductName(product.getName());
            bills.add(bill);
        }

        order.setTotal(total);
        order.setBills(bills);

        return orderRepository.save(order);
    }


    public List<Order> listAllOrders() {

        List<Order> orders = orderRepository.findAllByOrderByIdDesc();
        return orders;
    }

    public @Nullable Order listOrder(Long id) {
        return orderRepository.findOneById(id);
    }

    @Transactional
    public Order editOrder(Long orderId, OrderCreateRequest req) {
        // 1) Order'ı bul
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));

        // 1) Ürünleri çek
        List<Long> productIds = req.getItems()
                .stream()
                .map(OrderCreateRequest.OrderItemRequest::getProductId)
                .toList();

        List<ProductResponse> products = new ArrayList<>();

        for (Long productId : productIds) {
            ProductResponse product = productClient.getProduct(productId);
            products.add(product);
        }

        List<Bill> bills = new ArrayList<>();

        order.setStatus("Pending");

        // 2) Toplam fiyatı hesapla
        BigDecimal total = BigDecimal.ZERO;

        for (int i = 0; i < req.getItems().size(); i++) {
            OrderCreateRequest.OrderItemRequest itemReq = req.getItems().get(i);
            ProductResponse product = products.get(i);

            BigDecimal price = product.getPrice() != null
                    ? product.getPrice()
                    : BigDecimal.ZERO;

            BigDecimal itemTotal = price.multiply(BigDecimal.valueOf(itemReq.getQty()));

            total = total.add(itemTotal);
            Bill bill = new Bill();

            for (OrderCreateRequest.OrderItemRequest itemRequest : req.getItems()) {
                bill.setPiece(itemRequest.getQty());
            }

            bill.setProductName(product.getName());
            bills.add(bill);
        }

        order.setTotal(total);
        order.setBills(bills);

        return orderRepository.save(order);
    }

    public void cancelOrder(Long id) {
        Order order = orderRepository.findOneById(id);
        orderRepository.delete(order);
    }
}
