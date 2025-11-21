package com.edsoft.order_service.service;

import com.edsoft.order_service.client.ProductClient;
import com.edsoft.order_service.data.OrderCreateRequest;
import com.edsoft.order_service.data.ProductResponse;
import com.edsoft.order_service.model.Item;
import com.edsoft.order_service.model.Order;
import com.edsoft.order_service.repository.OrderRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
        List<ProductResponse> products = req.getItems().stream()
                .map(i -> productClient.getProduct(i.getProductId()))
                .toList();

        List<Item> snapshot = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (int i = 0; i < req.getItems().size(); i++) {
            var itemReq = req.getItems().get(i);
            var product = products.get(i);
            BigDecimal price = Optional.ofNullable(product.getPrice()).orElse(BigDecimal.ZERO);

            total = total.add(price.multiply(BigDecimal.valueOf(itemReq.getQty())));

            snapshot.add(new Item(product.getName(), price, itemReq.getQty()));
        }

        Order order = new Order();
        order.setStatus("PENDING");
        order.setTotal(total);

        return orderRepository.save(order);
    }
}
