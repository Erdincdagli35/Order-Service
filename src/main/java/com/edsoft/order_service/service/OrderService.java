package com.edsoft.order_service.service;

import com.edsoft.order_service.client.ProductClient;
import com.edsoft.order_service.client.RoomClient;
import com.edsoft.order_service.data.OrderByRoomResponse;
import com.edsoft.order_service.data.OrderCreateRequest;
import com.edsoft.order_service.data.ProductResponse;
import com.edsoft.order_service.data.RoomResponse;
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
import java.math.RoundingMode;
import java.util.*;

@Service
public class OrderService {

    @Autowired
    private final ProductClient productClient;

    @Autowired
    private final RoomClient roomClient;

    @Autowired
    private final OrderRepository orderRepository;

    public OrderService(ProductClient productClient, RoomClient roomClient, OrderRepository orderRepository) {
        this.productClient = productClient;
        this.roomClient = roomClient;
        this.orderRepository = orderRepository;
    }

    public Order createOrder(OrderCreateRequest req) throws Exception {

        if (req == null || req.getItems() == null || req.getItems().isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one item");
        }

        // 1) Ürünleri çek
        List<Long> productIds = req.getItems()
                .stream()
                .map(OrderCreateRequest.OrderItemRequest::getProductId)
                .toList();

        Map<Long, ProductResponse> productMap = new HashMap<>();
        for (Long productId : productIds) {
            ProductResponse product = productClient.getProduct(productId); // handle exception upstream if not found
            if (product == null) {
                throw new Exception("Product not found: " + productId);
            }
            productMap.put(productId, product);
        }

        List<Bill> bills = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (OrderCreateRequest.OrderItemRequest itemReq : req.getItems()) {
            Long pId = itemReq.getProductId();
            Integer qty = itemReq.getQty() == null ? 0 : itemReq.getQty();

            if (qty <= 0) {
                throw new IllegalArgumentException("Quantity must be > 0 for product: " + pId);
            }

            ProductResponse product = productMap.get(pId);
            if (product == null) {
                throw new Exception("Product not found: " + pId);
            }

            BigDecimal unitPrice = product.getPrice() == null ? BigDecimal.ZERO : product.getPrice();
            BigDecimal itemTotal = unitPrice.multiply(BigDecimal.valueOf(qty));

            total = total.add(itemTotal);

            Bill bill = new Bill();
            bill.setProductName(product.getName());
            bill.setPiece(qty);
            bill.setPersonalId(req.getPersonalId());
            bills.add(bill);
        }

        // 4) Order oluştur ve kaydet
        Order order = new Order();
        order.setStatus("Pending");
        order.setTotal(total.setScale(2, RoundingMode.HALF_UP));
        order.setBills(bills);
        order.setPersonalId(req.getPersonalId());
        order.setRoomNo(req.getRoomNo());

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

    public Order willDeliver(Long id) {
        Order order = orderRepository.findOneById(id);
        order.setStatus("Will Deliver");
        return orderRepository.save(order);
    }

    public  Order delivered(Long id) {
        Order order = orderRepository.findOneById(id);
        order.setStatus("Delivered");
        return orderRepository.save(order);
    }

    public List<OrderByRoomResponse> listOrderByRoomNo() {
        List<RoomResponse> roomResponses = roomClient.getAllRoom();
        if (roomResponses == null || roomResponses.isEmpty()) {
            return Collections.emptyList();
        }

        List<Order> allOrders = listAllOrders();
        Map<String, Stats> statsMap = new HashMap<>();

        if (allOrders != null && !allOrders.isEmpty()) {
            for (Order order : allOrders) {
                if (order == null) continue;
                String rNo = order.getRoomNo();
                if (rNo == null) continue;

                Stats s = statsMap.computeIfAbsent(rNo, k -> new Stats());
                s.incrementOrderCount();

                // Buradaki önemli değişiklik: artık order.getTotal() kullanıyoruz
                BigDecimal orderTotal = order.getTotal() == null ? BigDecimal.ZERO : order.getTotal();
                s.addRevenue(orderTotal);
            }
        }

        List<OrderByRoomResponse> result = new ArrayList<>(roomResponses.size());
        for (RoomResponse room : roomResponses) {
            if (room == null) continue;
            String rNo = room.getRoomNo();
            Stats s = (rNo == null) ? null : statsMap.get(rNo);

            int orderCount = (s == null) ? 0 : s.getOrderCount();
            BigDecimal totalRevenue = (s == null) ? BigDecimal.ZERO : s.getTotalRevenue();

            OrderByRoomResponse resp = buildOrderByRoomResponse(room, orderCount, totalRevenue);
            result.add(resp);
        }

        // opsiyonel: odaları roomNo'ya göre sırala
        result.sort(Comparator.comparing(OrderByRoomResponse::getRoomNo, Comparator.nullsLast(String::compareTo)));

        return result;
    }

    /** Basit aggregation taşıyıcı sınıfı */
    private static class Stats {
        private int orderCount = 0;
        private BigDecimal totalRevenue = BigDecimal.ZERO;

        void incrementOrderCount() { orderCount++; }
        void addRevenue(BigDecimal amount) {
            if (amount != null) {
                totalRevenue = totalRevenue.add(amount);
            }
        }
        int getOrderCount() { return orderCount; }
        BigDecimal getTotalRevenue() { return totalRevenue; }
    }

    private OrderByRoomResponse buildOrderByRoomResponse(RoomResponse room, int orderCount, BigDecimal totalRevenue) {
        OrderByRoomResponse r = new OrderByRoomResponse();
        r.setRoomNo(room.getRoomNo());
        r.setDescription(room.getDescription());
        r.setOrderCount(orderCount);
        r.setTotalRevenue(totalRevenue == null ? BigDecimal.ZERO : totalRevenue);
        return r;
    }
}
