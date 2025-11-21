package com.edsoft.order_service.service;

import com.edsoft.order_service.client.ProductClient;
import com.edsoft.order_service.data.OrderCreateRequest;
import com.edsoft.order_service.data.ProductResponse;
import com.edsoft.order_service.model.Item;
import com.edsoft.order_service.model.Order;
import com.edsoft.order_service.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class OrderService {

    @Autowired
    private final ProductClient productClient;

    @Autowired
    private final OrderRepository orderRepository;

    @Autowired
    private final ObjectMapper objectMapper;

    public OrderService(ProductClient productClient, OrderRepository orderRepository, ObjectMapper objectMapper) {
        this.productClient = productClient;
        this.orderRepository = orderRepository;
        this.objectMapper = objectMapper;
    }

    public Order createOrder(OrderCreateRequest req) {

        // 1) Ürünleri product service'den çek
        List<ProductResponse> products = req.getItems().stream()
                .map(i -> productClient.getProduct(i.getProductId()))
                .toList();

        // 2) Snapshot oluştur
        BigDecimal total = BigDecimal.ZERO;
        List<Item> snapshot = new ArrayList<>();

        for (int i = 0; i < req.getItems().size(); i++) {
            OrderCreateRequest.OrderItemRequest itemReq = req.getItems().get(i); // tip explicit
            ProductResponse product = products.get(i);        // tip explicit

            BigDecimal productPrice = product.getPrice() != null
                    ? product.getPrice()  // direkt BigDecimal olarak al
                    : BigDecimal.ZERO;



            BigDecimal lineTotal = productPrice.multiply(BigDecimal.valueOf(itemReq.getQty()));
            total = total.add(lineTotal);

            Item item = new Item();
            item.setName(Objects.toString(product.getName(), ""));
            item.setQuantity(itemReq.getQty());
            item.setPrice(productPrice);

            snapshot.add(item);
        }

        // 3) Order entity oluştur
        Order order = new Order();
        order.setStatus("PENDING");
        order.setItems(snapshot);  // artık List<Item> olarak set ediliyor
        order.setTotal(total);

        // 4) DB'ye kaydet
        orderRepository.save(order);

        return order;
    }
}
